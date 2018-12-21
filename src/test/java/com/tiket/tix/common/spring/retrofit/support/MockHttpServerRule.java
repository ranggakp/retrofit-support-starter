package com.tiket.tix.common.spring.retrofit.support;

import io.fabric8.mockwebserver.DefaultMockServer;
import io.fabric8.mockwebserver.dsl.MockServerExpectation;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Simple test rule for starting and stopping mock http/web server(s) for testing purpose.
 *
 * @author zakyalvan
 */
public class MockHttpServerRule implements TestRule {
    public static final String DEFAULT_SERVER_NAME = "default";

    private static final Logger LOGGER = LoggerFactory.getLogger(MockServerAdapter.class);

    private final MockServerAdapter defaultMock;

    private final Map<String, MockServerAdapter> mockServers = new HashMap<>();

    public MockHttpServerRule(Consumer<ServerAddress>... addresses) {
        if(ObjectUtils.isEmpty(addresses)) {
            LOGGER.trace("Create default http mock server, use random chosen port");
            defaultMock = MockServerAdapter.builder()
                    .mockServer(new DefaultMockServer())
                    .boundPort(0)
                    .build();

            mockServers.put(DEFAULT_SERVER_NAME, defaultMock);
        }
        else {
            for(Consumer<ServerAddress> customizer : addresses) {
                ServerAddress address = new ServerAddress();
                customizer.accept(address);
                mockServers.put(address.identifier(), MockServerAdapter.builder()
                        .mockServer(new DefaultMockServer())
                        .boundPort(address.portNumber())
                        .build());
            }

            if(!mockServers.containsKey(DEFAULT_SERVER_NAME)) {
                Consumer<ServerAddress> defaultCustomizer = addresses[0];
                ServerAddress defaultAddress = new ServerAddress();
                defaultCustomizer.accept(defaultAddress);
                defaultMock = mockServers.get(defaultAddress.identifier());
            }
            else {
                defaultMock = mockServers.get(DEFAULT_SERVER_NAME);
            }
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    mockServers.forEach((port, adapter) -> adapter.start());
                    base.evaluate();
                }
                finally {
                    mockServers.forEach((port, adapter) -> adapter.shutdown());
                }
            }
        };
    }

    /**
     * Retrieve default mock server. Default if named {@link #DEFAULT_SERVER_NAME}, or the first one defined.
     *
     * @return
     */
    public MockServerAdapter mockServer() {
        return defaultMock;
    }

    /**
     * Retrieve mock server bound to given port.
     *
     * @param identifier
     * @return
     */
    public Optional<MockServerAdapter> mockServer(String identifier) {
        if(mockServers.containsKey(identifier)) {
            return Optional.of(mockServers.get(identifier));
        }

        LOGGER.error("No mock http server found with key {}", identifier);
        return Optional.empty();
    }

    @Data
    @Accessors(fluent = true, chain = true, prefix = "")
    public static class ServerAddress {
        private String identifier = "";
        private String addressScheme = "http";
        private String hostName = "localhost";
        private int portNumber = 0;
        private String basePath = "/";

        public ServerAddress defaultServer() {
            this.identifier = MockHttpServerRule.DEFAULT_SERVER_NAME;
            return this;
        }

        public ServerAddress secureScheme(boolean secureScheme) {
            if(secureScheme) {
                this.addressScheme = "https";
            }
            else {
                this.addressScheme = "http";
            }
            return this;
        }
    }

    /**
     * Adapter for {@link DefaultMockServer}.
     */
    public static class MockServerAdapter {
        private DefaultMockServer mockServer;
        private int boundPort;

        private AtomicBoolean running = new AtomicBoolean(false);

        @Builder
        protected MockServerAdapter(DefaultMockServer mockServer, ServerAddress serverAddress, int boundPort) {
            Assert.notNull(mockServer, "Mock server instance must be provided");

            this.mockServer = mockServer;
            this.boundPort = boundPort;
        }

        public void start() {
            if(running.compareAndSet(false, true)) {

            }

            if(boundPort > 0) {
                mockServer.start(boundPort);
            }
            else {
                mockServer.start();
            }
        }

        public void shutdown() {
            mockServer.shutdown();
        }

        public String url(String path) {
            return mockServer.url(path);
        }

        public MockServerExpectation expect() {
            return mockServer.expect();
        }
    }
}
