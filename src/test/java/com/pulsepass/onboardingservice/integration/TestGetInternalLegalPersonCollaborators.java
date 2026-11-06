package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.CpfGenerator;
import com.pulsepass.onboardingservice.common.MockTools;
import com.pulsepass.onboardingservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestGetInternalLegalPersonCollaborators extends BaseIntegrationTest {

    private static final String GET_INTERNAL_LEGAL_PERSON_COLLABORATORS =
            "/onboarding/internal/legal_persons/{legal_person_key}/collaborators";

    @Test
    void getInternalLegalPersonCollaborators_internalToken_returnsOwner() {
        String ownerCpf = CpfGenerator.generateCpf();
        String ownerEmail = UUID.randomUUID() + "@lenha.com.br";
        String legalPersonKey = SetupTools.createLegalPerson(ownerCpf, UUID.randomUUID() + "@lenha.com.br", ownerEmail)
                .getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(UUID.fromString(response.jsonPath().getString("data[0].legal_person_collaborator_key")))
                .isNotNull();
        assertThat(response.jsonPath().getString("data[0].collaborator_role")).isEqualTo("OWNER");
        assertThat(response.jsonPath().getString("data[0].legal_person_collaborator_status")).isEqualTo("ACTIVE");
        assertThat(response.jsonPath().getString("data[0].natural_person.email")).isEqualTo(ownerEmail);
        String ownerPersonKey = response.jsonPath().getString("data[0].natural_person.person.person_key");
        assertThat(UUID.fromString(ownerPersonKey)).isNotNull();
        assertThat(ownerPersonKey).isNotEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("data[0].natural_person.person.name")).isEqualTo("Bianca Torres");
        assertThat(response.jsonPath().getString("data[0].natural_person.person.document_number"))
                .isEqualTo(ownerCpf);
        assertThat(response.jsonPath().getInt("pagination.page")).isEqualTo(1);
        assertThat(response.jsonPath().getInt("pagination.page_size")).isEqualTo(30);
    }

    @Test
    void getInternalLegalPersonCollaborators_adminRole_returnsOwner() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "organizer, admin")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
    }

    @Test
    void getInternalLegalPersonCollaborators_roleOwner_returnsOnlyOwnerOfThatCompany() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        SetupTools.createLegalPerson(CpfGenerator.generateCpf());
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("collaborator_role", "OWNER")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].collaborator_role")).isEqualTo("OWNER");
    }

    @Test
    void getInternalLegalPersonCollaborators_roleMember_returnsEmptyList() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("collaborator_role", "MEMBER")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).isEmpty();
    }

    @Test
    void getInternalLegalPersonCollaborators_pagination_splitsPages() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response firstPage = given()
                    .header("internal-token", internalToken)
                    .queryParam("page", 1)
                    .queryParam("page_size", 1)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();
        Response secondPage = given()
                    .header("internal-token", internalToken)
                    .queryParam("page", 2)
                    .queryParam("page_size", 1)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(firstPage.statusCode()).isEqualTo(200);
        assertThat(firstPage.jsonPath().getList("data")).hasSize(1);
        assertThat(firstPage.jsonPath().getInt("pagination.page_size")).isEqualTo(1);
        assertThat(secondPage.statusCode()).isEqualTo(200);
        assertThat(secondPage.jsonPath().getList("data")).isEmpty();
        assertThat(secondPage.jsonPath().getInt("pagination.page")).isEqualTo(2);
    }

    @Test
    void getInternalLegalPersonCollaborators_keyOfAnotherCompany_returnsOnlyItsOwnCollaborators() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        String otherOwnerCpf = CpfGenerator.generateCpf();
        SetupTools.createLegalPerson(otherOwnerCpf);
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].natural_person.person.document_number"))
                .isNotEqualTo(otherOwnerCpf);
    }

    @Test
    void getInternalLegalPersonCollaborators_noTokenAndNoRole_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getInternalLegalPersonCollaborators_organizerRole_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("selected-user-roles", "organizer")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getInternalLegalPersonCollaborators_wrongInternalToken_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", "wrong-token")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void getInternalLegalPersonCollaborators_invalidCollaboratorRole_returnsBadRequest() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("collaborator_role", "BOSS")
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("collaborator_role");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("INVALID_FORMAT");
    }

    @Test
    void getInternalLegalPersonCollaborators_unknownKey_returnsEmptyList() {
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, UUID.randomUUID().toString())
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).isEmpty();
    }

    @Test
    void getInternalLegalPersonCollaborators_pageSizeAboveLimit_returnsBadRequest() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("page_size", 101)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("page_size");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("SIZE");
    }

    @Test
    void getInternalLegalPersonCollaborators_pageZero_returnsBadRequest() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("page", 0)
                .when()
                    .get(GET_INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("page");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("SIZE");
    }
}
