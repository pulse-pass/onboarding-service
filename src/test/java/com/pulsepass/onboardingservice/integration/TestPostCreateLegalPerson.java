package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.DocumentGenerators;
import com.pulsepass.onboardingservice.common.MockServerHandlers;
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = DocumentGenerators.generateCpf();
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", cpf);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = DocumentGenerators.generateCnpj();
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", cnpj);
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        String userId = UUID.randomUUID().toString();
        MockServerHandlers.expectUserCreated(cpf, userId);
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

        List<Map<String, Object>> users = MockServerHandlers.retrieveRequests(
                "POST", MockServerHandlers.USERS_PATH, cpf);
        assertThat(users).hasSize(1);
        Map<String, Object> userRequestBody = (Map<String, Object>) users.get(0).get("body");
        JsonPath user = new JsonPath(mapper.writeValueAsString(userRequestBody.get("json")));
        assertThat(user.getString("username")).isEqualTo(cpf);
        assertThat(user.getString("email")).isEqualTo("bianca@lenha.com.br");
        assertThat(user.getBoolean("enabled")).isTrue();
        assertThat(user.getString("attributes.person_key[0]")).isNotBlank().isNotEqualTo(personKey);

        List<Map<String, Object>> roleMappings = MockServerHandlers.retrieveRequests(
                "POST", MockServerHandlers.USERS_PATH + "/" + userId + "/role-mappings/realm", "organizer");
        assertThat(roleMappings).hasSize(1);

        List<Map<String, Object>> organizations = MockServerHandlers.retrieveRequests(
                "POST", MockServerHandlers.ORGANIZATIONS_PATH, cnpj);
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = DocumentGenerators.generateCnpj();
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", cnpj);
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
        assertThat(response.statusCode()).isEqualTo(201);

        Map<String, Object> secondPhoneNumber = new HashMap<>();
        secondPhoneNumber.put("country_code", "55");
        secondPhoneNumber.put("area_code", "51");
        secondPhoneNumber.put("number", "30224180");
        Map<String, Object> secondCollaborator = new HashMap<>();
        secondCollaborator.put("collaborator_role", "OWNER");
        secondCollaborator.put("document_number", DocumentGenerators.generateCpf());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = DocumentGenerators.generateCpf();
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", cpf);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        secondPayload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = DocumentGenerators.generateCnpj();
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = DocumentGenerators.generateCpf();
        char last = cpf.charAt(10);
        String invalid = cpf.substring(0, 10) + (last == '0' ? '1' : '0');
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", invalid);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "MEMBER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj().substring(0, 13));
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf().substring(0, 10));
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "+55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "5511");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "511");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "3022418");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "3022418012");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "not-an-email");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "1234567");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> firstCollaborator = new HashMap<>();
        firstCollaborator.put("collaborator_role", "OWNER");
        firstCollaborator.put("document_number", DocumentGenerators.generateCpf());
        firstCollaborator.put("name", "Bianca Torres");
        firstCollaborator.put("email", "bianca@lenha.com.br");
        firstCollaborator.put("password", "12345678");
        Map<String, Object> secondCollaborator = new HashMap<>();
        secondCollaborator.put("collaborator_role", "OWNER");
        secondCollaborator.put("document_number", DocumentGenerators.generateCpf());
        secondCollaborator.put("name", "Bianca Torres");
        secondCollaborator.put("email", "bianca@lenha.com.br");
        secondCollaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
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
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
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
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "   ");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", DocumentGenerators.generateCpf());
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", DocumentGenerators.generateCnpj());
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
        Response response = given().when().get(POST_LEGAL_PERSONS).then().extract().response();

        assertThat(response.statusCode()).isEqualTo(405);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00405");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_plainTextContentType_returnsUnsupportedMediaType() {
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
        Response response = given().when().get("/rota_inexistente").then().extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00404");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createLegalPerson_identityProviderFailure_returnsUnprocessableContentAndKeepsRegistration() {
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = DocumentGenerators.generateCpf();
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", cpf);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = DocumentGenerators.generateCnpj();
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", cnpj);
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        MockServerHandlers.expectUserCreationFailure(cpf);
        final int roleMappingsBefore = MockServerHandlers.retrieveRequests("POST",
                MockServerHandlers.USERS_PATH + "/" + MockServerHandlers.DEFAULT_USER_ID + "/role-mappings/realm",
                "organizer").size();
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
        assertThat(MockServerHandlers.retrieveRequests("POST", MockServerHandlers.ORGANIZATIONS_PATH, cnpj)).isEmpty();
        // The failed user creation returns no Location, so role-mapping calls cannot be matched by CPF;
        // the count of calls to the default user path must not change.
        assertThat(MockServerHandlers.retrieveRequests("POST",
                MockServerHandlers.USERS_PATH + "/" + MockServerHandlers.DEFAULT_USER_ID + "/role-mappings/realm",
                "organizer")).hasSize(roleMappingsBefore);

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
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        String cpf = DocumentGenerators.generateCpf();
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", cpf);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        String cnpj = DocumentGenerators.generateCnpj();
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", cnpj);
        payload.put("name", "Lenha Produções Artísticas Ltda");
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber);
        payload.put("legal_person_collaborators", new ArrayList<>(List.of(collaborator)));

        MockServerHandlers.expectOrganizationCreationFailure(cnpj);
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
