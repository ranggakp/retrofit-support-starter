package com.tiket.tix.common.spring.retrofit.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.tiket.tix.common.spring.retrofit.annotation.RetrofitService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import retrofit.converter.java8.Java8OptionalConverterFactory;
import retrofit2.Retrofit;
import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * An {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration} for auto-configuring
 * {@link Retrofit} client instance. Client definition is detected by scanned for interface marked
 * with {@link RetrofitService}.
 *
 * @author zakyalvan
 */
@Configuration
@AutoConfigureAfter({ValidationAutoConfiguration.class, JacksonAutoConfiguration.class})
@ConditionalOnClass(Retrofit.class)
@EnableConfigurationProperties(RetrofitProperties.class)
@Import(RetrofitRegistryConfiguration.class)
public class RetrofitServiceAutoConfiguration {

    /**
     * Configure {@link JacksonConverterFactory}.
     */
    @Configuration
    @ConditionalOnClass({ObjectMapper.class, JacksonConverterFactory.class})
    public static class JacksonConversionConfiguration {
        @Bean
        @ConditionalOnMissingBean
        ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper;
        }

        @Bean
        @ConditionalOnMissingBean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        JacksonConverterFactory jacksonConverters(ObjectMapper objectMapper) {
            return JacksonConverterFactory.create(objectMapper);
        }
    }

    @Configuration
    @ConditionalOnJava(ConditionalOnJava.JavaVersion.EIGHT)
    @ConditionalOnClass(Java8OptionalConverterFactory.class)
    static class Java8OptionalConversionConfiguration {
        @Bean
        @ConditionalOnMissingBean
        Java8OptionalConverterFactory java8OptionalConverterFactory() {
            return Java8OptionalConverterFactory.create();
        }
    }

    /**
     * Configure Java8 call adapter retrofit, which enable us to return {@link java.util.concurrent.CompletableFuture}
     * from our retrofit service interface.
     */
    @Configuration
    @ConditionalOnJava(ConditionalOnJava.JavaVersion.EIGHT)
    @ConditionalOnClass(Java8CallAdapterFactory.class)
    static class Java8CallAdapterConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @Order(Ordered.HIGHEST_PRECEDENCE + 10)
        Java8CallAdapterFactory java8CallAdapterFactory() {
            return Java8CallAdapterFactory.create();
        }
    }

    /**
     * Configure guava call adapter retrofit which enable us to return {@link ListenableFuture}
     * from our retrofit2 service interface.
     */
    @Configuration
    @ConditionalOnClass({GuavaCallAdapterFactory.class, ListenableFuture.class})
    static class GuavaCallAdapterConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @Order(Ordered.HIGHEST_PRECEDENCE + 20)
        GuavaCallAdapterFactory guavaCallAdapterFactory() {
            return GuavaCallAdapterFactory.create();
        }
    }
}
