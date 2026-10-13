package com.pulsepass.onboardingservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.DocumentGenerators;
import com.pulsepass.onboardingservice.common.MockServerHandlers;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class TestPostCreateLegalPerson extends BaseIntegrationTest {

    private static final String POST_LEGAL_PERSONS = "/legal_persons";
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private static final String NAME = "Lenha Produções Artísticas Ltda";

    private static Map<String, Object> phoneNumber() {
        Map<String, Object> phoneNumber = new HashMap<>();
        phoneNumber.put("country_code", "55");
        phoneNumber.put("area_code", "51");
        phoneNumber.put("number", "30224180");
        return phoneNumber;
    }

    private static Map<String, Object> collaborator(String cpf) {
        Map<String, Object> collaborator = new HashMap<>();
        collaborator.put("collaborator_role", "OWNER");
        collaborator.put("document_number", cpf);
        collaborator.put("name", "Bianca Torres");
        collaborator.put("email", "bianca@lenha.com.br");
        collaborator.put("password", "12345678");
        return collaborator;
    }

    private static Map<String, Object> payload(String cnpj, String cpf) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", cnpj);
        payload.put("name", NAME);
        payload.put("email", "contato@lenha.com.br");
        payload.put("phone_number", phoneNumber());
        payload.put("legal_person_collaborators", new java.util.ArrayList<>(List.of(collaborator(cpf))));
        return payload;
    }

    private static Map<String, Object> validPayload() {
        return payload(DocumentGenerators.generateCnpj(), DocumentGenerators.generateCpf());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstCollaborator(Map<String, Object> payload) {
        return ((List<Map<String, Object>>) payload.get("legal_person_collaborators")).get(0);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> phoneNumberOf(Map<String, Object> payload) {
        return (Map<String, Object>) payload.get("phone_number");
    }

    private static Response post(Object payload, String language) {
        return given()
                    .header("Accept-Language", language)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_LEGAL_PERSONS)
                .then()
                    .extract().response();
    }

    private static Response post(Object payload) {
        return post(payload, "en");
    }

    private static void assertError(Response response, int status, String code) {
        assertThat(response.statusCode()).isEqualTo(status);
        assertThat(response.jsonPath().getString("code")).isEqualTo(code);
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    private static void assertSchemaError(Response response, String field, String... errors) {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        Map<?, ?>[] expected = Arrays.stream(errors)
                .map(error -> Map.of("field", field, "error", error))
                .toArray(Map<?, ?>[]::new);
        assertThat(response.jsonPath().getList("extra_fields", Map.class)).containsExactlyInAnyOrder(expected);
    }

    @SuppressWarnings("unchecked")
    private static JsonPath bodyOf(Map<String, Object> recordedRequest) {
        Map<String, Object> body = (Map<String, Object>) recordedRequest.get("body");
        return new JsonPath(JSON_MAPPER.writeValueAsString(body.get("json")));
    }

    @SuppressWarnings("unchecked")
    private static String headerOf(Map<String, Object> recordedRequest, String name) {
        Map<String, List<String>> headers = (Map<String, List<String>>) recordedRequest.get("headers");
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(entry -> entry.getValue().get(0))
                .findFirst()
                .orElse(null);
    }

    @Test
    void createLegalPerson_validPayload_returnsCreatedAndCallsDependencies() {
        String cnpj = DocumentGenerators.generateCnpj();
        String cpf = DocumentGenerators.generateCpf();
        String userId = UUID.randomUUID().toString();
        MockServerHandlers.expectUserCreated(cpf, userId);

        Response response = post(payload(cnpj, cpf));

        assertThat(response.statusCode()).isEqualTo(201);
        String personKey = response.jsonPath().getString("person_key");
        assertThat(UUID.fromString(personKey)).isNotNull();
        assertThat(response.jsonPath().getString("document_number")).isEqualTo(cnpj);
        assertThat(response.jsonPath().getString("name")).isEqualTo(NAME);
        assertThat(response.jsonPath().getString("email")).isEqualTo("contato@lenha.com.br");

        List<Map<String, Object>> users = MockServerHandlers.retrieveRequests(
                "POST", MockServerHandlers.USERS_PATH, cpf);
        assertThat(users).hasSize(1);
        JsonPath user = bodyOf(users.get(0));
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
        JsonPath organization = bodyOf(organizations.get(0));
        assertThat(organization.getString("legal_person_key")).isEqualTo(personKey);
        assertThat(organization.getString("document_number")).isEqualTo(cnpj);
        assertThat(organization.getString("name")).isEqualTo(NAME);
        assertThat(headerOf(organizations.get(0), "internal-token")).isEqualTo(internalToken);
    }

    @Test
    void createLegalPerson_duplicatedCnpj_returnsConflictInPortuguese() {
        String cnpj = DocumentGenerators.generateCnpj();
        assertThat(post(payload(cnpj, DocumentGenerators.generateCpf())).statusCode()).isEqualTo(201);

        Response response = post(payload(cnpj, DocumentGenerators.generateCpf()), "pt-BR");

        assertError(response, 409, "ONB00001");
        assertThat(response.jsonPath().getString("message")).isEqualTo(
                "Este CNPJ já está cadastrado. Peça um convite à dona da conta ou entre com o seu CPF.");
    }

    @Test
    void createLegalPerson_duplicatedCpf_returnsConflict() {
        String cpf = DocumentGenerators.generateCpf();
        assertThat(post(payload(DocumentGenerators.generateCnpj(), cpf)).statusCode()).isEqualTo(201);

        Response response = post(payload(DocumentGenerators.generateCnpj(), cpf));

        assertError(response, 409, "ONB00002");
    }

    @Test
    void createLegalPerson_cnpjWithInvalidCheckDigit_returnsBadRequest() {
        String cnpj = DocumentGenerators.generateCnpj();
        char last = cnpj.charAt(13);
        String invalid = cnpj.substring(0, 13) + (last == '0' ? '1' : '0');

        assertError(post(payload(invalid, DocumentGenerators.generateCpf())), 400, "ONB00003");
    }

    @Test
    void createLegalPerson_cpfWithInvalidCheckDigit_returnsBadRequest() {
        String cpf = DocumentGenerators.generateCpf();
        char last = cpf.charAt(10);
        String invalid = cpf.substring(0, 10) + (last == '0' ? '1' : '0');

        assertError(post(payload(DocumentGenerators.generateCnpj(), invalid)), 400, "ONB00004");
    }

    @Test
    void createLegalPerson_memberRole_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).put("collaborator_role", "MEMBER");

        assertError(post(payload), 400, "ONB00005");
    }

    @Test
    void createLegalPerson_missingDocumentNumber_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.remove("document_number");

        assertSchemaError(post(payload), "document_number", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingName_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.remove("name");

        assertSchemaError(post(payload), "name", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingEmail_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.remove("email");

        assertSchemaError(post(payload), "email", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingPhoneNumber_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.remove("phone_number");

        assertSchemaError(post(payload), "phone_number", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingCollaborators_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.remove("legal_person_collaborators");

        assertSchemaError(post(payload), "legal_person_collaborators", "REQUIRED");
    }

    @Test
    void createLegalPerson_cnpjWithThirteenDigits_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("document_number", DocumentGenerators.generateCnpj().substring(0, 13));

        assertSchemaError(post(payload), "document_number", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_cpfWithTenDigits_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).put("document_number", DocumentGenerators.generateCpf().substring(0, 10));

        assertSchemaError(post(payload), "legal_person_collaborators[0].document_number", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_countryCodeWithPlusSign_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        phoneNumberOf(payload).put("country_code", "+55");

        assertSchemaError(post(payload), "phone_number.country_code", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_countryCodeWithFourDigits_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        phoneNumberOf(payload).put("country_code", "5511");

        assertSchemaError(post(payload), "phone_number.country_code", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_areaCodeWithThreeDigits_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        phoneNumberOf(payload).put("area_code", "511");

        assertSchemaError(post(payload), "phone_number.area_code", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_numberWithSevenDigits_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        phoneNumberOf(payload).put("number", "3022418");

        assertSchemaError(post(payload), "phone_number.number", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_numberWithTenDigits_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        phoneNumberOf(payload).put("number", "3022418012");

        assertSchemaError(post(payload), "phone_number.number", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_invalidEmail_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("email", "not-an-email");

        assertSchemaError(post(payload), "email", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_invalidCollaboratorEmail_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).put("email", "not-an-email");

        assertSchemaError(post(payload), "legal_person_collaborators[0].email", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_passwordWithSevenCharacters_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).put("password", "1234567");

        assertSchemaError(post(payload), "legal_person_collaborators[0].password", "SIZE");
    }

    @Test
    void createLegalPerson_emptyCollaboratorList_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("legal_person_collaborators", List.of());

        assertSchemaError(post(payload), "legal_person_collaborators", "SIZE");
    }

    @Test
    void createLegalPerson_twoCollaborators_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("legal_person_collaborators", List.of(
                collaborator(DocumentGenerators.generateCpf()),
                collaborator(DocumentGenerators.generateCpf())));

        assertSchemaError(post(payload), "legal_person_collaborators", "SIZE");
    }

    @Test
    void createLegalPerson_missingCollaboratorCollaboratorRole_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).remove("collaborator_role");

        assertSchemaError(post(payload), "legal_person_collaborators[0].collaborator_role", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingCollaboratorDocumentNumber_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).remove("document_number");

        assertSchemaError(post(payload), "legal_person_collaborators[0].document_number", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingCollaboratorName_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).remove("name");

        assertSchemaError(post(payload), "legal_person_collaborators[0].name", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingCollaboratorEmail_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).remove("email");

        assertSchemaError(post(payload), "legal_person_collaborators[0].email", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingCollaboratorPassword_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).remove("password");

        assertSchemaError(post(payload), "legal_person_collaborators[0].password", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingPhoneCountryCode_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        phoneNumberOf(payload).remove("country_code");

        assertSchemaError(post(payload), "phone_number.country_code", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingPhoneAreaCode_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        phoneNumberOf(payload).remove("area_code");

        assertSchemaError(post(payload), "phone_number.area_code", "REQUIRED");
    }

    @Test
    void createLegalPerson_missingPhoneNumberDigits_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        phoneNumberOf(payload).remove("number");

        assertSchemaError(post(payload), "phone_number.number", "REQUIRED");
    }

    @Test
    void createLegalPerson_cnpjWithMask_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("document_number", "11.222.333/0001-81");

        assertSchemaError(post(payload), "document_number", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_cpfWithMask_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).put("document_number", "529.982.247-25");

        assertSchemaError(post(payload), "legal_person_collaborators[0].document_number", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_cnpjWithLetters_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("document_number", "1122233300018A");

        assertSchemaError(post(payload), "document_number", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_cpfWithLetters_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).put("document_number", "5299822472A");

        assertSchemaError(post(payload), "legal_person_collaborators[0].document_number", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_blankCollaboratorPassword_returnsRequiredAndSize() {
        Map<String, Object> payload = validPayload();
        firstCollaborator(payload).put("password", "   ");

        assertSchemaError(post(payload), "legal_person_collaborators[0].password", "REQUIRED", "SIZE");
    }

    @Test
    void createLegalPerson_blankDocumentNumber_returnsRequiredAndInvalidFormat() {
        Map<String, Object> payload = validPayload();
        payload.put("document_number", "");

        assertSchemaError(post(payload), "document_number", "REQUIRED", "INVALID_FORMAT");
    }

    @Test
    void createLegalPerson_blankName_returnsRequired() {
        Map<String, Object> payload = validPayload();
        payload.put("name", "   ");

        assertSchemaError(post(payload), "name", "REQUIRED");
    }

    @Test
    void createLegalPerson_schemaErrorInPortuguese_returnsPortugueseMessage() {
        Map<String, Object> payload = validPayload();
        payload.remove("name");

        Response response = post(payload, "pt-BR");

        assertSchemaError(response, "name", "REQUIRED");
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

        assertError(response, 405, "GEN00405");
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

        assertError(response, 415, "GEN00415");
    }

    @Test
    void createLegalPerson_unknownRoute_returnsNotFound() {
        Response response = given().when().get("/rota_inexistente").then().extract().response();

        assertError(response, 404, "GEN00404");
    }

    @Test
    void createLegalPerson_identityProviderFailure_returnsInternalServerErrorAndKeepsRegistration() {
        String cnpj = DocumentGenerators.generateCnpj();
        String cpf = DocumentGenerators.generateCpf();
        MockServerHandlers.expectUserCreationFailure(cpf);
        int roleMappingsBefore = MockServerHandlers.retrieveRequests("POST",
                MockServerHandlers.USERS_PATH + "/" + MockServerHandlers.DEFAULT_USER_ID + "/role-mappings/realm",
                "organizer").size();

        Response response = post(payload(cnpj, cpf));

        assertError(response, 500, "ONB00006");
        assertThat(MockServerHandlers.retrieveRequests("POST", MockServerHandlers.ORGANIZATIONS_PATH, cnpj)).isEmpty();
        // The failed user creation returns no Location, so role-mapping calls cannot be matched by CPF;
        // the count of calls to the default user path must not change.
        assertThat(MockServerHandlers.retrieveRequests("POST",
                MockServerHandlers.USERS_PATH + "/" + MockServerHandlers.DEFAULT_USER_ID + "/role-mappings/realm",
                "organizer")).hasSize(roleMappingsBefore);
        assertError(post(payload(cnpj, cpf)), 409, "ONB00001");
    }

    @Test
    void createLegalPerson_organizationServiceFailure_returnsInternalServerErrorAndKeepsRegistration() {
        String cnpj = DocumentGenerators.generateCnpj();
        String cpf = DocumentGenerators.generateCpf();
        MockServerHandlers.expectOrganizationCreationFailure(cnpj);

        Response response = post(payload(cnpj, cpf));

        assertError(response, 500, "ONB00007");
        assertError(post(payload(cnpj, cpf)), 409, "ONB00001");
    }
}
