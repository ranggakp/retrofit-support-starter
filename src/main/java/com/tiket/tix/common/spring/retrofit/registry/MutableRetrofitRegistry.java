package com.tiket.tix.common.spring.retrofit.registry;

import org.hibernate.validator.constraints.NotBlank;
import retrofit2.Retrofit;

import javax.validation.constraints.NotNull;

/**
 * @author zakyalvan
 */
public interface MutableRetrofitRegistry extends RetrofitRegistry {
    /**
     * Add {@link Retrofit} instance into the registry.
     *
     * @param name
     * @param retrofit
     */
    RetrofitRegistry register(@NotBlank String name, @NotNull Retrofit retrofit);

    /**
     * Remove {@link Retrofit} instance from the registry.
     *
     * @param name
     * @return
     */
    RetrofitRegistry unregister(@NotBlank String name);
}
