package com.tiket.tix.common.spring.retrofit.autoconfigure;

import com.tiket.tix.common.spring.retrofit.registry.DefaultRetrofitRegistry;
import com.tiket.tix.common.spring.retrofit.registry.RetrofitRegistry;
import com.tiket.tix.common.spring.retrofit.support.HttpLoggingCategory;
import io.reactivex.internal.schedulers.RxThreadFactory;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Configure {@link retrofit2.Retrofit} objects based on {@link RetrofitProperties}.
 */
@Configuration
class RetrofitRegistryConfiguration implements ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetrofitRegistryConfiguration.class);

    private final RetrofitProperties retrofitProperties;

    private List<Interceptor> clientInterceptors = new ArrayList<>();

    private List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();

    private List<Converter.Factory> converterFactories = new ArrayList<>();

    private ApplicationContext applicationContext;

    public RetrofitRegistryConfiguration(RetrofitProperties retrofitProperties) {
        Assert.notNull(retrofitProperties, "Retrofit properties object must be provided");
        this.retrofitProperties = retrofitProperties;
    }

    @Autowired(required = false)
    public void setClientInterceptors(List<Interceptor> clientInterceptors) {
        this.clientInterceptors.addAll(clientInterceptors);
        AnnotationAwareOrderComparator.sort(this.clientInterceptors);
    }

    @Autowired(required = false)
    public void setCallAdapterFactories(List<CallAdapter.Factory> callAdapterFactories) {
        this.callAdapterFactories.addAll(callAdapterFactories);
        AnnotationAwareOrderComparator.sort(this.callAdapterFactories);
    }

    @Autowired(required = false)
    public void setConverterFactories(List<Converter.Factory> converterFactories) {
        this.converterFactories.addAll(converterFactories);
        AnnotationAwareOrderComparator.sort(this.converterFactories);
    }

    /**
     * Create retrofit service retrofit registry, {@link RetrofitRegistry}.
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    DefaultRetrofitRegistry retrofitRegistry() {
        LOGGER.debug("Create Retrofit object registry");

        DefaultRetrofitRegistry retrofitRegistry = new DefaultRetrofitRegistry();

        Retrofit defaultRetrofit = createDefaultRetrofit();
        if (defaultRetrofit != null) {
            retrofitRegistry.register(RetrofitRegistry.DEFAULT_RETROFIT, defaultRetrofit);
        }

        retrofitProperties.getFactories().forEach((customName, customSpecs) -> {
            Retrofit customRetrofit = createCustomRetrofit(customSpecs);
            if (customRetrofit != null) {
                retrofitRegistry.register(customName, customRetrofit);
            }
        });

        // Register custom Retrofit bean created manually by user, so that can be referred from @RetrofitService
        applicationContext.getBeansOfType(Retrofit.class).forEach((beanName, userRetrofit) -> {
            retrofitRegistry.register(beanName, userRetrofit);
        });

        return retrofitRegistry;
    }

    /**
     * Create default {@link Retrofit} object. Create if {@link RetrofitProperties#defaultUrl} provided,
     * otherwise simply return null.
     *
     * @return
     */
    private Retrofit createDefaultRetrofit() {
        if (retrofitProperties.getDefaultUrl() == null) {
            return null;
        }

        final Retrofit.Builder defaultBuilder = new Retrofit.Builder();
        defaultBuilder.validateEagerly(true);

        Call.Factory callFactory = createCallFactory(retrofitProperties.getConnection());
        defaultBuilder.callFactory(callFactory);

        if(retrofitProperties.getConnection().isAsyncRequest()) {

            if (retrofitProperties.getConnection().getScheduler().isOverrideDefault()) {
                int corePollSize = retrofitProperties.getConnection().getScheduler().getCorePollSize();
                String threadNamePrefix = retrofitProperties.getConnection().getScheduler().getThreadNamePrefix();
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(corePollSize, new RxThreadFactory(threadNamePrefix));

                defaultBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.from(executor)));
            } else {
                defaultBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()));
            }
        }
        else {
            defaultBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        }

        callAdapterFactories.forEach(defaultBuilder::addCallAdapterFactory);
        converterFactories.forEach(defaultBuilder::addConverterFactory);

        HttpUrl defaultUrl = HttpUrl.get(normalizeBaseUrl(retrofitProperties.getDefaultUrl()));
        return defaultBuilder.baseUrl(defaultUrl).build();
    }

    /**
     * Create custom {@link Retrofit} instance.
     *
     * @param customSpecs
     * @return
     */
    private Retrofit createCustomRetrofit(RetrofitProperties.CustomRetrofit customSpecs) {
        Retrofit.Builder customBuilder = new Retrofit.Builder();
        customBuilder.validateEagerly(true);

        Call.Factory callFactory = createCallFactory(customSpecs.getConnection());
        customBuilder.callFactory(callFactory);

        if(customSpecs.getConnection().isAsyncRequest()) {
            if (customSpecs.getConnection().getScheduler().isOverrideDefault()) {
                int corePollSize = customSpecs.getConnection().getScheduler().getCorePollSize();
                String threadNamePrefix = customSpecs.getConnection().getScheduler().getThreadNamePrefix();
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(corePollSize, new RxThreadFactory(threadNamePrefix));

                customBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.from(executor)));
            } else {
                customBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()));
            }
        }
        else {
            customBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        }

        callAdapterFactories.forEach(customBuilder::addCallAdapterFactory);
        converterFactories.forEach(customBuilder::addConverterFactory);

        HttpUrl customUrl = HttpUrl.get(normalizeBaseUrl(customSpecs.getBaseUrl()));
        return customBuilder.baseUrl(customUrl).build();
    }

    /**
     * Create OkHttp {@link Call.Factory} to be used on building {@link Retrofit} object.
     *
     * @return
     */
    private Call.Factory createCallFactory(RetrofitProperties.ConnectionProperties connection) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        clientInterceptors.forEach(clientBuilder::addInterceptor);

        if(connection.getConnectTimeout() > 100) {
            clientBuilder.connectTimeout(connection.getConnectTimeout(), TimeUnit.MILLISECONDS);
        }
        if(connection.getReadTimeout() > 100) {
            clientBuilder.readTimeout(connection.getReadTimeout(), TimeUnit.MILLISECONDS);
        }
        if(connection.getWriteTimeout() > 100) {
            clientBuilder.writeTimeout(connection.getWriteTimeout(), TimeUnit.MILLISECONDS);
        }

        if(connection.isDebugRequest()) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> HttpLoggingCategory.LOGGER.debug(message))
                    .setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(interceptor);
        }

        return clientBuilder.build();
    }

    /**
     * Normalize {@link Retrofit} base url, i.e. adding '/' in the end if not provided.
     *
     * @param baseUrl
     * @return
     */
    private URI normalizeBaseUrl(URI baseUrl) {
        try {
            if (!baseUrl.toString().endsWith("/")) {
                return URI.create(baseUrl.toString().concat("/"));
            } else {
                return baseUrl;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not normalize base url of retrofit instance");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}