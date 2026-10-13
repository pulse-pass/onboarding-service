package com.pulsepass.onboardingservice.infrastructure.gateways.keycloak;

import com.pulsepass.onboardingservice.common.HttpClientHandlers;
import com.pulsepass.onboardingservice.domain.exceptions.UserCreationException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.IdentityProviderGateway;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateUserInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.UserRoleInputDTO;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakCreateUserRequest;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakCredentialRequest;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakRoleMappingRequest;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakRoleResponse;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakTokenResponse;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
            @Value("${app.http.connect-timeout}") Duration connectTimeout,
            @Value("${app.http.read-timeout}") Duration readTimeout
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(HttpClientHandlers.requestFactory(connectTimeout, readTimeout))
                .build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private String fetchAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", this.clientId);
        form.add("client_secret", this.clientSecret);

        KeycloakTokenResponse response = restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", this.realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KeycloakTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("Keycloak token response without access token");
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
                Map.of("person_key", List.of(input.personKey())),
                List.of(new KeycloakCredentialRequest("password", input.password(), false))
        );

        String accessToken = fetchAccessToken();
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
}
