package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.MockTools;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestPostRefreshSession extends BaseIntegrationTest {

    private static final String POST_REFRESH = "/onboarding/auth/refresh";

    @Test
    void refreshSession_validToken_returnsSessionAndCallsKeycloakWithRefreshGrant() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("refresh_token", "old-refresh-token-123");
        MockTools.addExpectation("keycloak/post-token_refresh", Map.of(), Map.of(
                "payload", Map.of("access_token", "new-access-token", "refresh_token", "new-refresh-token")));

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_REFRESH)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("access_token")).isEqualTo("new-access-token");
        assertThat(response.jsonPath().getInt("expires_in")).isEqualTo(300);
        assertThat(response.jsonPath().getString("refresh_token")).isEqualTo("new-refresh-token");
        assertThat(response.jsonPath().getInt("refresh_expires_in")).isEqualTo(1800);
        assertThat(response.jsonPath().getString("token_type")).isEqualTo("Bearer");

        List<Map<String, Object>> requests = MockTools.retrieveRequests(
                "POST", "/realms/pulse-pass/protocol/openid-connect/token");
        assertThat(requests).hasSize(1);
        Map<?, ?> sentBody = (Map<?, ?>) requests.get(0).get("body");
        assertThat(sentBody.get("contentType")).isEqualTo("application/x-www-form-urlencoded");
        assertThat(((String) sentBody.get("string")).split("&"))
                .contains("grant_type=refresh_token", "refresh_token=old-refresh-token-123");
    }

    @Test
    void refreshSession_expiredToken_returnsUnauthorizedInPortuguese() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("refresh_token", "expired-refresh-token");
        MockTools.addExpectation("keycloak/post-token_refresh_invalid_grant");

        Response response = given()
                    .header("Accept-Language", "pt-BR")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_REFRESH)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(response.jsonPath().getString("message")).isEqualTo("Sua sessão expirou. Entre novamente.");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void refreshSession_expiredToken_returnsUnauthorizedInEnglish() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("refresh_token", "expired-refresh-token");
        MockTools.addExpectation("keycloak/post-token_refresh_invalid_grant");

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_REFRESH)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("message"))
                .isEqualTo("Your session has expired. Please sign in again.");
    }

    @Test
    void refreshSession_missingToken_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of();

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_REFRESH)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("refresh_token");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("REQUIRED");
    }

    @Test
    void refreshSession_keycloakFails_returnsUnprocessableContent() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("refresh_token", "refresh-token-abc");
        MockTools.addExpectation("keycloak/post-token_server_error");

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_REFRESH)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00008");
    }
}
