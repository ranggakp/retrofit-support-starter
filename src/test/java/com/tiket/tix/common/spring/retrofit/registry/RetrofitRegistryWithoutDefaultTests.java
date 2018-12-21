package com.tiket.tix.common.spring.retrofit.registry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author zakyalvan
 */
@SpringBootTest(classes = RetrofitRegistryWithDefaultTests.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "tiket.retrofit.factories.custom.base-url=http://localhost:9090/custom/"
        })
@RunWith(SpringRunner.class)
public class RetrofitRegistryWithoutDefaultTests {
    @Autowired
    private RetrofitRegistry retrofitRegistry;

    @Test
    public void givenNoDefaultRetrofitConfigured_whenInquiryRegistryForDefault_thenNoInstanceFound() {
        assertThat(retrofitRegistry.contains(RetrofitRegistry.DEFAULT_RETROFIT), is(false));
    }
}
