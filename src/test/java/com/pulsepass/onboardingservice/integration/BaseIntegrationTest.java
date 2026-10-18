package com.pulsepass.onboardingservice.integration;

import com.pulsepass.onboardingservice.common.MockTools;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseIntegrationTest {

    protected static String internalToken;

    private static String env(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = env("API_BASE_URL", "http://localhost");
        RestAssured.port = Integer.parseInt(env("API_PORT", "8081"));
        internalToken = env("INTERNAL_TOKEN", "pulse-pass-internal-token");
        MockTools.configure(
                env("API_BASE_URL", "http://localhost"),
                Integer.parseInt(env("MOCKSERVER_PORT", "1081"))
        );
    }
}
