package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.CnpjGenerator;
import com.pulsepass.onboardingservice.common.CpfGenerator;
import com.pulsepass.onboardingservice.common.MockTools;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class TestPostCreateLegalPerson extends BaseIntegrationTest {

    private static final String POST_LEGAL_PERSONS = "/onboarding/legal_persons";

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    @SuppressWarnings("unchecked")
    void createLegalPerson_validPayload_returnsCreatedAndCallsDependencies() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = CpfGenerator.generateCpf();
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", cpf);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = CnpjGenerator.generateCnpj();
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", cnpj);
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        String userId = UUID.randomUUID().toString();
        String roleId = UUID.randomUUID().toString();
        String organizationKey = UUID.randomUUID().toString();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-role", Map.of("role_name", "organizer"),
                Map.of("payload", Map.of("id", roleId)));
        MockTools.addExpectation("keycloak/post-users", Map.of(), Map.of(
                "headers", Map.of("Location", List.of("/admin/realms/pulse-pass/users/" + userId))));
        MockTools.addExpectation("keycloak/post-role_mappings", Map.of("user_id", userId));
        MockTools.addExpectation("organization-service/post-organizations", Map.of(),
                Map.of("payload", Map.of("organization_key", organizationKey)));
        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(201);
        String personKey = response.jsonPath().getString("person_key");
        assertThat(UUID.fromString(personKey)).isNotNull();
        assertThat(response.jsonPath().getString("document_number")).isEqualTo(cnpj);
        assertThat(response.jsonPath().getString("name")).isEqualTo("Lenha Produções Artísticas Ltda");
        assertThat(response.jsonPath().getString("email")).isEqualTo("contato@lenha.com.br");

        List<Map<String, Object>> users = MockTools.retrieveRequests(
                "POST", "/admin/realms/pulse-pass/users");
        assertThat(users).hasSize(1);
        Map<String, Object> userRequestBody = (Map<String, Object>) users.get(0).get("body");
        JsonPath user = new JsonPath(mapper.writeValueAsString(userRequestBody.get("json")));
        assertThat(user.getString("username")).isEqualTo(cpf);
        assertThat(user.getString("email")).isEqualTo("bianca@lenha.com.br");
        assertThat(user.getBoolean("enabled")).isTrue();
        assertThat(user.getString("attributes.person_key[0]")).isNotBlank().isNotEqualTo(personKey);

        List<Map<String, Object>> roleMappings = MockTools.retrieveRequests(
                "POST", "/admin/realms/pulse-pass/users/" + userId + "/role-mappings/realm");
        assertThat(roleMappings).hasSize(1);
        Map<String, Object> roleMappingRequestBody = (Map<String, Object>) roleMappings.get(0).get("body");
        JsonPath roleMapping = new JsonPath(mapper.writeValueAsString(roleMappingRequestBody.get("json")));
        assertThat(roleMapping.getList("$")).hasSize(1);
        assertThat(roleMapping.getString("[0].id")).isEqualTo(roleId);
        assertThat(roleMapping.getString("[0].name")).isEqualTo("organizer");

        List<Map<String, Object>> organizations = MockTools.retrieveRequests(
                "POST", "/organization/internal/organizations");
        assertThat(organizations).hasSize(1);
        Map<String, Object> organizationRequestBody = (Map<String, Object>) organizations.get(0).get("body");
        JsonPath organization = new JsonPath(mapper.writeValueAsString(organizationRequestBody.get("json")));
        assertThat(organization.getString("legal_person_key")).isEqualTo(personKey);
        assertThat(organization.getString("document_number")).isEqualTo(cnpj);
        assertThat(organization.getString("name")).isEqualTo("Lenha Produções Artísticas Ltda");
        Map<String, List<String>> organizationHeaders = (Map<String, List<String>>) organizations.get(0).get("headers");
        String organizationInternalToken = organizationHeaders.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase("internal-token"))
                .map(entry -> entry.getValue().get(0))
                .findFirst()
                .orElse(null);
        assertThat(organizationInternalToken).isEqualTo(internalToken);
    }

    @Test
    void createLegalPerson_duplicatedCnpj_returnsConflictInPortuguese() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = CnpjGenerator.generateCnpj();
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", cnpj);
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        String userId = UUID.randomUUID().toString();
        String roleId = UUID.randomUUID().toString();
        String organizationKey = UUID.randomUUID().toString();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-role", Map.of("role_name", "organizer"),
                Map.of("payload", Map.of("id", roleId)));
        MockTools.addExpectation("keycloak/post-users", Map.of(), Map.of(
                "headers", Map.of("Location", List.of("/admin/realms/pulse-pass/users/" + userId))));
        MockTools.addExpectation("keycloak/post-role_mappings", Map.of("user_id", userId));
        MockTools.addExpectation("organization-service/post-organizations", Map.of(),
                Map.of("payload", Map.of("organization_key", organizationKey)));
        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();
        assertThat(response.statusCode()).isEqualTo(201);

        Map<String, Object> secondPhoneNumber = new HashMap<>();
        secondPhoneNumber.put("country_code", "55");
        secondPhoneNumber.put("area_code", "51");
        secondPhoneNumber.put("number", "30224180");
        Map<String, Object> secondCollaborator = new HashMap<>();
        secondCollaborator.put("collaborator_role", "OWNER");
        secondCollaborator.put("document_number", CpfGenerator.generateCpf());
        secondCollaborator.put("name", "Bianca Torres");
        secondCollaborator.put("email", "bianca@lenha.com.br");
        secondCollaborator.put("password", "12345678");
        Map<String, Object> secondPayload = new HashMap<>();
        secondPayload.put("document_number", cnpj);
        secondPayload.put("name", "Lenha Produções Artísticas Ltda");
        secondPayload.put("email", "contato@lenha.com.br");
        secondPayload.put("phone_number", secondPhoneNumber);
        secondPayload.put("legal_person_collaborators", new ArrayList<>(List.of(secondCollaborator)));

        Response secondResponse = given()
                    .header("Accept-Language", "pt-BR")
                    .contentType(ContentType.JSON)
                    .body(secondPayload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(secondResponse.statusCode()).isEqualTo(409);
        assertThat(secondResponse.jsonPath().getString("code")).isEqualTo("ONB00001");
        assertThat(secondResponse.jsonPath().getList("extra_fields")).isEmpty();
        assertThat(secondResponse.jsonPath().getString("message")).isEqualTo(
                "Este CNPJ já está cadastrado. Peça um convite à dona da conta ou entre com o seu CPF.");
    }

    @Test
    void createLegalPerson_duplicatedCpf_returnsConflict() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = CpfGenerator.generateCpf();
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", cpf);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        String userId = UUID.randomUUID().toString();
        String roleId = UUID.randomUUID().toString();
        String organizationKey = UUID.randomUUID().toString();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-role", Map.of("role_name", "organizer"),
                Map.of("payload", Map.of("id", roleId)));
        MockTools.addExpectation("keycloak/post-users", Map.of(), Map.of(
                "headers", Map.of("Location", List.of("/admin/realms/pulse-pass/users/" + userId))));
        MockTools.addExpectation("keycloak/post-role_mappings", Map.of("user_id", userId));
        MockTools.addExpectation("organization-service/post-organizations", Map.of(),
                Map.of("payload", Map.of("organization_key", organizationKey)));
        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();
        assertThat(response.statusCode()).isEqualTo(201);

        Map<String, Object> secondPhoneNumber = new HashMap<>();
        secondPhoneNumber.put("country_code", "55");
        secondPhoneNumber.put("area_code", "51");
        secondPhoneNumber.put("number", "30224180");
        Map<String, Object> secondCollaborator = new HashMap<>();
        secondCollaborator.put("collaborator_role", "OWNER");
        secondCollaborator.put("document_number", cpf);
        secondCollaborator.put("name", "Bianca Torres");
        secondCollaborator.put("email", "bianca@lenha.com.br");
        secondCollaborator.put("password", "12345678");
        Map<String, Object> secondPayload = new HashMap<>();
        secondPayload.put("document_number", CnpjGenerator.generateCnpj());
        secondPayload.put("name", "Lenha Produções Artísticas Ltda");
        secondPayload.put("email", "contato@lenha.com.br");
        secondPayload.put("phone_number", secondPhoneNumber);
        secondPayload.put("legal_person_collaborators", new ArrayList<>(List.of(secondCollaborator)));

        Response secondResponse = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(secondPayload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(secondResponse.statusCode()).isEqualTo(409);
        assertThat(secondResponse.jsonPath().getString("code")).isEqualTo("ONB00002");
        assertThat(secondResponse.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_cnpjWithInvalidCheckDigit_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = CnpjGenerator.generateCnpj();
        char last = cnpj.charAt(13);
        String invalid = cnpj.substring(0, 13) + (last == '0' ? '1' : '0');
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", invalid);
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00003");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_cpfWithInvalidCheckDigit_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = CpfGenerator.generateCpf();
        char last = cpf.charAt(10);
        String invalid = cpf.substring(0, 10) + (last == '0' ? '1' : '0');
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", invalid);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00004");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_memberRole_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "MEMBER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00005");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_missingDocumentNumber_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "document_number", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingName_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "name", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingEmail_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "email", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingPhoneNumber_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "phone_number", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingCollaborators_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_cnpjWithThirteenDigits_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj().substring(0, 13));
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "document_number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_cpfWithTenDigits_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf().substring(0, 10));
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].document_number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_countryCodeWithPlusSign_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "+55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "phone_number.country_code", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_countryCodeWithFourDigits_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "5511");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "phone_number.country_code", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_areaCodeWithThreeDigits_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "511");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "phone_number.area_code", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_numberWithSevenDigits_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "3022418");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "phone_number.number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_numberWithTenDigits_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "3022418012");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "phone_number.number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_invalidEmail_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "not-an-email");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "email", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_invalidCollaboratorEmail_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "not-an-email");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].email", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_passwordWithSevenCharacters_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "1234567");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].password", "error", "SIZE"));
    }

    @Test
    void createLegalPerson_emptyCollaboratorList_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", List.of());

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators", "error", "SIZE"));
    }

    @Test
    void createLegalPerson_twoCollaborators_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> firstCollaborator = new HashMap<>();
        firstCollaborator.put("collaborator_role", "OWNER");
        firstCollaborator.put("document_number", CpfGenerator.generateCpf());
        firstCollaborator.put("name", "Bianca Torres");
        firstCollaborator.put("email", "bianca@lenha.com.br");
        firstCollaborator.put("password", "12345678");
        Map<String, Object> secondCollaborator = new HashMap<>();
        secondCollaborator.put("collaborator_role", "OWNER");
        secondCollaborator.put("document_number", CpfGenerator.generateCpf());
        secondCollaborator.put("name", "Bianca Torres");
        secondCollaborator.put("email", "bianca@lenha.com.br");
        secondCollaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(firstCollaborator, secondCollaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators", "error", "SIZE"));
    }

    @Test
    void createLegalPerson_missingCollaboratorCollaboratorRole_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].collaborator_role", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingCollaboratorDocumentNumber_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].document_number", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingCollaboratorName_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].name", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingCollaboratorEmail_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].email", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingCollaboratorPassword_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].password", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingPhoneCountryCode_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "phone_number.country_code", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingPhoneAreaCode_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "phone_number.area_code", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_missingPhoneNumberDigits_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "phone_number.number", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_cnpjWithMask_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", "11.222.333/0001-81");
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "document_number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_cpfWithMask_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", "529.982.247-25");
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].document_number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_cnpjWithLetters_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", "1122233300018A");
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "document_number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_cpfWithLetters_returnsBadRequest() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", "5299822472A");
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].document_number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_blankCollaboratorPassword_returnsRequiredAndSize() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "   ");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "legal_person_collaborators[0].password", "error", "REQUIRED"),
                Map.of("field", "legal_person_collaborators[0].password", "error", "SIZE"));
    }

    @Test
    void createLegalPerson_blankDocumentNumber_returnsRequiredAndInvalidFormat() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", "");
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "document_number", "error", "REQUIRED"),
                Map.of("field", "document_number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_blankName_returnsRequired() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "   ");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "name", "error", "REQUIRED"));
    }

    @Test
    void createLegalPerson_schemaErrorInPortuguese_returnsPortugueseMessage() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", CpfGenerator.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        Response response = given()
                    .header("Accept-Language", "pt-BR")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(
                Map.of("field", "name", "error", "REQUIRED"));
        assertThat(response.jsonPath().getString("message")).isEqualTo(
                "A requisição é inválida. Verifique as informações enviadas e tente novamente.");
    }

    @Test
    void createLegalPerson_malformedJson_returnsBadRequestWithBodyField() {
        MockTools.clearMocker();
        Response response = given()
                    .contentType(ContentType.JSON)
                    .body("{\"document_number\": ")
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "body", "error", "INVALID_FORMAT"));
    }

    @Test
    void createLegalPerson_getMethod_returnsMethodNotAllowed() {
        MockTools.clearMocker();
        Response response = given().when().get(POST_LEGAL_PERSONS).then().extract().response();

        assertThat(response.statusCode()).isEqualTo(405);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00405");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_plainTextContentType_returnsUnsupportedMediaType() {
        MockTools.clearMocker();
        Response response = given()
                    .contentType(ContentType.TEXT)
                    .body("plain")
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(415);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00415");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_unknownRoute_returnsNotFound() {
        MockTools.clearMocker();
        Response response = given().when().get("/rota_inexistente").then().extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00404");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_identityProviderFailure_returnsUnprocessableContentAndKeepsRegistration() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = CpfGenerator.generateCpf();
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", cpf);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = CnpjGenerator.generateCnpj();
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", cnpj);
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-role", Map.of("role_name", "organizer"));
        MockTools.addExpectation("keycloak/post-users_server_error");
        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00006");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
        assertThat(MockTools.retrieveRequests("POST", "/admin/realms/pulse-pass/users")).hasSize(1);
        assertThat(MockTools.retrieveRequests(
                "POST", "/admin/realms/pulse-pass/users/.*/role-mappings/realm")).isEmpty();
        assertThat(MockTools.retrieveRequests("POST", "/organization/internal/organizations")).isEmpty();

        Map<String, Object> secondPhoneNumber = new HashMap<>();
        secondPhoneNumber.put("country_code", "55");
        secondPhoneNumber.put("area_code", "51");
        secondPhoneNumber.put("number", "30224180");
        Map<String, Object> secondCollaborator = new HashMap<>();
        secondCollaborator.put("collaborator_role", "OWNER");
        secondCollaborator.put("document_number", cpf);
        secondCollaborator.put("name", "Bianca Torres");
        secondCollaborator.put("email", "bianca@lenha.com.br");
        secondCollaborator.put("password", "12345678");
        Map<String, Object> secondPayload = new HashMap<>();
        secondPayload.put("document_number", cnpj);
        secondPayload.put("name", "Lenha Produções Artísticas Ltda");
        secondPayload.put("email", "contato@lenha.com.br");
        secondPayload.put("phone_number", secondPhoneNumber);
        secondPayload.put("legal_person_collaborators", new ArrayList<>(List.of(secondCollaborator)));

        Response secondResponse = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(secondPayload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(secondResponse.statusCode()).isEqualTo(409);
        assertThat(secondResponse.jsonPath().getString("code")).isEqualTo("ONB00001");
        assertThat(secondResponse.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_organizationServiceFailure_returnsUnprocessableContentAndKeepsRegistration() {
        MockTools.clearMocker();
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = CpfGenerator.generateCpf();
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", cpf);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = CnpjGenerator.generateCnpj();
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", cnpj);
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        String userId = UUID.randomUUID().toString();
        MockTools.addExpectation("keycloak/post-token");
        MockTools.addExpectation("keycloak/get-role", Map.of("role_name", "organizer"));
        MockTools.addExpectation("keycloak/post-users", Map.of(), Map.of(
                "headers", Map.of("Location", List.of("/admin/realms/pulse-pass/users/" + userId))));
        MockTools.addExpectation("keycloak/post-role_mappings", Map.of("user_id", userId));
        MockTools.addExpectation("organization-service/post-organizations_server_error");
        Response response = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ONB00007");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();

        Map<String, Object> secondPhoneNumber = new HashMap<>();
        secondPhoneNumber.put("country_code", "55");
        secondPhoneNumber.put("area_code", "51");
        secondPhoneNumber.put("number", "30224180");
        Map<String, Object> secondCollaborator = new HashMap<>();
        secondCollaborator.put("collaborator_role", "OWNER");
        secondCollaborator.put("document_number", cpf);
        secondCollaborator.put("name", "Bianca Torres");
        secondCollaborator.put("email", "bianca@lenha.com.br");
        secondCollaborator.put("password", "12345678");
        Map<String, Object> secondPayload = new HashMap<>();
        secondPayload.put("document_number", cnpj);
        secondPayload.put("name", "Lenha Produções Artísticas Ltda");
        secondPayload.put("email", "contato@lenha.com.br");
        secondPayload.put("phone_number", secondPhoneNumber);
        secondPayload.put("legal_person_collaborators", new ArrayList<>(List.of(secondCollaborator)));

        Response secondResponse = given()
                    .header("Accept-Language", "en")
                    .contentType(ContentType.JSON)
                    .body(secondPayload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();

        assertThat(secondResponse.statusCode()).isEqualTo(409);
        assertThat(secondResponse.jsonPath().getString("code")).isEqualTo("ONB00001");
        assertThat(secondResponse.jsonPath().getList("extra_fields")).isEmpty();
    }
}
