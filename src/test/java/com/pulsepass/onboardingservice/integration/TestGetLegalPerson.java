package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.CpfGenerator;
import com.pulsepass.onboardingservice.common.MockTools;
import com.pulsepass.onboardingservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestGetLegalPerson extends BaseIntegrationTest {

    private static final String GET_LEGAL_PERSON = "/onboarding/legal_persons/{legal_person_key}";

    @Test
    void getLegalPerson_ownKey_returnsItWithOwner() {
        String ownerCpf = CpfGenerator.generateCpf();
        String legalPersonKey = SetupTools.createLegalPerson(ownerCpf).getString("person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-agent-key", legalPersonKey)
                .when()
                    .get(GET_LEGAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("legal_person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("name")).isEqualTo("Lenha Produções Artísticas Ltda");
        assertThat(response.jsonPath().getString("document_number")).hasSize(14);
        assertThat(response.jsonPath().getString("email")).endsWith("@lenha.com.br");
        assertThat(response.jsonPath().getString("created_at")).endsWith("Z");
        assertThat(response.jsonPath().getString("owner.person_key")).isNotBlank();
        assertThat(response.jsonPath().getString("owner.name")).isEqualTo("Bianca Torres");
        assertThat(response.jsonPath().getString("owner.document_number")).isEqualTo(ownerCpf);
        assertThat(response.jsonPath().getString("owner.email")).endsWith("@lenha.com.br");
    }

    @Test
    void getLegalPerson_keyOfAnotherCompany_returnsNotFound() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person_key");
        String otherLegalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf())
                .getString("person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-agent-key", legalPersonKey)
                .when()
                    .get(GET_LEGAL_PERSON, otherLegalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void getLegalPerson_unknownKey_returnsNotFound() {
        String unknownKey = UUID.randomUUID().toString();
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-agent-key", unknownKey)
                .when()
                    .get(GET_LEGAL_PERSON, unknownKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void getLegalPerson_missingAgentKeyHeader_returnsUnauthorized() {
        MockTools.clearMocker();

        Response response = given()
                .when()
                    .get(GET_LEGAL_PERSON, UUID.randomUUID().toString())
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getLegalPerson_emptyAgentKeyHeader_returnsUnauthorized() {
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-agent-key", "")
                .when()
                    .get(GET_LEGAL_PERSON, UUID.randomUUID().toString())
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }
}
