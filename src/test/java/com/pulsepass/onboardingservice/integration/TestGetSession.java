package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.CpfGenerator;
import com.pulsepass.onboardingservice.common.MockTools;
import com.pulsepass.onboardingservice.common.SetupTools;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class TestGetSession extends BaseIntegrationTest {

    private static final String GET_SESSION = "/onboarding/auth/me";

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    @SuppressWarnings("unchecked")
    void getSession_organizerHeaders_returnsPersonRolesAndLegalPerson() {
        String cpf = CpfGenerator.generateCpf();
        String legalPersonKey = SetupTools.createLegalPerson(cpf).getString("person_key");
        List<Map<String, Object>> users = MockTools.retrieveRequests("POST", "/admin/realms/pulse-pass/users");
        Map<String, Object> userRequestBody = (Map<String, Object>) users.get(0).get("body");
        JsonPath user = new JsonPath(mapper.writeValueAsString(userRequestBody.get("json")));
        String personKey = user.getString("attributes.person_key[0]");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-key", personKey)
                    .header("selected-agent-key", legalPersonKey)
                    .header("selected-user-roles", "offline_access,organizer")
                .when()
                    .get(GET_SESSION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo(personKey);
        assertThat(response.jsonPath().getString("name")).isEqualTo("Bianca Torres");
        assertThat(response.jsonPath().getString("document_number")).isEqualTo(cpf);
        assertThat(response.jsonPath().getList("roles")).containsExactly("offline_access", "organizer");
        assertThat(response.jsonPath().getString("legal_person.legal_person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("legal_person.name")).isEqualTo("Lenha Produções Artísticas Ltda");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSession_withoutAgentKey_returnsNullLegalPerson() {
        String cpf = CpfGenerator.generateCpf();
        SetupTools.createLegalPerson(cpf);
        List<Map<String, Object>> users = MockTools.retrieveRequests("POST", "/admin/realms/pulse-pass/users");
        Map<String, Object> userRequestBody = (Map<String, Object>) users.get(0).get("body");
        JsonPath user = new JsonPath(mapper.writeValueAsString(userRequestBody.get("json")));
        String personKey = user.getString("attributes.person_key[0]");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-key", personKey)
                    .header("selected-user-roles", "admin")
                .when()
                    .get(GET_SESSION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo(personKey);
        assertThat(response.jsonPath().getList("roles")).containsExactly("admin");
        assertThat(response.asString()).contains("\"legal_person\":null");
    }

    @Test
    void getSession_unknownPerson_returnsUnauthorizedInPortuguese() {
        MockTools.clearMocker();

        Response response = given()
                    .header("Accept-Language", "pt-BR")
                    .header("selected-user-key", UUID.randomUUID().toString())
                    .header("selected-user-roles", "organizer")
                .when()
                    .get(GET_SESSION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(response.jsonPath().getString("message")).isEqualTo("Sua sessão expirou. Entre novamente.");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSession_unknownLegalPerson_returnsUnauthorizedInEnglish() {
        SetupTools.createLegalPerson(CpfGenerator.generateCpf());
        List<Map<String, Object>> users = MockTools.retrieveRequests("POST", "/admin/realms/pulse-pass/users");
        Map<String, Object> userRequestBody = (Map<String, Object>) users.get(0).get("body");
        JsonPath user = new JsonPath(mapper.writeValueAsString(userRequestBody.get("json")));
        String personKey = user.getString("attributes.person_key[0]");
        MockTools.clearMocker();

        Response response = given()
                    .header("Accept-Language", "en")
                    .header("selected-user-key", personKey)
                    .header("selected-agent-key", UUID.randomUUID().toString())
                    .header("selected-user-roles", "organizer")
                .when()
                    .get(GET_SESSION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(response.jsonPath().getString("message"))
                .isEqualTo("Your session has expired. Please sign in again.");
    }

    @Test
    void getSession_missingUserKeyHeader_returnsBadRequest() {
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "organizer")
                .when()
                    .get(GET_SESSION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("selected-user-key");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("REQUIRED");
    }

    @Test
    void getSession_missingRolesHeader_returnsBadRequest() {
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-key", UUID.randomUUID().toString())
                .when()
                    .get(GET_SESSION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("selected-user-roles");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("REQUIRED");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSession_agentKeyOfAnotherCompany_returnsUnauthorized() {
        SetupTools.createLegalPerson(CpfGenerator.generateCpf());
        List<Map<String, Object>> users = MockTools.retrieveRequests("POST", "/admin/realms/pulse-pass/users");
        Map<String, Object> userRequestBody = (Map<String, Object>) users.get(0).get("body");
        JsonPath user = new JsonPath(mapper.writeValueAsString(userRequestBody.get("json")));
        String personKey = user.getString("attributes.person_key[0]");
        String otherLegalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf())
                .getString("person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-key", personKey)
                    .header("selected-agent-key", otherLegalPersonKey)
                    .header("selected-user-roles", "organizer")
                .when()
                    .get(GET_SESSION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }
}
