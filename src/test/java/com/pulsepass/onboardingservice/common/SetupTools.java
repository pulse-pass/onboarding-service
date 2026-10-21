package com.pulsepass.onboardingservice.common;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SetupTools {

    private static final String POST_LEGAL_PERSONS = "/onboarding/legal_persons";

    private SetupTools() {
    }

    public static JsonPath createLegalPerson(String ownerDocumentNumber) {
        MockTools.clearMocker();
        String userId = UUID.randomUUID().toString();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-role", Map.of("role_name", "organizer"),
                Map.of("payload", Map.of("id", UUID.randomUUID().toString())));
        MockTools.addExpectation("keycloak/post-users", Map.of(), Map.of(
                "headers", Map.of("Location", List.of("/admin/realms/pulse-pass/users/" + userId))));
        MockTools.addExpectation("keycloak/post-role_mappings", Map.of("user_id", userId));
        MockTools.addExpectation("organization-service/post-organizations", Map.of(),
                Map.of("payload", Map.of("organization_key", UUID.randomUUID().toString())));

        Map<String, Object> payload = Map.of(
                "document_number", CnpjGenerator.generateCnpj(),
                "name", "Lenha Produções Artísticas Ltda",
                "email", "contato@lenha.com.br",
                "phone_number", Map.of("country_code", "55", "area_code", "51", "number", "30224180"),
                "legal_person_collaborators", List.of(Map.of(
                        "collaborator_role", "OWNER",
                        "document_number", ownerDocumentNumber,
                        "name", "Bianca Torres",
                        "email", "bianca@lenha.com.br",
                        "password", "12345678")));

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(201);
        return response.jsonPath();
    }
}
