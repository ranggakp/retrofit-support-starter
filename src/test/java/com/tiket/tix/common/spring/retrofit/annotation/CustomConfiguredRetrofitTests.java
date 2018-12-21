package com.tiket.tix.common.spring.retrofit.annotation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.tiket.tix.common.spring.retrofit.support.HttpLoggingCategory;
import com.tiket.tix.common.spring.retrofit.support.MockHttpServerRule;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import lombok.Builder;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author zakyalvan
 */
@SpringBootTest(classes = CustomConfiguredRetrofitTests.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "logging.level.com.tiket.tix.common.spring.retrofit=debug",

                "tiket.retrofit.default-url=http://localhost:10080/",
                "tiket.retrofit.factories.custom.base-url=http://localhost:9999/custom",
                "tiket.retrofit.factories.calculate.connection.debug-request=true",
                "tiket.retrofit.factories.calculate.connection.scheduler.thread-name-prefix=CalcScheduler",
                "tiket.retrofit.factories.calculate.connection.scheduler.core-poll-size=50",
                "tiket.retrofit.factories.calculate.base-url=http://localhost:10090/"
        })
@RunWith(SpringRunner.class)
public class CustomConfiguredRetrofitTests {
    @Rule
    public MockHttpServerRule serverRule = new MockHttpServerRule(first -> first.portNumber(19091).defaultServer());

    @Autowired
    private CalculatorClient calculatorClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenUserDefinedRetrofit_whenRunning_thenOverrideConfiguredInSettings() {

        serverRule.mockServer().expect().get().withPath("/test/calculate/add?first=5&second=12").andReply(200, request -> {
            try {
                int first = Integer.parseInt(request.getRequestUrl().queryParameter("first"));
                int second = Integer.parseInt(request.getRequestUrl().queryParameter("second"));
                return objectMapper.writeValueAsString(AddResult.builder().first(first).second(second).build());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).once();

        TestObserver<AddResult> testObserver = new TestObserver<>();
        calculatorClient.add(5, 12).subscribe(testObserver);
        Awaitility.await().timeout(2, TimeUnit.SECONDS).until(testObserver::valueCount, Matchers.is(1));
        testObserver.assertSubscribed().assertComplete().assertValue(addResult -> addResult.getResult() == 17);
    }

    @RetrofitService(retrofit = "calculate")
    public interface CalculatorClient {
        @GET("calculate/add")
        Single<AddResult> add(@Query("first") int firstNumber, @Query("second") int secondNumber);
    }

    @Getter
    @JsonDeserialize(builder = AddResult.AddResultBuilder.class)
    public static class AddResult {
        private int first;
        private int second;
        private int result;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private Date timestamp;

        @Builder
        protected AddResult(int first, int second, Date timestamp) {
            this.first = first;
            this.second = second;
            this.result = first + second;

            if(timestamp == null) {
                this.timestamp = new Date();
            }
            else {
                this.timestamp = timestamp;
            }
        }

        @JsonPOJOBuilder(withPrefix = "")
        @JsonIgnoreProperties("result")
        static class AddResultBuilder {

        }
    }

    @RetrofitServiceScan
    @SpringBootApplication
    static class TestApplication {
        @Bean("calculate")
        Retrofit customRetrofit() {
            return new Retrofit.Builder()
                    .baseUrl("http://localhost:19091/test/")
                    .client(new OkHttpClient.Builder()
                            .addInterceptor(new HttpLoggingInterceptor(message -> HttpLoggingCategory.LOGGER.debug(message))
                                    .setLevel(HttpLoggingInterceptor.Level.BODY))
                            .build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        }
    }
}
