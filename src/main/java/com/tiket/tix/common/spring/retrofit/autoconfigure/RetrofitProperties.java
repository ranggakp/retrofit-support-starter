package com.tiket.tix.common.spring.retrofit.autoconfigure;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zakyalvan
 */
@Data
@Validated
@ConfigurationProperties(prefix = "tiket.retrofit")
public class RetrofitProperties implements Serializable {
    /**
     * Default base url for retrofit client.
     */
    private URI defaultUrl;

    /**
     * Default http connection settings.
     */
    @Valid
    @NestedConfigurationProperty
    private final ConnectionProperties connection = new ConnectionProperties();

    /**
     * Map of custom {@link retrofit2.Retrofit} specifications.
     */
    private final Map<String, CustomRetrofit> factories = new HashMap<>();

    /**
     * Custom retrofit object.
     */
    @Data
    public static class CustomRetrofit implements Serializable {
        @NotNull
        private URI baseUrl;

        private ConnectionProperties connection = new ConnectionProperties();
    }

    /**
     * Http connection settings.
     */
    @Data
    public static class ConnectionProperties implements Serializable {
        /**
         * Http connect timeout, in millis.
         */
        private int connectTimeout = 30_000;

        /**
         * Http read timeout, in millis.
         */
        private int readTimeout = 30_000;

        /**
         * Http write timeout, in millis.
         */
        private int writeTimeout = 30_000;

        /**
         * Flag whether to debug http request and response header and body/payload.
         */
        private boolean debugRequest = false;

        /**
         * Whether RxJava call adapter using scheduler or not, default is I/O scheduler.
         */
        private boolean asyncRequest = true;

        /**
         * Reactive {@link io.reactivex.Scheduler} configuration.
         */
        private final ReactiveScheduler scheduler = new ReactiveScheduler();
    }

    /**
     * RxJava {@link io.reactivex.Scheduler} settings.
     */
    @Data
    public static class ReactiveScheduler implements Serializable {
        /**
         * Flag whether to use custom scheduler.
         */
        private boolean overrideDefault = true;

        /**
         * Number of threads core poll size.
         */
        @Min(1)
        @NotNull
        private Integer corePollSize = 100;

        /**
         * Scheduler thread name prefix.
         */
        @NotBlank
        private String threadNamePrefix = "CustomScheduler";
    }
}
