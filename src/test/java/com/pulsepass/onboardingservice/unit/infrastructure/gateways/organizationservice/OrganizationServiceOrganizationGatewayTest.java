package com.pulsepass.onboardingservice.unit.infrastructure.gateways.organizationservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.onboardingservice.domain.exceptions.OrganizationCreationException;
import com.pulsepass.onboardingservice.domain.gateways.organization.dtos.CreateOrganizationInputDTO;
import com.pulsepass.onboardingservice.infrastructure.gateways.organizationservice.OrganizationServiceOrganizationGateway;
import com.pulsepass.onboardingservice.unit.infrastructure.gateways.StubHttpServer;
import com.pulsepass.onboardingservice.unit.infrastructure.gateways.StubHttpServer.Response;
import java.net.http.HttpClient;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

class OrganizationServiceOrganizationGatewayTest {

    private static final String PATH = "/organization/internal/organizations";
    private static final String TOKEN = "internal-secret";
    private static final CreateOrganizationInputDTO INPUT =
            new CreateOrganizationInputDTO("legal-key", "12345678000195", "Acme Events");

    private final JsonMapper mapper = JsonMapper.builder().build();
    private StubHttpServer server;
    private OrganizationServiceOrganizationGateway gateway;

    @BeforeEach
    void setUp() throws Exception {
        server = new StubHttpServer();
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(300));
        gateway = new OrganizationServiceOrganizationGateway(
                server.url(), TOKEN, RestClient.builder().requestFactory(requestFactory));
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void createOrganization_created_returnsOrganizationKey() {
        server.on("POST", PATH, Response.of(201, "{\"organization_key\":\"org-key\"}"));

        var output = gateway.createOrganization(INPUT);

        assertThat(output.organizationKey()).isEqualTo("org-key");
    }

    @Test
    void createOrganization_created_sendsInternalTokenAndSnakeCaseBody() throws Exception {
        server.on("POST", PATH, Response.of(201, "{\"organization_key\":\"org-key\"}"));

        gateway.createOrganization(INPUT);

        assertThat(server.received()).hasSize(1);
        var request = server.received().get(0);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo(PATH);
        assertThat(request.headers().get("internal-token")).isEqualTo(TOKEN);
        assertThat(request.headers().get("content-type")).startsWith("application/json");
        var body = mapper.readTree(request.body());
        assertThat(body.size()).isEqualTo(3);
        assertThat(body.get("legal_person_key").asString()).isEqualTo("legal-key");
        assertThat(body.get("document_number").asString()).isEqualTo("12345678000195");
        assertThat(body.get("name").asString()).isEqualTo("Acme Events");
    }

    @Test
    void createOrganization_conflict_throwsOrganizationCreationException() {
        server.on("POST", PATH, Response.of(409, "{\"code\":\"ORG00001\"}"));

        assertThatThrownBy(() -> gateway.createOrganization(INPUT))
                .isInstanceOf(OrganizationCreationException.class);
    }

    @Test
    void createOrganization_serverError_throwsOrganizationCreationException() {
        server.on("POST", PATH, Response.of(500, "{\"code\":\"GEN00500\"}"));

        assertThatThrownBy(() -> gateway.createOrganization(INPUT))
                .isInstanceOf(OrganizationCreationException.class);
    }

    @Test
    void createOrganization_statusOkInsteadOfCreated_throwsOrganizationCreationException() {
        server.on("POST", PATH, Response.of(200, "{\"organization_key\":\"org-key\"}"));

        assertThatThrownBy(() -> gateway.createOrganization(INPUT))
                .isInstanceOf(OrganizationCreationException.class);
    }

    @Test
    void createOrganization_createdWithEmptyBody_throwsOrganizationCreationException() {
        server.on("POST", PATH, Response.empty(201));

        assertThatThrownBy(() -> gateway.createOrganization(INPUT))
                .isInstanceOf(OrganizationCreationException.class);
    }

    @Test
    void createOrganization_createdWithoutOrganizationKey_throwsOrganizationCreationException() {
        server.on("POST", PATH, Response.of(201, "{}"));

        assertThatThrownBy(() -> gateway.createOrganization(INPUT))
                .isInstanceOf(OrganizationCreationException.class);
    }

    @Test
    void createOrganization_readTimeoutExceeded_throwsOrganizationCreationException() {
        server.on("POST", PATH, Response.of(201, "{\"organization_key\":\"org-key\"}"));
        server.delay("POST", PATH, 1500);

        assertThatThrownBy(() -> gateway.createOrganization(INPUT))
                .isInstanceOf(OrganizationCreationException.class);
    }

    @Test
    void createOrganization_serverDown_throwsOrganizationCreationException() {
        server.close();

        assertThatThrownBy(() -> gateway.createOrganization(INPUT))
                .isInstanceOf(OrganizationCreationException.class);
    }
}
