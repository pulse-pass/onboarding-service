package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.CpfGenerator;
import com.pulsepass.onboardingservice.common.MockTools;
import com.pulsepass.onboardingservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestGetPerson extends BaseIntegrationTest {

    private static final String GET_PERSON = "/onboarding/persons/{person_key}";
    private static final String GET_INTERNAL_LEGAL_PERSON_COLLABORATORS =
            "/onboarding/internal/legal_persons/{legal_person_key}/collaborators";
    private static final String GET_INTERNAL_PERSON = "/onboarding/internal/persons/{person_key}";

    @Test
    void getPerson_legalPersonWithOwnAgentKey_returnsLegalPersonWithoutOwnerAndNullNaturalPerson() {
        String ownerCpf = CpfGenerator.generateCpf();
        String legalPersonKey = SetupTools.createLegalPerson(ownerCpf).getString("person.person_key");
        MockTools.clearMocker();
        String ownerPersonKey = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response().jsonPath()
                .getString("data[0].natural_person.person.person_key");

        Response response = given()
                    .header("selected-user-key", ownerPersonKey)
                    .header("selected-agent-key", legalPersonKey)
                .when()
                    .get(GET_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("person_type")).isEqualTo("LEGAL");
        assertThat(response.jsonPath().getString("name")).isEqualTo("Lenha Produções Artísticas Ltda");
        assertThat(response.jsonPath().getString("document_number")).hasSize(14);
        assertThat(response.jsonPath().getString("created_at")).endsWith("Z");
        assertThat(response.jsonPath().getString("legal_person.email")).endsWith("@lenha.com.br");
        assertThat(response.jsonPath().getMap("legal_person")).doesNotContainKey("owner");
        assertThat(response.asString()).contains("\"natural_person\":null");
        assertThat(response.jsonPath().getString("legal_person_key")).isNull();
        assertThat(response.jsonPath().getString("phone_numbers")).isNull();
    }

    @Test
    void getPerson_ownerWithOwnUserKey_returnsNaturalPersonAndNullLegalPerson() {
        String ownerCpf = CpfGenerator.generateCpf();
        String ownerEmail = UUID.randomUUID() + "@lenha.com.br";
        String legalPersonKey = SetupTools.createLegalPerson(ownerCpf, UUID.randomUUID() + "@lenha.com.br", ownerEmail)
                .getString("person.person_key");
        MockTools.clearMocker();
        String ownerPersonKey = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response().jsonPath()
                .getString("data[0].natural_person.person.person_key");

        Response response = given()
                    .header("selected-user-key", ownerPersonKey)
                    .header("selected-agent-key", legalPersonKey)
                .when()
                    .get(GET_PERSON, ownerPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo(ownerPersonKey);
        assertThat(response.jsonPath().getString("person_type")).isEqualTo("NATURAL");
        assertThat(response.jsonPath().getString("name")).isEqualTo("Bianca Torres");
        assertThat(response.jsonPath().getString("document_number")).isEqualTo(ownerCpf);
        assertThat(response.jsonPath().getString("created_at")).endsWith("Z");
        assertThat(response.jsonPath().getString("natural_person.email")).isEqualTo(ownerEmail);
        assertThat(response.asString()).contains("\"legal_person\":null");
    }

    @Test
    void getPerson_ownerWithUserKeyOnly_returnsNaturalPerson() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();
        String ownerPersonKey = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response().jsonPath()
                .getString("data[0].natural_person.person.person_key");

        Response response = given()
                    .header("selected-user-key", ownerPersonKey)
                .when()
                    .get(GET_PERSON, ownerPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo(ownerPersonKey);
        assertThat(response.jsonPath().getString("natural_person.email")).endsWith("@lenha.com.br");
    }

    @Test
    void getPerson_seedAdminWithOwnUserKey_returnsNaturalPerson() {
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-key", "00000000-0000-4000-8000-000000000001")
                .when()
                    .get(GET_PERSON, "00000000-0000-4000-8000-000000000001")
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo("00000000-0000-4000-8000-000000000001");
        assertThat(response.jsonPath().getString("person_type")).isEqualTo("NATURAL");
        assertThat(response.jsonPath().getString("name")).isEqualTo("Administrador PulsePass");
        assertThat(response.jsonPath().getString("document_number")).isEqualTo("52998224725");
        assertThat(response.jsonPath().getString("created_at")).endsWith("Z");
        assertThat(response.jsonPath().getString("natural_person.email")).isEqualTo("admin@pulsepass.local");
        assertThat(response.asString()).contains("\"legal_person\":null");
    }

    @Test
    void getPerson_keyOfAnotherAgent_returnsNotFound() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        String otherLegalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf())
                .getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-key", UUID.randomUUID().toString())
                    .header("selected-agent-key", legalPersonKey)
                .when()
                    .get(GET_PERSON, otherLegalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void getPerson_keyOfAnotherUser_returnsNotFound() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-key", UUID.randomUUID().toString())
                .when()
                    .get(GET_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void getPerson_unknownKeyEqualToUserKey_returnsNotFound() {
        String personKey = UUID.randomUUID().toString();
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-key", personKey)
                .when()
                    .get(GET_PERSON, personKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void getPerson_keyOutsideUuidFormat_returnsNotFound() {
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-key", UUID.randomUUID().toString())
                .when()
                    .get(GET_PERSON, "not-a-uuid")
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void getPerson_withoutSelectedUserKey_returnsBadRequest() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-agent-key", legalPersonKey)
                .when()
                    .get(GET_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("selected-user-key");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("REQUIRED");
    }

    @Test
    void getPerson_withoutAnyHeader_returnsBadRequest() {
        MockTools.clearMocker();

        Response response = given()
                .when()
                    .get(GET_PERSON, UUID.randomUUID().toString())
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
    }
}
