package com.tiket.tix.common.spring.retrofit.config;

import com.tiket.tix.common.spring.retrofit.annotation.RetrofitService;
import com.tiket.tix.common.spring.retrofit.registry.RetrofitRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.StringUtils;

/**
 * Post process detected interface type marked with {@link RetrofitService}, then create the instance.
 *
 * @author zakyalvan
 */
public class RetrofitServiceBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements BeanFactoryAware {
    public static final String BEAN_NAME = "retrofitServiceBeanPostProcessor";

    private BeanFactory beanFactory;

    private RetrofitServiceFactory serviceFactory;

    /**
     * Create real retrofit service bean if found in spring context. This will be delegated to {@link RetrofitServiceFactory}.
     *
     * Return null to continue using default instantiation of non {@link RetrofitService} type.
     *
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if (!beanClass.isAnnotationPresent(RetrofitService.class)) {
            return null;
        }

        RetrofitService serviceAnnotation = beanClass.getAnnotation(RetrofitService.class);
        String retrofitName = !StringUtils.hasText(serviceAnnotation.retrofit()) ?
                serviceAnnotation.value() : serviceAnnotation.retrofit();

        if(serviceFactory == null) {
            serviceFactory = createServiceFactory();
        }

        return serviceFactory.createInstance(beanClass, retrofitName);
    }

    /**
     * Create {@link RetrofitServiceFactory} which responsible to create {@link RetrofitService} bean.
     *
     * @return
     */
    private RetrofitServiceFactory createServiceFactory() {
        if(beanFactory == null) {
            throw  new BeanCreationException("Can not create retrofit service retrofit, no bean retrofit instance given. It might be unmanaged spring context");
        }

        RetrofitRegistry retrofitRegistry = beanFactory.getBean(RetrofitRegistry.class);
        return new RetrofitServiceFactory(retrofitRegistry);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
