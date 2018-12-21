package com.tiket.tix.common.spring.retrofit.annotation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.tiket.tix.common.spring.retrofit.support.MockHttpServerRule;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import lombok.Builder;
import lombok.Getter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

/**
 * @author zakyalvan
 */
@SpringBootTest(classes = BaseUrlRetrofitServiceTests.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "logging.level.com.tiket.tix.common.spring.retrofit=debug",

                "tiket.retrofit.default-url=http://localhost:10080/",
                "tiket.retrofit.connection.debug-request=true",
                "tiket.retrofit.connection.async-request=true",
                "tiket.retrofit.connection.scheduler.override-default=true",
                "tiket.retrofit.connection.scheduler.thread-name-prefix=CalcScheduler",
                "tiket.retrofit.connection.scheduler.core-poll-size=50",
                "tiket.retrofit.factories.calculate.base-url=http://localhost:10080/",
                "tiket.retrofit.factories.custom.base-url=http://localhost:10090/"
        })
@RunWith(SpringRunner.class)
public class BaseUrlRetrofitServiceTests {
    @Rule
    public MockHttpServerRule serverRule = new MockHttpServerRule(first -> first.defaultServer().portNumber(10080));

    public final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DefaultClient defaultClient;

    @Autowired
    private CustomClient customClient;

    @Test
    public void givenClientInterface_whenRunning_mustRegisteredAsBean() {
        serverRule.mockServer().expect().post().withPath("/ping").andReply(200, request -> {
            try {
                Ping ping = objectMapper.readValue(request.getBody().inputStream(), Ping.class);
                return Pong.reply(ping);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).once();

        TestObserver<Pong> firstObserver = new TestObserver<>();

        final Ping ping = Ping.create();
        defaultClient.ping(ping).subscribe(firstObserver);
        await().timeout(10, TimeUnit.SECONDS).until(firstObserver::valueCount, is(1));
        firstObserver.assertSubscribed().assertComplete().assertValue(pong -> pong.getId().equals(ping.getId()));
    }


    @RetrofitService
    public interface DefaultClient {
        @POST("/ping")
        Single<Pong> ping(@Body Ping data);
    }

    @RetrofitService(retrofit = "custom")
    public interface CustomClient {
        @POST("/ping")
        Single<String> ping();
    }


    @Getter
    @JsonDeserialize(builder = Ping.PingBuilder.class)
    @SuppressWarnings("serial")
    public static class Ping {
        private final UUID id;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final Date timestamp;

        protected Ping() {
            this(UUID.randomUUID(), new Date());
        }

        @Builder
        protected Ping(UUID id, Date timestamp) {
            this.id = id;
            this.timestamp = timestamp;
        }

        public static Ping create() {
            return new Ping();
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class PingBuilder {

        }
    }

    @Getter
    @JsonDeserialize(builder = Pong.PongBuilder.class)
    @SuppressWarnings("serial")
    public static class Pong implements Serializable {
        public final UUID id;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        public final Date timestamp;

        protected Pong(UUID id) {
            this(id, new Date());
        }
        @Builder
        protected Pong(UUID id, Date timestamp) {
            this.id = id;
            this.timestamp = timestamp;
        }

        public static Pong reply(Ping ping) {
            return new Pong(ping.getId());
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class PongBuilder {

        }
    }

    @RetrofitServiceScan
    @SpringBootApplication
    public static class TestApplication {}
}
