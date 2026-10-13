package com.pulsepass.onboardingservice.unit.infrastructure.gateways.keycloak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.onboardingservice.domain.exceptions.UserCreationException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateUserInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.UserRoleInputDTO;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.KeycloakIdentityProviderGateway;
import com.pulsepass.onboardingservice.unit.infrastructure.gateways.StubHttpServer;
import com.pulsepass.onboardingservice.unit.infrastructure.gateways.StubHttpServer.Response;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class KeycloakIdentityProviderGatewayTest {

    private static final String REALM = "pulse-pass";
    private static final String TOKEN_PATH = "/realms/pulse-pass/protocol/openid-connect/token";
    private static final String USERS_PATH = "/admin/realms/pulse-pass/users";
    private static final String USER_PATH = USERS_PATH + "/user-id";
    private static final String ROLE_MAPPINGS_PATH = USER_PATH + "/role-mappings/realm";
    private static final String ROLE_PATH = "/admin/realms/pulse-pass/roles/organizer";

    private final JsonMapper mapper = JsonMapper.builder().build();
    private StubHttpServer server;
    private KeycloakIdentityProviderGateway gateway;

    @BeforeEach
    void setUp() throws Exception {
        server = new StubHttpServer();
        gateway = new KeycloakIdentityProviderGateway(
                server.url(), REALM, "onboarding-service", "client-secret",
                Duration.ofSeconds(2), Duration.ofMillis(300));
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    private static CreateUserInputDTO input(String name, List<UserRoleInputDTO> roles) {
        return new CreateUserInputDTO(
                "52998224725", name, "john@mail.com", "Secret123", "owner-key", roles);
    }

    private void stubHappyPath() {
        server.on("POST", TOKEN_PATH, Response.of(200, "{\"access_token\":\"tok\",\"expires_in\":60}"));
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
    void createUser_tokenWithoutAccessToken_throwsIllegalState() {
        server.on("POST", TOKEN_PATH, Response.of(200, "{}"));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wasCalled("POST", USERS_PATH)).isFalse();
    }

    @Test
    void createUser_tokenEndpointFails_throwsRestClientException() {
        server.on("POST", TOKEN_PATH, Response.of(401, "{\"error\":\"unauthorized_client\"}"));

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(RestClientException.class);

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
    void createUser_serverDown_throwsRestClientException() {
        server.close();

        assertThatThrownBy(() -> gateway.createUser(input("John Doe", List.of(UserRoleInputDTO.ORGANIZER))))
                .isInstanceOf(RestClientException.class);
    }
}
