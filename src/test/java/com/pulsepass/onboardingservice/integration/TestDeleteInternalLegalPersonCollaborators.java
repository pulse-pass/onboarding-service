package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.CpfGenerator;
import com.pulsepass.onboardingservice.common.MockTools;
import com.pulsepass.onboardingservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestDeleteInternalLegalPersonCollaborators extends BaseIntegrationTest {

    private static final String INTERNAL_LEGAL_PERSON_COLLABORATORS =
            "/onboarding/internal/legal_persons/{legal_person_key}/collaborators";

    @Test
    void deleteInternalLegalPersonCollaborators_internalToken_returnsNoContentAndRevokesOwnerAccess() {
        String ownerCpf = CpfGenerator.generateCpf();
        String legalPersonKey = SetupTools.createLegalPerson(ownerCpf).getString("person.person_key");
        String userId = "8c1f6a52-7d3b-4e0a-9b64-2a5d9e7f1c30";
        MockTools.clearMocker();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-users");
        MockTools.addExpectation("keycloak/put-users", Map.of("user_id", userId));
        MockTools.addExpectation("keycloak/post-users_logout", Map.of("user_id", userId));

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(response.body().asString()).isEmpty();

        List<Map<String, Object>> lookups = MockTools.retrieveRequests("GET", "/admin/realms/pulse-pass/users");
        assertThat(lookups).hasSize(1);
        assertThat(lookups.get(0).get("queryStringParameters"))
                .isEqualTo(Map.of("username", List.of(ownerCpf), "exact", List.of("true")));
        List<Map<String, Object>> updates = MockTools.retrieveRequests(
                "PUT", "/admin/realms/pulse-pass/users/" + userId);
        assertThat(updates).hasSize(1);
        assertThat(updates.get(0).get("body").toString()).contains("enabled").contains("false");
        assertThat(MockTools.retrieveRequests("POST", "/admin/realms/pulse-pass/users/" + userId + "/logout"))
                .hasSize(1);

        Response collaborators = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(collaborators.statusCode()).isEqualTo(200);
        assertThat(collaborators.jsonPath().getList("data")).hasSize(1);
        assertThat(collaborators.jsonPath().getString("data[0].collaborator_role")).isEqualTo("OWNER");
        assertThat(collaborators.jsonPath().getString("data[0].legal_person_collaborator_status"))
                .isEqualTo("DELETED");
    }

    @Test
    void deleteInternalLegalPersonCollaborators_repeatedCall_returnsNoContentWithoutCallingKeycloakAgain() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        String userId = "8c1f6a52-7d3b-4e0a-9b64-2a5d9e7f1c30";
        MockTools.clearMocker();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-users");
        MockTools.addExpectation("keycloak/put-users", Map.of("user_id", userId));
        MockTools.addExpectation("keycloak/post-users_logout", Map.of("user_id", userId));

        Response first = given()
                    .header("internal-token", internalToken)
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();
        Response second = given()
                    .header("internal-token", internalToken)
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(first.statusCode()).isEqualTo(204);
        assertThat(second.statusCode()).isEqualTo(204);
        assertThat(MockTools.retrieveRequests("PUT", "/admin/realms/pulse-pass/users/" + userId)).hasSize(1);
        assertThat(MockTools.retrieveRequests("POST", "/admin/realms/pulse-pass/users/" + userId + "/logout"))
                .hasSize(1);
    }

    @Test
    void deleteInternalLegalPersonCollaborators_adminRole_returnsNoContent() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        String userId = "8c1f6a52-7d3b-4e0a-9b64-2a5d9e7f1c30";
        MockTools.clearMocker();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-users");
        MockTools.addExpectation("keycloak/put-users", Map.of("user_id", userId));
        MockTools.addExpectation("keycloak/post-users_logout", Map.of("user_id", userId));

        Response response = given()
                    .header("selected-user-roles", "organizer, admin")
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    void deleteInternalLegalPersonCollaborators_unknownLegalPerson_returnsNotFound() {
        String legalPersonKey = UUID.randomUUID().toString();
        MockTools.clearMocker();

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00009");
    }

    @Test
    void deleteInternalLegalPersonCollaborators_withoutToken_returnsUnauthorized() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();

        Response response = given()
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(MockTools.retrieveRequests("GET", "/admin/realms/pulse-pass/users")).isEmpty();
    }

    @Test
    void deleteInternalLegalPersonCollaborators_keycloakUpdateFails_returnsUnprocessableContentAndKeepsOwnerActive() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        String userId = "8c1f6a52-7d3b-4e0a-9b64-2a5d9e7f1c30";
        MockTools.clearMocker();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-users");
        MockTools.addExpectation("keycloak/put-users_server_error", Map.of("user_id", userId));

        Response response = given()
                    .header("internal-token", internalToken)
                    .header("Accept-Language", "en")
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00011");
        assertThat(response.jsonPath().getString("message"))
                .isEqualTo("We could not revoke the access of the company collaborators. Please try again later.");

        Response collaborators = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(collaborators.statusCode()).isEqualTo(200);
        assertThat(collaborators.jsonPath().getString("data[0].legal_person_collaborator_status"))
                .isEqualTo("ACTIVE");
    }

    @Test
    void deleteInternalLegalPersonCollaborators_tokenFails_returnsUnprocessableContentAndKeepsOwnerActive() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();
        MockTools.addExpectation("keycloak/post-token_server_error");

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00011");

        Response collaborators = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(collaborators.statusCode()).isEqualTo(200);
        assertThat(collaborators.jsonPath().getString("data[0].legal_person_collaborator_status"))
                .isEqualTo("ACTIVE");
    }

    @Test
    void deleteInternalLegalPersonCollaborators_keycloakLookupFails_returnsUnprocessableContentAndKeepsOwnerActive() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-users_server_error");

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00011");

        Response collaborators = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(collaborators.statusCode()).isEqualTo(200);
        assertThat(collaborators.jsonPath().getString("data[0].legal_person_collaborator_status"))
                .isEqualTo("ACTIVE");
    }

    @Test
    void deleteInternalLegalPersonCollaborators_keycloakLogoutFails_returnsUnprocessableContentAndKeepsOwnerActive() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        String userId = "8c1f6a52-7d3b-4e0a-9b64-2a5d9e7f1c30";
        MockTools.clearMocker();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-users");
        MockTools.addExpectation("keycloak/put-users", Map.of("user_id", userId));
        MockTools.addExpectation("keycloak/post-users_logout_server_error", Map.of("user_id", userId));

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00011");

        Response collaborators = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(collaborators.statusCode()).isEqualTo(200);
        assertThat(collaborators.jsonPath().getString("data[0].legal_person_collaborator_status"))
                .isEqualTo("ACTIVE");
    }

    @Test
    void deleteInternalLegalPersonCollaborators_userAbsentInKeycloak_returnsNoContentAndMarksOwnerDeleted() {
        String legalPersonKey = SetupTools.createLegalPerson(CpfGenerator.generateCpf()).getString("person.person_key");
        MockTools.clearMocker();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-users_empty");

        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .delete(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(MockTools.retrieveRequests("GET", "/admin/realms/pulse-pass/users")).hasSize(1);
        assertThat(MockTools.retrieveRequests("PUT", "/admin/realms/pulse-pass/users/.*")).isEmpty();
        assertThat(MockTools.retrieveRequests("POST", "/admin/realms/pulse-pass/users/.*/logout")).isEmpty();

        Response collaborators = given()
                    .header("internal-token", internalToken)
                .when()
                    .get(INTERNAL_LEGAL_PERSON_COLLABORATORS, legalPersonKey)
                .then()
                    .extract().response();

        assertThat(collaborators.statusCode()).isEqualTo(200);
        assertThat(collaborators.jsonPath().getString("data[0].legal_person_collaborator_status"))
                .isEqualTo("DELETED");
    }
}
