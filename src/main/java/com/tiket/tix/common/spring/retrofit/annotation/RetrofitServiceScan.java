package com.tiket.tix.common.spring.retrofit.annotation;

import com.tiket.tix.common.spring.retrofit.config.RetrofitServiceBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Scan retrofit client interface.
 *
 * @author zakyalvan
 * @see RetrofitServiceBeanDefinitionRegistrar
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RetrofitServiceBeanDefinitionRegistrar.class)
public @interface RetrofitServiceScan {
    /**
     * Flag whether {@link RetrofitService} scan enabled or disabled.
     *
     * @return
     */
    boolean enabled() default true;

    /**
     * Base packages to be scanned for {@link RetrofitService}.
     *
     * @return
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to be scanned for {@link RetrofitService}.
     *
     * @return
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * Base packages to be scanned for {@link RetrofitService}.
     *
     * @return
     */
    Class<?>[] basePackageClasses() default {};
}
