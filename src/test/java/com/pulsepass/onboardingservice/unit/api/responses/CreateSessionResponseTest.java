package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.CreateSessionResponse;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.SessionOutputDTO;
import org.junit.jupiter.api.Test;

class CreateSessionResponseTest {

    @Test
    void fromSessionOutput_session_mapsAllFields() {
        var response = CreateSessionResponse.fromSessionOutput(
                new SessionOutputDTO("access-secret", 300, "refresh-secret", 1800, "Bearer"));

        assertThat(response.accessToken()).isEqualTo("access-secret");
        assertThat(response.expiresIn()).isEqualTo(300);
        assertThat(response.refreshToken()).isEqualTo("refresh-secret");
        assertThat(response.refreshExpiresIn()).isEqualTo(1800);
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }
}
