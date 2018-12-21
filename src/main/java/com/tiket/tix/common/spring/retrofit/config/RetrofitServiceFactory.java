package com.tiket.tix.common.spring.retrofit.config;

import com.tiket.tix.common.spring.retrofit.registry.RetrofitRegistry;
import org.springframework.util.Assert;
import retrofit2.Retrofit;

/**
 * Utility type for creating retrofit service objects. In background, service creation is delegated
 * to registered {@link Retrofit} found in {@link RetrofitRegistry}.
 *
 * @author zakyalvan
 */
class RetrofitServiceFactory {
    private final RetrofitRegistry retrofitRegistry;

    RetrofitServiceFactory(RetrofitRegistry retrofitRegistry) {
        Assert.notNull(retrofitRegistry, "Retrofit registry must be provided");
        this.retrofitRegistry = retrofitRegistry;
    }

    public <T> T createInstance(Class<T> serviceType, String retrofitName) {
        Retrofit retrofit = retrofitRegistry.get(retrofitName)
                .orElseThrow(() -> new InvalidRetrofitNameException(retrofitName));

        return retrofit.create(serviceType);
    }
}
