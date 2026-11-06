package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.CpfGenerator;
import com.pulsepass.onboardingservice.common.MockTools;
import com.pulsepass.onboardingservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestGetInternalPerson extends BaseIntegrationTest {

    private static final String GET_INTERNAL_LEGAL_PERSON_COLLABORATORS =
            "/onboarding/internal/legal_persons/{legal_person_key}/collaborators";
    private static final String GET_INTERNAL_PERSON = "/onboarding/internal/persons/{person_key}";

    @Test
    void getInternalPerson_legalPersonWithInternalToken_returnsCompanyWithPhonesAndNoOwner() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("person_type")).isEqualTo("LEGAL");
        assertThat(response.jsonPath().getString("name")).isEqualTo("Lenha Produções Artísticas Ltda");
        assertThat(response.jsonPath().getString("document_number")).hasSize(14);
        assertThat(response.jsonPath().getString("created_at")).endsWith("Z");
        assertThat(response.jsonPath().getList("phone_numbers")).hasSize(1);
        assertThat(response.jsonPath().getString("phone_numbers[0].country_code")).isEqualTo("55");
        assertThat(response.jsonPath().getString("phone_numbers[0].area_code")).isEqualTo("51");
        assertThat(response.jsonPath().getString("phone_numbers[0].number")).isEqualTo("999990000");
        assertThat(response.jsonPath().getString("legal_person.email")).endsWith("@lenha.com.br");
        assertThat(response.jsonPath().getString("legal_person.legal_person_company_type")).isEqualTo("LIMITED");
        assertThat(response.jsonPath().getDouble("legal_person.monthly_revenue_amount")).isEqualTo(25000.00);
        String addressKey = response.jsonPath().getString("legal_person.legal_person_address.legal_person_address_key");
        assertThat(UUID.fromString(addressKey)).isNotNull();
        assertThat(response.jsonPath().getString("legal_person.legal_person_address.postal_code"))
                .isEqualTo("90110220");
        assertThat(response.jsonPath().getString("legal_person.legal_person_address.street"))
                .isEqualTo("Rua Ismael Chaves Barcelos");
        assertThat(response.jsonPath().getString("legal_person.legal_person_address.number")).isEqualTo("60");
        assertThat(response.jsonPath().getString("legal_person.legal_person_address.complement"))
                .isEqualTo("Sala 2");
        assertThat(response.jsonPath().getString("legal_person.legal_person_address.neighborhood"))
                .isEqualTo("Praia de Belas");
        assertThat(response.jsonPath().getMap("legal_person")).doesNotContainKey("owner");
        assertThat(response.asString()).contains("\"natural_person\":null");
    }

    @Test
    void getInternalPerson_ownerWithInternalToken_returnsNaturalPersonWithPhoneNumbersAndNullLegalPerson() {
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
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_PERSON, ownerPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo(ownerPersonKey);
        assertThat(response.jsonPath().getString("person_type")).isEqualTo("NATURAL");
        assertThat(response.jsonPath().getString("name")).isEqualTo("Bianca Torres");
        assertThat(response.jsonPath().getString("document_number")).isEqualTo(ownerCpf);
        assertThat(response.jsonPath().getString("created_at")).endsWith("Z");
        assertThat(response.jsonPath().getList("phone_numbers")).isEmpty();
        assertThat(response.jsonPath().getString("natural_person.email")).isEqualTo(ownerEmail);
        assertThat(response.asString()).contains("\"legal_person\":null");
    }

    @Test
    void getInternalPerson_seedAdminWithAdminRole_returnsNaturalPerson() {
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "admin")
                .when()
                    .get(GET_INTERNAL_PERSON, "00000000-0000-4000-8000-000000000001")
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo("00000000-0000-4000-8000-000000000001");
        assertThat(response.jsonPath().getString("person_type")).isEqualTo("NATURAL");
        assertThat(response.jsonPath().getString("name")).isEqualTo("Administrador PulsePass");
        assertThat(response.jsonPath().getString("document_number")).isEqualTo("52998224725");
        assertThat(response.jsonPath().getList("phone_numbers")).isEmpty();
        assertThat(response.jsonPath().getString("natural_person.email")).isEqualTo("admin@pulsepass.local");
        assertThat(response.asString()).contains("\"legal_person\":null");
    }

    @Test
    void getInternalPerson_adminRole_returnsCompany() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "admin")
                .when()
                    .get(GET_INTERNAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getMap("legal_person")).doesNotContainKey("owner");
    }

    @Test
    void getInternalPerson_organizerAndAdminRoles_returnsCompany() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "organizer, admin")
                .when()
                    .get(GET_INTERNAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("person_key")).isEqualTo(legalPersonKey);
    }

    @Test
    void getInternalPerson_noTokenAndNoRole_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                .when()
                    .get(GET_INTERNAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getInternalPerson_organizerRole_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "organizer")
                .when()
                    .get(GET_INTERNAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getInternalPerson_wrongInternalTokenWithoutRole_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", "wrong-token")
                .when()
                    .get(GET_INTERNAL_PERSON, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getInternalPerson_unknownKey_returnsNotFound() {
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_PERSON, UUID.randomUUID().toString())
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void getInternalPerson_keyOutsideUuidFormat_returnsNotFound() {
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_PERSON, "not-a-uuid")
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }
}
