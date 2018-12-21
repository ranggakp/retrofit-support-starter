package com.tiket.tix.common.spring.retrofit.registry;

import org.springframework.validation.annotation.Validated;
import retrofit2.Retrofit;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zakyalvan
 */
@Validated
public class DefaultRetrofitRegistry implements MutableRetrofitRegistry {
    private final Map<String, Retrofit> instances = new ConcurrentHashMap<>();

    @Override
    public boolean contains(String name) {
        return instances.containsKey(name);
    }

    @Override
    public RetrofitRegistry register(String name, Retrofit retrofit) {
        instances.put(name, retrofit);
        return this;
    }

    @Override
    public RetrofitRegistry unregister(String name) {
        instances.remove(name);
        return this;
    }

    @Override
    public Optional<Retrofit> get(String name) {
        return Optional.ofNullable(instances.get(name));
    }
}
