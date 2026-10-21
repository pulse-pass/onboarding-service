package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.CpfGenerator;
import com.pulsepass.onboardingservice.common.MockTools;
import com.pulsepass.onboardingservice.common.SetupTools;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestPostCreateSession extends BaseIntegrationTest {

    private static final String POST_LOGIN = "/onboarding/auth/login";

    @Test
    void createSession_validCredentials_returnsSessionAndCallsKeycloakWithPasswordGrant() {
        String cpf = CpfGenerator.generateCpf();
        SetupTools.createLegalPerson(cpf);
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("document_number", cpf, "password", "12345678");
        MockTools.addExpectation("keycloak/post-token_password", Map.of(), Map.of(
                "payload", Map.of("access_token", "access-token-abc", "refresh_token", "refresh-token-xyz")));

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGIN)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("access_token")).isEqualTo("access-token-abc");
        assertThat(response.jsonPath().getInt("expires_in")).isEqualTo(300);
        assertThat(response.jsonPath().getString("refresh_token")).isEqualTo("refresh-token-xyz");
        assertThat(response.jsonPath().getInt("refresh_expires_in")).isEqualTo(1800);
        assertThat(response.jsonPath().getString("token_type")).isEqualTo("Bearer");

        List<Map<String, Object>> requests = MockTools.retrieveRequests(
                "POST", "/realms/pulse-pass/protocol/openid-connect/token");
        assertThat(requests).hasSize(1);
        Map<?, ?> sentBody = (Map<?, ?>) requests.get(0).get("body");
        assertThat(sentBody.get("contentType")).isEqualTo("application/x-www-form-urlencoded");
        assertThat(((String) sentBody.get("string")).split("&"))
                .contains("grant_type=password", "username=" + cpf);
    }

    @Test
    void createSession_wrongCredentials_returnsUnauthorizedInPortuguese() {
        String cpf = CpfGenerator.generateCpf();
        SetupTools.createLegalPerson(cpf);
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("document_number", cpf, "password", "wrong-pass");
        MockTools.addExpectation("keycloak/post-token_invalid_grant");

        Response response = given()
                    .header("Accept-Language", "pt-BR")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGIN)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(response.jsonPath().getString("message")).isEqualTo("CPF ou senha inválidos.");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createSession_wrongCredentials_returnsUnauthorizedInEnglish() {
        String cpf = CpfGenerator.generateCpf();
        SetupTools.createLegalPerson(cpf);
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("document_number", cpf, "password", "wrong-pass");
        MockTools.addExpectation("keycloak/post-token_invalid_grant");

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGIN)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(response.jsonPath().getString("message")).isEqualTo("Invalid CPF or password.");
    }

    @Test
    void createSession_unknownCpf_returnsUnauthorizedWithoutCallingKeycloak() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("document_number", CpfGenerator.generateCpf(), "password", "12345678");
        MockTools.addExpectation("keycloak/post-token_password");

        Response response = given()
                    .header("Accept-Language", "pt-BR")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGIN)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(response.jsonPath().getString("message")).isEqualTo("CPF ou senha inválidos.");
        assertThat(MockTools.retrieveRequests("POST", "/realms/pulse-pass/protocol/openid-connect/token")).isEmpty();
    }

    @Test
    void createSession_documentWithTenDigits_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("document_number", "1234567890", "password", "12345678");

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGIN)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("document_number");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("INVALID_FORMAT");
        assertThat(MockTools.retrieveRequests("POST", "/realms/pulse-pass/protocol/openid-connect/token")).isEmpty();
    }

    @Test
    void createSession_missingPassword_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("document_number", CpfGenerator.generateCpf());

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGIN)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("password");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("REQUIRED");
    }

    @Test
    void createSession_keycloakFails_returnsUnprocessableContent() {
        String cpf = CpfGenerator.generateCpf();
        SetupTools.createLegalPerson(cpf);
        MockTools.clearMocker();
        Map<String, Object> payload = Map.of("document_number", cpf, "password", "12345678");
        MockTools.addExpectation("keycloak/post-token_server_error");

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LOGIN)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00008");
        assertThat(response.jsonPath().getString("message"))
                .isEqualTo("We could not complete your request. Please try again later.");
    }
}
