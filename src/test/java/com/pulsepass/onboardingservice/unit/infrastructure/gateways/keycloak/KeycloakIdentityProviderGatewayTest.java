package com.pulsepass.onboardingservice.unit.infrastructure.gateways.keycloak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.onboardingservice.domain.exceptions.InvalidCredentialsException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidRefreshTokenException;
import com.pulsepass.onboardingservice.domain.exceptions.SessionException;
import com.pulsepass.onboardingservice.domain.exceptions.UserCreationException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateUserInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.DeleteSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.RefreshSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.UserRoleInputDTO;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.KeycloakIdentityProviderGateway;
import com.pulsepass.onboardingservice.unit.infrastructure.gateways.StubHttpServer;
import com.pulsepass.onboardingservice.unit.infrastructure.gateways.StubHttpServer.Response;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class KeycloakIdentityProviderGatewayTest {

    private static final String REALM = "pulse-pass";
    private static final String TOKEN_PATH = "/realms/pulse-pass/protocol/openid-connect/token";
    private static final String USERS_PATH = "/admin/realms/pulse-pass/users";
    private static final String USER_PATH = USERS_PATH + "/user-id";
    private static final String ROLE_MAPPINGS_PATH = USER_PATH + "/role-mappings/realm";
    private static final String LOGOUT_PATH = "/realms/pulse-pass/protocol/openid-connect/logout";
    private static final String TOKEN_BODY = """
            {"access_token":"access-token","expires_in":300,"refresh_token":"refresh-token",
             "refresh_expires_in":1800,"token_type":"Bearer"}
            """;
    private static final String ROLE_PATH = "/admin/realms/pulse-pass/roles/organizer";

    private final JsonMapper mapper = JsonMapper.builder().build();
    private StubHttpServer server;
    private KeycloakIdentityProviderGateway gateway;

    @BeforeEach
    void setUp() throws Exception {
        server = new StubHttpServer();
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(300));
        gateway = new KeycloakIdentityProviderGateway(
                server.url(), REALM, "onboarding-service", "client-secret",
                RestClient.builder().requestFactory(requestFactory));
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    private static CreateUserInputDTO input(String name, List<UserRoleInputDTO> roles) {
        return new CreateUserInputDTO(
                "52998224725", name, "john@mail.com", "Secret123", "owner-key", "legal-key", roles);
    }

    private void stubHappyPath() {
        server.on("POST", TOKEN_PATH,
                Response.of(200, "{\"access_token\":\"tok\",\"expires_in\":60}"));
        server.on("POST", USERS_PATH,
                new Response(201, Map.of("Location", server.url() + USER_PATH), ""));
        server.on("GET", ROLE_PATH,
                Response.of(200, "{\"id\":\"role-id\",\"name\":\"organizer\",\"composite\":false}"));
        server.on("POST", ROLE_MAPPINGS_PATH, Response.empty(204));
    }

    private StubHttpServer.Received requestTo(String method, String path) {
        return server.received().stream()
                .filter(r -> r.method().equals(method) && r.path().equals(path))
                .findFirst()
                .orElseThrow();
    }

    private boolean wasCalled(String method, String path) {
        return server.received().stream().anyMatch(r -> r.method().equals(method) && r.path().equals(path));
    }

    @Test
    void createUser_validInput_runsTokenRoleLookupCreateAndRoleMappingInOrder() {
        stubHappyPath();

        assertThatCode(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .doesNotThrowAnyException();

        assertThat(server.received()).extracting(r -> r.method() + " " + r.path()).containsExactly(
                "POST " + TOKEN_PATH,
                "GET " + ROLE_PATH,
                "POST " + USERS_PATH,
                "POST " + ROLE_MAPPINGS_PATH);
    }

    @Test
    void createUser_validInput_requestsTokenWithClientCredentialsForm() {
        stubHappyPath();

        gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER)));

        var request = requestTo("POST", TOKEN_PATH);
        assertThat(request.headers().get("content-type")).startsWith("application/x-www-form-urlencoded");
        assertThat(request.body()).contains("grant_type=client_credentials")
                .contains("client_id=onboarding-service")
                .contains("client_secret=client-secret");
    }

    @Test
    void createUser_validInput_sendsUserBodyWithSplitNameAndCredentials() throws Exception {
        stubHappyPath();

        gateway.createUser(input("John Doe Smith", List.of(UserRoleInputDTO.ORGANIZER)));

        var request = requestTo("POST", USERS_PATH);
        assertThat(request.headers().get("authorization")).isEqualTo("Bearer tok");
        assertThat(request.headers().get("content-type")).startsWith("application/json");
        JsonNode body = mapper.readTree(request.body());
        assertThat(body.get("username").asString()).isEqualTo("52998224725");
        assertThat(body.get("email").asString()).isEqualTo("john@mail.com");
        assertThat(body.get("firstName").asString()).isEqualTo("John");
        assertThat(body.get("lastName").asString()).isEqualTo("Doe Smith");
        assertThat(body.get("enabled").asBoolean()).isTrue();
        assertThat(body.get("emailVerified").asBoolean()).isTrue();
        assertThat(body.get("attributes").get("person_key").get(0).asString()).isEqualTo("owner-key");
        assertThat(body.get("attributes").get("legal_person_key").get(0).asString()).isEqualTo("legal-key");
        assertThat(body.get("credentials")).hasSize(1);
        assertThat(body.get("credentials").get(0).get("type").asString()).isEqualTo("password");
        assertThat(body.get("credentials").get(0).get("value").asString()).isEqualTo("Secret123");
        assertThat(body.get("credentials").get(0).get("temporary").asBoolean()).isFalse();
    }

    @Test
    void createUser_singleWordName_usesNameAsFirstAndLastName() throws Exception {
        stubHappyPath();

        gateway.createUser(input("  Madonna  ", List.of(UserRoleInputDTO.ORGANIZER)));

        JsonNode body = mapper.readTree(requestTo("POST", USERS_PATH).body());
        assertThat(body.get("firstName").asString()).isEqualTo("Madonna");
        assertThat(body.get("lastName").asString()).isEqualTo("Madonna");
    }

    @Test
    void createUser_validInput_assignsRealmRoleToCreatedUserUsingLocationHeader() throws Exception {
        stubHappyPath();

        gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER)));

        assertThat(requestTo("GET", ROLE_PATH).headers().get("authorization")).isEqualTo("Bearer tok");
        var mapping = requestTo("POST", ROLE_MAPPINGS_PATH);
        assertThat(mapping.headers().get("authorization")).isEqualTo("Bearer tok");
        JsonNode body = mapper.readTree(mapping.body());
        assertThat(body).hasSize(1);
        assertThat(body.get(0).get("id").asString()).isEqualTo("role-id");
        assertThat(body.get(0).get("name").asString()).isEqualTo("organizer");
        assertThat(body.get(0).size()).isEqualTo(2);
    }

    @Test
    void createUser_tokenWithoutAccessToken_throwsUserCreationException() {
        server.on("POST", TOKEN_PATH, Response.of(200, "{}"));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(UserCreationException.class);

        assertThat(wasCalled("POST", USERS_PATH)).isFalse();
    }

    @Test
    void createUser_tokenEndpointFails_throwsUserCreationException() {
        server.on("POST", TOKEN_PATH, Response.of(401, "{\"error\":\"unauthorized_client\"}"));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(UserCreationException.class);

        assertThat(wasCalled("POST", USERS_PATH)).isFalse();
    }

    @Test
    void createUser_statusOkInsteadOfCreated_throwsUserCreationException() {
        stubHappyPath();
        server.on("POST", USERS_PATH, new Response(200, Map.of("Location", server.url() + USER_PATH), ""));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(UserCreationException.class);

        assertThat(wasCalled("POST", ROLE_MAPPINGS_PATH)).isFalse();
    }

    @Test
    void createUser_userAlreadyExistsInKeycloak_throwsUserCreationException() {
        stubHappyPath();
        server.on("POST", USERS_PATH, Response.of(409, "{\"errorMessage\":\"User exists\"}"));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(UserCreationException.class);
    }

    @Test
    void createUser_serverErrorOnUserCreation_throwsUserCreationException() {
        stubHappyPath();
        server.on("POST", USERS_PATH, Response.of(500, "{}"));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(UserCreationException.class);
    }

    @Test
    void createUser_createdWithoutLocationHeader_throwsIllegalState() {
        stubHappyPath();
        server.on("POST", USERS_PATH, Response.empty(201));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wasCalled("POST", ROLE_MAPPINGS_PATH)).isFalse();
    }

    @Test
    void createUser_roleLookupFails_throwsRestClientExceptionWithoutCreatingUser() {
        stubHappyPath();
        server.on("GET", ROLE_PATH, Response.of(404, "{\"error\":\"Role not found\"}"));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(RestClientException.class);

        assertThat(wasCalled("POST", USERS_PATH)).isFalse();
    }

    @Test
    void createUser_roleLookupReturnsEmptyBody_throwsIllegalStateWithoutCreatingUser() {
        stubHappyPath();
        server.on("GET", ROLE_PATH, Response.empty(200));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wasCalled("POST", USERS_PATH)).isFalse();
    }

    @Test
    void createUser_roleMappingFails_throwsUserCreationException() {
        stubHappyPath();
        server.on("POST", ROLE_MAPPINGS_PATH, Response.of(500, "{}"));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(UserCreationException.class);
    }

    @Test
    void createUser_locationWithDifferentHost_usesBaseUrlAndLastPathSegment() {
        stubHappyPath();
        server.on("POST", USERS_PATH,
                new Response(201, Map.of("Location", "http://other-host.invalid:9999/admin/realms/x/users/user-id"), ""));

        assertThatCode(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .doesNotThrowAnyException();

        assertThat(wasCalled("POST", ROLE_MAPPINGS_PATH)).isTrue();
    }

    @Test
    void createUser_multipleSpacesInName_splitsOnFirstWhitespaceRun() throws Exception {
        stubHappyPath();

        gateway.createUser(input("John    Doe  Smith", List.of(UserRoleInputDTO.ORGANIZER)));

        JsonNode body = mapper.readTree(requestTo("POST", USERS_PATH).body());
        assertThat(body.get("firstName").asString()).isEqualTo("John");
        assertThat(body.get("lastName").asString()).isEqualTo("Doe  Smith");
    }

    @Test
    void createUser_readTimeoutExceeded_throwsUserCreationException() {
        stubHappyPath();
        server.delay("POST", USERS_PATH, 1500);

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(UserCreationException.class);

        assertThat(wasCalled("POST", ROLE_MAPPINGS_PATH)).isFalse();
    }

    @Test
    void createUser_serverDown_throwsUserCreationException() {
        server.close();

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(UserCreationException.class);
    }

    @Test
    void createSession_validCredentials_returnsSessionFromKeycloak() {
        server.on("POST", TOKEN_PATH, Response.of(200, TOKEN_BODY));

        var session = gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123"));

        assertThat(session.accessToken()).isEqualTo("access-token");
        assertThat(session.expiresIn()).isEqualTo(300);
        assertThat(session.refreshToken()).isEqualTo("refresh-token");
        assertThat(session.refreshExpiresIn()).isEqualTo(1800);
        assertThat(session.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void createSession_validCredentials_sendsPasswordGrantForm() {
        server.on("POST", TOKEN_PATH, Response.of(200, TOKEN_BODY));

        gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123"));

        var request = requestTo("POST", TOKEN_PATH);
        assertThat(request.headers().get("content-type")).startsWith("application/x-www-form-urlencoded");
        assertThat(request.body()).contains("grant_type=password")
                .contains("client_id=onboarding-service")
                .contains("client_secret=client-secret")
                .contains("username=52998224725")
                .contains("password=Secret123");
    }

    @Test
    void createSession_unauthorizedInvalidGrant_throwsInvalidCredentialsException() {
        server.on("POST", TOKEN_PATH, Response.of(401, "{\"error\":\"invalid_grant\"}"));

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void createSession_badRequestInvalidGrant_throwsInvalidCredentialsException() {
        server.on("POST", TOKEN_PATH, Response.of(400, "{\"error\":\"invalid_grant\"}"));

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void createSession_unauthorizedClient_throwsSessionException() {
        server.on("POST", TOKEN_PATH, Response.of(401, "{\"error\":\"unauthorized_client\"}"));

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void createSession_serverError_throwsSessionException() {
        server.on("POST", TOKEN_PATH, Response.of(500, "{}"));

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void createSession_bodyWithoutAccessToken_throwsSessionException() {
        server.on("POST", TOKEN_PATH, Response.of(200, "{}"));

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void createSession_bodyWithoutRefreshToken_throwsSessionException() {
        server.on("POST", TOKEN_PATH,
                Response.of(200, "{\"access_token\":\"a\",\"expires_in\":300,\"refresh_expires_in\":1800}"));

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void createSession_bodyWithoutExpiresIn_throwsSessionException() {
        server.on("POST", TOKEN_PATH,
                Response.of(200, "{\"access_token\":\"a\",\"refresh_token\":\"r\",\"refresh_expires_in\":1800}"));

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void createSession_bodyWithoutRefreshExpiresIn_throwsSessionException() {
        server.on("POST", TOKEN_PATH,
                Response.of(200, "{\"access_token\":\"a\",\"refresh_token\":\"r\",\"expires_in\":300}"));

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void createSession_emptyBody_throwsSessionException() {
        server.on("POST", TOKEN_PATH, Response.empty(200));

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void createSession_serverDown_throwsSessionException() {
        server.close();

        assertThatThrownBy(() -> gateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void refreshSession_validToken_returnsSessionAndSendsRefreshGrantForm() {
        server.on("POST", TOKEN_PATH, Response.of(200, TOKEN_BODY));

        var session = gateway.refreshSession(new RefreshSessionInputDTO("old-refresh-token"));

        assertThat(session.accessToken()).isEqualTo("access-token");
        assertThat(session.refreshToken()).isEqualTo("refresh-token");
        var request = requestTo("POST", TOKEN_PATH);
        assertThat(request.body()).contains("grant_type=refresh_token")
                .contains("client_id=onboarding-service")
                .contains("client_secret=client-secret")
                .contains("refresh_token=old-refresh-token")
                .doesNotContain("username=");
    }

    @Test
    void refreshSession_badRequestInvalidGrant_throwsInvalidRefreshTokenException() {
        server.on("POST", TOKEN_PATH, Response.of(400, "{\"error\":\"invalid_grant\"}"));

        assertThatThrownBy(() -> gateway.refreshSession(new RefreshSessionInputDTO("expired")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refreshSession_serverError_throwsSessionException() {
        server.on("POST", TOKEN_PATH, Response.of(503, "{}"));

        assertThatThrownBy(() -> gateway.refreshSession(new RefreshSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void refreshSession_unauthorizedInvalidClient_throwsSessionException() {
        server.on("POST", TOKEN_PATH, Response.of(401, "{\"error\":\"invalid_client\"}"));

        assertThatThrownBy(() -> gateway.refreshSession(new RefreshSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void refreshSession_bodyWithoutAccessToken_throwsSessionException() {
        server.on("POST", TOKEN_PATH, Response.of(200, "{}"));

        assertThatThrownBy(() -> gateway.refreshSession(new RefreshSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void refreshSession_emptyBody_throwsSessionException() {
        server.on("POST", TOKEN_PATH, Response.empty(200));

        assertThatThrownBy(() -> gateway.refreshSession(new RefreshSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void refreshSession_bodyWithoutRefreshToken_throwsSessionException() {
        server.on("POST", TOKEN_PATH,
                Response.of(200, "{\"access_token\":\"a\",\"expires_in\":300,\"refresh_expires_in\":1800}"));

        assertThatThrownBy(() -> gateway.refreshSession(new RefreshSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void refreshSession_bodyWithoutExpiresIn_throwsSessionException() {
        server.on("POST", TOKEN_PATH,
                Response.of(200, "{\"access_token\":\"a\",\"refresh_token\":\"r\",\"refresh_expires_in\":1800}"));

        assertThatThrownBy(() -> gateway.refreshSession(new RefreshSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void refreshSession_bodyWithoutRefreshExpiresIn_throwsSessionException() {
        server.on("POST", TOKEN_PATH,
                Response.of(200, "{\"access_token\":\"a\",\"refresh_token\":\"r\",\"expires_in\":300}"));

        assertThatThrownBy(() -> gateway.refreshSession(new RefreshSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void refreshSession_serverDown_throwsSessionException() {
        server.close();

        assertThatThrownBy(() -> gateway.refreshSession(new RefreshSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void deleteSession_validToken_sendsLogoutForm() {
        server.on("POST", LOGOUT_PATH, Response.empty(204));

        assertThatCode(() -> gateway.deleteSession(new DeleteSessionInputDTO("refresh-token")))
                .doesNotThrowAnyException();

        var request = requestTo("POST", LOGOUT_PATH);
        assertThat(request.headers().get("content-type")).startsWith("application/x-www-form-urlencoded");
        assertThat(request.body()).contains("client_id=onboarding-service")
                .contains("client_secret=client-secret")
                .contains("refresh_token=refresh-token");
    }

    @Test
    void deleteSession_badRequestInvalidGrant_throwsInvalidRefreshTokenException() {
        server.on("POST", LOGOUT_PATH, Response.of(400, "{\"error\":\"invalid_grant\"}"));

        assertThatThrownBy(() -> gateway.deleteSession(new DeleteSessionInputDTO("expired")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void deleteSession_badRequestInvalidRequest_throwsSessionException() {
        server.on("POST", LOGOUT_PATH, Response.of(400, "{\"error\":\"invalid_request\"}"));

        assertThatThrownBy(() -> gateway.deleteSession(new DeleteSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void deleteSession_badRequestEmptyBody_throwsSessionException() {
        server.on("POST", LOGOUT_PATH, Response.empty(400));

        assertThatThrownBy(() -> gateway.deleteSession(new DeleteSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void deleteSession_unauthorizedInvalidClient_throwsSessionException() {
        server.on("POST", LOGOUT_PATH, Response.of(401, "{\"error\":\"invalid_client\"}"));

        assertThatThrownBy(() -> gateway.deleteSession(new DeleteSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void deleteSession_serverError_throwsSessionException() {
        server.on("POST", LOGOUT_PATH, Response.of(500, "{}"));

        assertThatThrownBy(() -> gateway.deleteSession(new DeleteSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void deleteSession_unexpectedOkStatus_throwsSessionException() {
        server.on("POST", LOGOUT_PATH, Response.empty(200));

        assertThatThrownBy(() -> gateway.deleteSession(new DeleteSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }

    @Test
    void deleteSession_serverDown_throwsSessionException() {
        server.close();

        assertThatThrownBy(() -> gateway.deleteSession(new DeleteSessionInputDTO("refresh")))
                .isInstanceOf(SessionException.class);
    }
}
