package com.pulsepass.onboardingservice.infrastructure.gateways.organizationservice;

import com.pulsepass.onboardingservice.domain.exceptions.OrganizationCreationException;
import com.pulsepass.onboardingservice.domain.gateways.organization.OrganizationGateway;
import com.pulsepass.onboardingservice.domain.gateways.organization.dtos.CreateOrganizationInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.organization.dtos.CreateOrganizationOutputDTO;
import com.pulsepass.onboardingservice.infrastructure.gateways.organizationservice.dtos.OrganizationServiceCreateOrganizationRequest;
import com.pulsepass.onboardingservice.infrastructure.gateways.organizationservice.dtos.OrganizationServiceCreateOrganizationResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OrganizationServiceOrganizationGateway implements OrganizationGateway {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceOrganizationGateway.class);

    private final RestClient restClient;
    private final String internalToken;

    public OrganizationServiceOrganizationGateway(
            @Value("${app.organization-service.url}") String url,
            @Value("${app.internal-token}") String internalToken,
            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder
                .baseUrl(url)
                .build();
        this.internalToken = internalToken;
    }

    @Override
    public CreateOrganizationOutputDTO createOrganization(CreateOrganizationInputDTO input) {
        logger.info(
                "OrganizationServiceOrganizationGateway.createOrganization - Creating organization {}",
                Map.of("legal_person_key", input.legalPersonKey())
        );

        var request = new OrganizationServiceCreateOrganizationRequest(
                input.legalPersonKey(),
                input.documentNumber(),
                input.name()
        );

        ResponseEntity<OrganizationServiceCreateOrganizationResponse> response;
        try {
            response = restClient.post()
                    .uri("/organization/internal/organizations")
                    .header("internal-token", this.internalToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(OrganizationServiceCreateOrganizationResponse.class);
        } catch (RestClientException ex) {
            logger.error(
                    "OrganizationServiceOrganizationGateway.createOrganization - Request failed {}",
                    Map.of("legal_person_key", input.legalPersonKey()),
                    ex
            );
            throw new OrganizationCreationException();
        }

        if (response.getStatusCode() != HttpStatus.CREATED) {
            logger.error(
                    "OrganizationServiceOrganizationGateway.createOrganization - Unexpected status {}",
                    Map.of("step", "create_organization", "status", response.getStatusCode().value())
            );
            throw new OrganizationCreationException();
        }
        if (response.getBody() == null || response.getBody().organizationKey() == null) {
            logger.error(
                    "OrganizationServiceOrganizationGateway.createOrganization - Response without organization key {}",
                    Map.of("step", "create_organization", "status", response.getStatusCode().value())
            );
            throw new OrganizationCreationException();
        }

        return new CreateOrganizationOutputDTO(response.getBody().organizationKey());
    }
}
