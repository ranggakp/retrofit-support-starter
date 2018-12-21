package com.tiket.tix.common.spring.retrofit.config;

import lombok.Getter;
import org.springframework.core.NestedRuntimeException;

/**
 * @author zakyalvan
 */
@Getter
public class InvalidRetrofitNameException extends NestedRuntimeException {
    private final String retrofitName;

    public InvalidRetrofitNameException(String retrofitName) {
        super(String.format("No Retrofit object with identifier '%s' configured in registry, check your configuration", retrofitName));
        this.retrofitName = retrofitName;
    }
}
