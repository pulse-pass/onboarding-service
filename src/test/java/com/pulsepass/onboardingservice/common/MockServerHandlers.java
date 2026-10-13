package com.pulsepass.onboardingservice.common;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MockServerHandlers {

    public static final String TOKEN_PATH = "/realms/pulse-pass/protocol/openid-connect/token";
    public static final String USERS_PATH = "/admin/realms/pulse-pass/users";
    public static final String ROLE_PATH = "/admin/realms/pulse-pass/roles/organizer";
    public static final String ORGANIZATIONS_PATH = "/internal/organizations";

    public static final String DEFAULT_USER_ID = "00000000-0000-4000-8000-000000000001";
    private static final String ROLE_ID = "00000000-0000-4000-8000-000000000002";
    private static final String INTERNAL_BASE_URL = "http://onboarding-service-mockserver:1080";
    private static final int FAILURE_PRIORITY = 10;

    private static String baseUri = "http://localhost";
    private static int port = 1081;

    private MockServerHandlers() {
    }

    private static Map<String, Object> request(String method, String path) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", method);
        request.put("path", path);
        return request;
    }

    private static Map<String, Object> bodyContaining(String value) {
        return Map.of("type", "REGEX", "regex", "(?s).*" + value + ".*");
    }

    private static Map<String, Object> jsonResponse(int status, Map<String, Object> body) {
        return Map.of(
                "statusCode", status,
                "headers", Map.of("Content-Type", List.of("application/json")),
                "body", body
        );
    }

    private static void expect(int priority, Map<String, Object> request, Map<String, Object> response) {
        given()
                .baseUri(baseUri).port(port)
                .contentType(ContentType.JSON)
                .body(Map.of("priority", priority, "httpRequest", request, "httpResponse", response))
            .when()
                .put("/mockserver/expectation")
            .then()
                .statusCode(201);
    }

    public static void configure(String newBaseUri, int newPort) {
        baseUri = newBaseUri;
        port = newPort;
    }

    public static void registerDefaults() {
        expect(0, request("POST", TOKEN_PATH), jsonResponse(200, Map.of("access_token", "mock-access-token")));
        expect(0, request("POST", USERS_PATH), userCreatedResponse(DEFAULT_USER_ID));
        expect(0, request("GET", ROLE_PATH), jsonResponse(200, Map.of("id", ROLE_ID, "name", "organizer")));
        expect(0,
                request("POST", USERS_PATH + "/" + DEFAULT_USER_ID + "/role-mappings/realm"),
                Map.of("statusCode", 204));
        expect(0, request("POST", ORGANIZATIONS_PATH), jsonResponse(201, Map.of(
                "organization_key", UUID.randomUUID().toString(),
                "organization_status", "PENDING")));
    }

    private static Map<String, Object> userCreatedResponse(String userId) {
        return Map.of(
                "statusCode", 201,
                "headers", Map.of("Location", List.of(INTERNAL_BASE_URL + USERS_PATH + "/" + userId))
        );
    }

    public static void expectUserCreated(String documentNumber, String userId) {
        Map<String, Object> request = request("POST", USERS_PATH);
        request.put("body", bodyContaining(documentNumber));
        expect(FAILURE_PRIORITY, request, userCreatedResponse(userId));
        expect(0, request("POST", USERS_PATH + "/" + userId + "/role-mappings/realm"), Map.of("statusCode", 204));
    }

    public static void expectUserCreationFailure(String documentNumber) {
        Map<String, Object> request = request("POST", USERS_PATH);
        request.put("body", bodyContaining(documentNumber));
        expect(FAILURE_PRIORITY, request, Map.of("statusCode", 500));
    }

    public static void expectOrganizationCreationFailure(String documentNumber) {
        Map<String, Object> request = request("POST", ORGANIZATIONS_PATH);
        request.put("body", bodyContaining(documentNumber));
        expect(FAILURE_PRIORITY, request, Map.of("statusCode", 500));
    }

    public static List<Map<String, Object>> retrieveRequests(String method, String path, String bodyContent) {
        Map<String, Object> filter = request(method, path);
        filter.put("body", bodyContaining(bodyContent));
        Response response = given()
                .baseUri(baseUri).port(port)
                .queryParam("type", "REQUESTS")
                .contentType(ContentType.JSON)
                .body(filter)
                .when()
                    .put("/mockserver/retrieve")
                .then()
                    .statusCode(200)
                    .extract().response();
        return response.jsonPath().getList("$");
    }
}
