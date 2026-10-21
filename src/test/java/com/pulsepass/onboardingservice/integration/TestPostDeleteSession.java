package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.MockTools;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestPostDeleteSession extends BaseIntegrationTest {

    private static final String POST_LOGOUT = "/onboarding/auth/logout";

    @Test
    void deleteSession_validToken_returnsNoContentAndCallsKeycloakLogout() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("refresh_token", "refresh-token-to-revoke");
        MockTools.addExpectation("keycloak/post-logout");

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGOUT)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(response.body().asString()).isEmpty();

        List<Map<String, Object>> requests = MockTools.retrieveRequests(
                "POST", "/realms/pulse-pass/protocol/openid-connect/logout");
        assertThat(requests).hasSize(1);
        Map<?, ?> sentBody = (Map<?, ?>) requests.get(0).get("body");
        assertThat(sentBody.get("contentType")).isEqualTo("application/x-www-form-urlencoded");
        assertThat(((String) sentBody.get("string")).split("&"))
                .contains("refresh_token=refresh-token-to-revoke");
    }

    @Test
    void deleteSession_invalidToken_returnsUnauthorizedInPortuguese() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("refresh_token", "already-invalid-token");
        MockTools.addExpectation("keycloak/post-logout_invalid_grant");

        Response response = given()
                    .header("Accept-Language", "pt-BR")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGOUT)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(response.jsonPath().getString("message")).isEqualTo("Sua sessão expirou. Entre novamente.");
    }

    @Test
    void deleteSession_keycloakRejectsRequest_returnsUnprocessableContent() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("refresh_token", "refresh-token-abc");
        MockTools.addExpectation("keycloak/post-logout_invalid_request");

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGOUT)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00008");
    }

    @Test
    void deleteSession_blankToken_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("refresh_token", " ");

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGOUT)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("refresh_token");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("REQUIRED");
        assertThat(MockTools.retrieveRequests("POST", "/realms/pulse-pass/protocol/openid-connect/logout")).isEmpty();
    }

    @Test
    void deleteSession_keycloakFails_returnsUnprocessableContent() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("refresh_token", "refresh-token-abc");
        MockTools.addExpectation("keycloak/post-logout_server_error");

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGOUT)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00008");
    }
}
