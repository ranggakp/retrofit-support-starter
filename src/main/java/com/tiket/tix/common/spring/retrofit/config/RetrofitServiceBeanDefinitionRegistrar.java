package com.tiket.tix.common.spring.retrofit.config;

import com.tiket.tix.common.spring.retrofit.annotation.RetrofitServiceScan;
import com.tiket.tix.common.spring.retrofit.annotation.RetrofitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An {@link ImportBeanDefinitionRegistrar} to be imported when {@link RetrofitServiceScan} used.
 *
 * @author zakyalvan
 * @see RetrofitServiceScan
 */
public class RetrofitServiceBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetrofitServiceBeanDefinitionRegistrar.class);

    private final RetrofitServiceScanner clientScanner = new RetrofitServiceScanner();

    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanRegistry) {
        LOGGER.debug("Start registering retrofit service as spring bean");
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(RetrofitServiceScan.class.getName()));

        boolean enabled = attributes.getBoolean("enabled");
        if(!enabled) {
            LOGGER.debug("Scanning retrofit service interface is disabled, cancel scanning.");
            return;
        }

        if (!beanRegistry.containsBeanDefinition(RetrofitServiceBeanPostProcessor.BEAN_NAME)) {
            beanRegistry.registerBeanDefinition(RetrofitServiceBeanPostProcessor.BEAN_NAME,
                    new RootBeanDefinition(RetrofitServiceBeanPostProcessor.class));
        }
        registerRetrofitServiceBeans(annotationMetadata, beanRegistry);
    }

    /**
     * Process registration of {@link RetrofitService} interface as spring bean, this with start scanning
     * service definition based on attirubutes given in {@link RetrofitServiceScan}.
     *
     * @param annotationMetadata
     * @param beanRegistry
     */
    private void registerRetrofitServiceBeans(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanRegistry) {
        Set<BeanDefinition> candidateDefinitions = resolveCandidatePackages(annotationMetadata).parallelStream()
                .flatMap(basePackage -> clientScanner.findCandidateComponents(basePackage).stream())
                .map(definition -> {
                    try {
                        Class<?> candidateType = ClassUtils.forName(definition.getBeanClassName(), classLoader);
                        RetrofitService serviceAnnotation = AnnotationUtils.findAnnotation(candidateType, RetrofitService.class);
                        if(serviceAnnotation.singleton()) {
                            definition.setScope(GenericBeanDefinition.SCOPE_SINGLETON);
                        }
                        else {
                            definition.setScope(GenericBeanDefinition.SCOPE_PROTOTYPE);
                        }
                        return definition;
                    }
                    catch (ClassNotFoundException e) {
                        throw new IllegalStateException("Retrofit client contract find by scanner but class not found", e);
                    }
                })
                .collect(Collectors.toSet());

        if(!candidateDefinitions.isEmpty()) {
            processServiceRegistration(candidateDefinitions, beanRegistry);
        }
    }

    /**
     * Resolve packages to be scanned, looking for {@link RetrofitService} interface.
     * This is based on attributes of {@link RetrofitServiceScan}.
     *
     * @param annotationMetadata
     * @return
     */
    private Set<String> resolveCandidatePackages(AnnotationMetadata annotationMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(RetrofitServiceScan.class.getName()));

        String[] value = attributes.getStringArray("value");
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");

        if (!ObjectUtils.isEmpty(value)) {
            Assert.state(ObjectUtils.isEmpty(basePackages),
                    "@RetrofitServiceScan basePackages and value attributes are mutually exclusive");
        }

        Set<String> packagesToScan = new LinkedHashSet<>();
        packagesToScan.addAll(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));

        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }

        if (packagesToScan.isEmpty()) {
            return Collections.singleton(ClassUtils.getPackageName(annotationMetadata.getClassName()));
        }

        return packagesToScan;
    }

    private void processServiceRegistration(Set<BeanDefinition> candidateDefinitions, BeanDefinitionRegistry beanRegistry) {
        for (BeanDefinition candidate : candidateDefinitions) {
            beanRegistry.registerBeanDefinition(decideServiceName(candidate), candidate);
        }
    }

    private String decideServiceName(BeanDefinition beanDefinition) {
        try {
            Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
            RetrofitService client = beanClass.getAnnotation(RetrofitService.class);

            if (client != null && StringUtils.hasText(client.value())) {
                return client.value();
            }

            if (client != null && StringUtils.hasText(client.name())) {
                return client.name();
            }

            Qualifier qualifier = beanClass.getAnnotation(Qualifier.class);
            if (qualifier != null && StringUtils.hasText(qualifier.value())) {
                return qualifier.value();
            }

            return beanClass.getName();

        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException("Retrofit client contract find by scanner but class not found when calculating the name", e);
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
