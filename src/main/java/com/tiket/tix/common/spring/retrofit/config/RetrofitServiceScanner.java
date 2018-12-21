package com.tiket.tix.common.spring.retrofit.config;

import com.tiket.tix.common.spring.retrofit.annotation.RetrofitServiceScan;
import com.tiket.tix.common.spring.retrofit.annotation.RetrofitService;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Scanner for interface marked with {@link RetrofitService} annotation. Scanning target defined by
 * {@link RetrofitServiceScan} annotation.
 *
 * @author zakyalvan
 */
class RetrofitServiceScanner extends ClassPathScanningCandidateComponentProvider {
    RetrofitServiceScanner() {
        super(false);
        addIncludeFilter(new AnnotationTypeFilter(RetrofitService.class, true, true));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface();
    }
}
