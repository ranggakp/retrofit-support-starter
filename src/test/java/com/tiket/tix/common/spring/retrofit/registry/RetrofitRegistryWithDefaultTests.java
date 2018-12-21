package com.tiket.tix.common.spring.retrofit.registry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Retrofit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test for default implementation of {@link RetrofitRegistry}.
 *
 * @author zakyalvan
 */
@SpringBootTest(classes = RetrofitRegistryWithDefaultTests.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"tiket.retrofit.default-url=http://localhost:8080/default",
                "tiket.retrofit.factories.custom.base-url=http://localhost:9090/custom/"
        })
@RunWith(SpringRunner.class)
public class RetrofitRegistryWithDefaultTests {
    @Autowired
    private RetrofitRegistry retrofitRegistry;

    /**
     * Please remember, base url of {@link Retrofit} is normalized to add "/" in the end.
     */
    @Test
    public void givenRetrofitEndpointSetting_whenRunning_thenRegistryMustBePopulated() {
        Retrofit defaultInstance = retrofitRegistry.get(RetrofitRegistry.DEFAULT_RETROFIT).get();
        assertThat(defaultInstance, is(notNullValue()));
        assertThat(defaultInstance.baseUrl().toString(), equalTo("http://localhost:8080/default/"));

        Retrofit customInstance = retrofitRegistry.get("custom").get();
        assertThat(customInstance, is(notNullValue()));
        assertThat(customInstance.baseUrl().toString(), equalTo("http://localhost:9090/custom/"));

        assertThat(retrofitRegistry.get("other").orElse(null), is(nullValue()));

        Retrofit anotherInstance = retrofitRegistry.get("another").get();
        assertThat(anotherInstance, is(notNullValue()));
        assertThat(anotherInstance.baseUrl().toString(), equalTo("http://localhost:3333/"));
    }

    @SpringBootApplication
    public static class TestApplication {
        @Bean("another")
        Retrofit otherRetrofit() {
            return new Retrofit.Builder()
                    .validateEagerly(true)
                    .baseUrl("http://localhost:3333/")
                    .build();
        }
    }
}
