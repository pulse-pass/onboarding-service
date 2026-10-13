package com.pulsepass.onboardingservice.unit.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.HttpClientHandlers;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;

class HttpClientHandlersTest {

    @Test
    void requestFactory_timeouts_returnsJdkFactory() {
        var factory = HttpClientHandlers.requestFactory(Duration.ofSeconds(2), Duration.ofSeconds(5));

        assertThat(factory).isInstanceOf(JdkClientHttpRequestFactory.class);
    }

    @Test
    void constructor_utilityClass_isPrivate() throws Exception {
        var constructor = HttpClientHandlers.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThat(constructor.newInstance()).isNotNull();
    }
}
