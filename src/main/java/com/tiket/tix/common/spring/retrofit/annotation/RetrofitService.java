package com.tiket.tix.common.spring.retrofit.annotation;

import com.tiket.tix.common.spring.retrofit.registry.RetrofitRegistry;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Marker for retrofit service interface, so can be detected by scanner on context startup.
 *
 * @author zakyalvan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
@Component
public @interface RetrofitService {
    /**
     * Bean identifier of created retrofit client instance.
     *
     * @return
     */
    @AliasFor("name")
    String value() default "";

    /**
     * Bean identifier of created retrofit client instance.
     *
     * @return
     */
    @AliasFor("value")
    String name() default "";

    /**
     * {@link retrofit2.Retrofit} instance identifier, which registered in {@link RetrofitRegistry} bean,
     * responsible for creating this retrofit service.
     *
     * @return
     */
    String retrofit() default RetrofitRegistry.DEFAULT_RETROFIT;

    /**
     * Flag whether the client bean is singleton or not.
     *
     * @return
     */
    boolean singleton() default true;
}
