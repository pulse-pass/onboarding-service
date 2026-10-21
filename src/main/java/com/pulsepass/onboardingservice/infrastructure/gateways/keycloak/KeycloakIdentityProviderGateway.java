package com.pulsepass.onboardingservice.infrastructure.gateways.keycloak;

import com.pulsepass.onboardingservice.domain.exceptions.InvalidCredentialsException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidRefreshTokenException;
import com.pulsepass.onboardingservice.domain.exceptions.SessionException;
import com.pulsepass.onboardingservice.domain.exceptions.UserCreationException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.IdentityProviderGateway;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateUserInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.DeleteSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.RefreshSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.SessionOutputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.UserRoleInputDTO;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakCreateUserRequest;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakCredentialRequest;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakRoleMappingRequest;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakRoleResponse;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakTokenResponse;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KeycloakIdentityProviderGateway implements IdentityProviderGateway {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakIdentityProviderGateway.class);

    private final RestClient restClient;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    public KeycloakIdentityProviderGateway(
            @Value("${app.keycloak.url}") String url,
            @Value("${app.keycloak.realm}") String realm,
            @Value("${app.keycloak.client-id}") String clientId,
            @Value("${app.keycloak.client-secret}") String clientSecret,
            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder
                .baseUrl(url)
                .build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private String fetchAccessToken(String personKey) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", this.clientId);
        form.add("client_secret", this.clientSecret);

        KeycloakTokenResponse response;
        try {
            response = this.restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", this.realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);
        } catch (RestClientException ex) {
            logger.error(
                    "KeycloakIdentityProviderGateway.fetchAccessToken - Token request failed {}",
                    Map.of("person_key", personKey),
                    ex
            );
            throw new UserCreationException();
        }

        if (response == null || response.accessToken() == null) {
            logger.error(
                    "KeycloakIdentityProviderGateway.fetchAccessToken - Token response without access token {}",
                    Map.of("person_key", personKey)
            );
            throw new UserCreationException();
        }

        return response.accessToken();
    }

    private KeycloakRoleMappingRequest fetchRole(String accessToken, UserRoleInputDTO role) {
        KeycloakRoleResponse response = restClient.get()
                .uri("/admin/realms/{realm}/roles/{role}", this.realm, role.name().toLowerCase(Locale.ROOT))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KeycloakRoleResponse.class);

        if (response == null) {
            throw new IllegalStateException("Keycloak role response is empty");
        }

        return new KeycloakRoleMappingRequest(response.id(), response.name());
    }

    private boolean isInvalidGrant(KeycloakTokenResponse body) {
        return body != null && "invalid_grant".equals(body.error());
    }

    @Override
    public void createUser(CreateUserInputDTO input) {
        logger.info(
                "KeycloakIdentityProviderGateway.createUser - Creating user {}",
                Map.of("person_key", input.personKey())
        );

        String name = input.name().trim();
        String[] nameParts = name.split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length < 2 ? nameParts[0] : nameParts[1];

        var request = new KeycloakCreateUserRequest(
                input.documentNumber(),
                input.email(),
                firstName,
                lastName,
                true,
                true,
                Map.of(
                        "person_key", List.of(input.personKey()),
                        "legal_person_key", List.of(input.legalPersonKey())
                ),
                List.of(new KeycloakCredentialRequest("password", input.password(), false))
        );

        String accessToken = fetchAccessToken(input.personKey());
        List<KeycloakRoleMappingRequest> roles = input.roles().stream()
                .map(role -> fetchRole(accessToken, role))
                .toList();

        ResponseEntity<Void> response;
        try {
            response = restClient.post()
                    .uri("/admin/realms/{realm}/users", this.realm)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            logger.error(
                    "KeycloakIdentityProviderGateway.createUser - User creation request failed {}",
                    Map.of("person_key", input.personKey()),
                    ex
            );
            throw new UserCreationException();
        }

        if (response.getStatusCode() != HttpStatus.CREATED) {
            logger.error(
                    "KeycloakIdentityProviderGateway.createUser - Unexpected status {}",
                    Map.of("step", "create_user", "status", response.getStatusCode().value())
            );
            throw new UserCreationException();
        }

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new IllegalStateException("Keycloak user creation response without Location header");
        }
        String userId = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);

        try {
            restClient.post()
                    .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", this.realm, userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(roles)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            logger.error(
                    "KeycloakIdentityProviderGateway.createUser - Role mapping request failed {}",
                    Map.of("person_key", input.personKey()),
                    ex
            );
            throw new UserCreationException();
        }
    }

    @Override
    public SessionOutputDTO createSession(CreateSessionInputDTO input) {
        logger.info(
                "KeycloakIdentityProviderGateway.createSession - Creating session {}",
                Map.of("document_number", input.documentNumber())
        );

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", this.clientId);
        form.add("client_secret", this.clientSecret);
        form.add("username", input.documentNumber());
        form.add("password", input.password());

        ResponseEntity<KeycloakTokenResponse> response;
        try {
            response = this.restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", this.realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, errorResponse) -> {})
                    .toEntity(KeycloakTokenResponse.class);
        } catch (RestClientException ex) {
            logger.error("KeycloakIdentityProviderGateway.createSession - Token request failed", ex);
            throw new SessionException();
        }

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && isInvalidGrant(response.getBody())) {
            logger.warn(
                    "KeycloakIdentityProviderGateway.createSession - Invalid credentials {}",
                    Map.of("document_number", input.documentNumber())
            );
            throw new InvalidCredentialsException();
        }

        if (response.getStatusCode() == HttpStatus.BAD_REQUEST && isInvalidGrant(response.getBody())) {
            logger.warn(
                    "KeycloakIdentityProviderGateway.createSession - Invalid credentials {}",
                    Map.of("document_number", input.documentNumber())
            );
            throw new InvalidCredentialsException();
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            logger.error(
                    "KeycloakIdentityProviderGateway.createSession - Unexpected status {}",
                    Map.of("status", response.getStatusCode().value())
            );
            throw new SessionException();
        }

        KeycloakTokenResponse body = response.getBody();
        if (body == null || body.accessToken() == null) {
            logger.error("KeycloakIdentityProviderGateway.createSession - Token response without access token");
            throw new SessionException();
        }

        if (body.refreshToken() == null) {
            logger.error("KeycloakIdentityProviderGateway.createSession - Token response without refresh token");
            throw new SessionException();
        }

        if (body.expiresIn() == null) {
            logger.error("KeycloakIdentityProviderGateway.createSession - Token response without expiration");
            throw new SessionException();
        }

        if (body.refreshExpiresIn() == null) {
            logger.error("KeycloakIdentityProviderGateway.createSession - Token response without refresh expiration");
            throw new SessionException();
        }

        return new SessionOutputDTO(
                body.accessToken(),
                body.expiresIn(),
                body.refreshToken(),
                body.refreshExpiresIn(),
                body.tokenType()
        );
    }

    @Override
    public SessionOutputDTO refreshSession(RefreshSessionInputDTO input) {
        logger.info("KeycloakIdentityProviderGateway.refreshSession - Refreshing session");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", this.clientId);
        form.add("client_secret", this.clientSecret);
        form.add("refresh_token", input.refreshToken());

        ResponseEntity<KeycloakTokenResponse> response;
        try {
            response = this.restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", this.realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, errorResponse) -> {})
                    .toEntity(KeycloakTokenResponse.class);
        } catch (RestClientException ex) {
            logger.error("KeycloakIdentityProviderGateway.refreshSession - Token request failed", ex);
            throw new SessionException();
        }

        if (response.getStatusCode() == HttpStatus.BAD_REQUEST && isInvalidGrant(response.getBody())) {
            logger.warn("KeycloakIdentityProviderGateway.refreshSession - Invalid refresh token");
            throw new InvalidRefreshTokenException();
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            logger.error(
                    "KeycloakIdentityProviderGateway.refreshSession - Unexpected status {}",
                    Map.of("status", response.getStatusCode().value())
            );
            throw new SessionException();
        }

        KeycloakTokenResponse body = response.getBody();
        if (body == null || body.accessToken() == null) {
            logger.error("KeycloakIdentityProviderGateway.refreshSession - Token response without access token");
            throw new SessionException();
        }

        if (body.refreshToken() == null) {
            logger.error("KeycloakIdentityProviderGateway.refreshSession - Token response without refresh token");
            throw new SessionException();
        }

        if (body.expiresIn() == null) {
            logger.error("KeycloakIdentityProviderGateway.refreshSession - Token response without expiration");
            throw new SessionException();
        }

        if (body.refreshExpiresIn() == null) {
            logger.error("KeycloakIdentityProviderGateway.refreshSession - Token response without refresh expiration");
            throw new SessionException();
        }

        return new SessionOutputDTO(
                body.accessToken(),
                body.expiresIn(),
                body.refreshToken(),
                body.refreshExpiresIn(),
                body.tokenType()
        );
    }

    @Override
    public void deleteSession(DeleteSessionInputDTO input) {
        logger.info("KeycloakIdentityProviderGateway.deleteSession - Deleting session");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", this.clientId);
        form.add("client_secret", this.clientSecret);
        form.add("refresh_token", input.refreshToken());

        ResponseEntity<KeycloakTokenResponse> response;
        try {
            response = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/logout", this.realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, errorResponse) -> {})
                    .toEntity(KeycloakTokenResponse.class);
        } catch (RestClientException ex) {
            logger.error("KeycloakIdentityProviderGateway.deleteSession - Logout request failed", ex);
            throw new SessionException();
        }

        if (response.getStatusCode() == HttpStatus.BAD_REQUEST && isInvalidGrant(response.getBody())) {
            logger.warn("KeycloakIdentityProviderGateway.deleteSession - Invalid refresh token");
            throw new InvalidRefreshTokenException();
        }

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            logger.error(
                    "KeycloakIdentityProviderGateway.deleteSession - Unexpected status {}",
                    Map.of("status", response.getStatusCode().value())
            );
            throw new SessionException();
        }
    }
}
