package com.tiket.tix.common.spring.retrofit.registry;

import org.hibernate.validator.constraints.NotBlank;
import retrofit2.Retrofit;

import java.util.Optional;

/**
 * Contract for type responsible for maintaining {@link Retrofit} object.
 *
 * @author zakyalvan
 */
public interface RetrofitRegistry {
    String DEFAULT_RETROFIT = "__defaultRetrofit";

    /**
     * Check whether given name already registered in this registry.
     *
     * @param name
     * @return
     */
    boolean contains(@NotBlank String name);

    /**
     * Retrieve {@link Retrofit} instance with given identifier from registry.
     *
     * @param name
     * @return
     */
    Optional<Retrofit> get(@NotBlank String name);
}
