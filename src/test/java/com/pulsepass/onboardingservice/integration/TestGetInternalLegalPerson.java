package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.CpfGenerator;
import com.pulsepass.onboardingservice.common.MockTools;
import com.pulsepass.onboardingservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestGetInternalLegalPerson extends BaseIntegrationTest {

    private static final String GET_INTERNAL_LEGAL_PERSON = "/onboarding/internal/legal_persons/{legal_person_key}";

    @Test
    void getInternalLegalPerson_validInternalToken_returnsCompanyWithPhonesAndOwner() {
        String ownerCpf = CpfGenerator.generateCpf();
        String legalPersonKey = SetupTools.createLegalPerson(ownerCpf).getString("person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("legal_person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("name")).isEqualTo("Lenha Produções Artísticas Ltda");
        assertThat(response.jsonPath().getString("document_number")).hasSize(14);
        assertThat(response.jsonPath().getString("email")).endsWith("@lenha.com.br");
        assertThat(response.jsonPath().getString("created_at")).endsWith("Z");
        assertThat(response.jsonPath().getList("phone_numbers")).hasSize(1);
        assertThat(response.jsonPath().getString("phone_numbers[0].country_code")).isEqualTo("55");
        assertThat(response.jsonPath().getString("phone_numbers[0].area_code")).isEqualTo("51");
        assertThat(response.jsonPath().getString("phone_numbers[0].number")).isEqualTo("30224180");
        assertThat(response.jsonPath().getString("legal_person_company_type")).isEqualTo("LIMITED");
        assertThat(response.jsonPath().getDouble("monthly_revenue_amount")).isEqualTo(25000.00);
        assertThat(UUID.fromString(response.jsonPath().getString("legal_person_address.legal_person_address_key")))
                .isNotNull();
        assertThat(response.jsonPath().getString("legal_person_address.postal_code")).isEqualTo("90110220");
        assertThat(response.jsonPath().getString("legal_person_address.street"))
                .isEqualTo("Rua Ismael Chaves Barcelos");
        assertThat(response.jsonPath().getString("legal_person_address.number")).isEqualTo("60");
        assertThat(response.jsonPath().getString("legal_person_address.complement")).isEqualTo("Sala 2");
        assertThat(response.jsonPath().getString("legal_person_address.neighborhood")).isEqualTo("Praia de Belas");
        String ownerPersonKey = response.jsonPath().getString("owner.person_key");
        assertThat(UUID.fromString(ownerPersonKey)).isNotNull();
        assertThat(ownerPersonKey).isNotEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("owner.name")).isEqualTo("Bianca Torres");
        assertThat(response.jsonPath().getString("owner.document_number")).isEqualTo(ownerCpf);
        assertThat(response.jsonPath().getString("owner.email")).endsWith("@lenha.com.br");
    }

    @Test
    void getInternalLegalPerson_adminRole_returnsCompany() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "admin")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("legal_person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("owner.name")).isEqualTo("Bianca Torres");
    }

    @Test
    void getInternalLegalPerson_noTokenAndNoRole_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person_key");
        MockTools.clearMocker();

        Response response = given()
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getInternalLegalPerson_organizerRole_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "organizer")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getInternalLegalPerson_unknownKey_returnsNotFound() {
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, UUID.randomUUID().toString())
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void getInternalLegalPerson_keyOutsideUuidFormat_returnsNotFound() {
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, "not-a-uuid")
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void getInternalLegalPerson_wrongInternalTokenWithoutRole_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", "wrong-token")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getInternalLegalPerson_organizerAndAdminRoles_returnsCompany() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "organizer, admin")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("legal_person_key")).isEqualTo(legalPersonKey);
    }

    @Test
    void getInternalLegalPerson_ownerPersonKeyAsLegalPersonKey_returnsNotFound() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person_key");
        MockTools.clearMocker();
        String ownerPersonKey = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, legalPersonKey)
                .then()
                    .extract().response().jsonPath().getString("owner.person_key");

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON, ownerPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }
}
