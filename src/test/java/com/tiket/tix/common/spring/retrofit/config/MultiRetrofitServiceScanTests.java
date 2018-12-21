package com.tiket.tix.common.spring.retrofit.config;

import com.tiket.tix.common.spring.retrofit.annotation.RetrofitServiceScan;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test for multiple {@link RetrofitServiceScan}
 *
 * @author zakyalvan
 */
@SpringBootTest(classes = {MultiRetrofitServiceScanTests.TestApplication.class, MultiRetrofitServiceScanTests.EnableScannerConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "logging.level.com.tiket.tix.common.spring.retrofit=debug",

                "tiket.retrofit.default-url=http://localhost:10080/",
                "tiket.retrofit.factories.calculate.base-url=http://localhost:10080/",
                "tiket.retrofit.factories.custom.base-url=http://localhost:10090/"
        })
@RunWith(SpringRunner.class)
public class MultiRetrofitServiceScanTests {
    @Test
    public void givenMultiServiceScan_whenStartup_mustPreventDoubleServiceRegistration() {

    }

    @RetrofitServiceScan
    @SpringBootApplication
    static class TestApplication {

    }

    @Configuration
    @RetrofitServiceScan
    static class EnableScannerConfiguration {
        @Bean
        String nameBean() {
            return "Muhammad Zaky Alvan";
        }
    }
}
