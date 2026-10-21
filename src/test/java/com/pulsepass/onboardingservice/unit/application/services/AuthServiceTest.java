package com.pulsepass.onboardingservice.unit.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.application.dtos.input.CreateSessionDTO;
import com.pulsepass.onboardingservice.application.dtos.input.DeleteSessionDTO;
import com.pulsepass.onboardingservice.application.dtos.input.RefreshSessionDTO;
import com.pulsepass.onboardingservice.application.services.AuthService;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidCredentialsException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidRefreshTokenException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.IdentityProviderGateway;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.DeleteSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.RefreshSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.SessionOutputDTO;
import com.pulsepass.onboardingservice.domain.repositories.PersonRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final SessionOutputDTO SESSION = new SessionOutputDTO("access", 300, "refresh", 1800, "Bearer");

    @Mock
    private PersonRepository personRepository;

    @Mock
    private IdentityProviderGateway identityProviderGateway;

    @Mock
    private Person person;

    @InjectMocks
    private AuthService service;

    @Test
    void createSession_validCredentials_returnsGatewaySession() {
        when(personRepository.findByDocumentNumber("52998224725")).thenReturn(Optional.of(person));
        when(identityProviderGateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .thenReturn(SESSION);

        var result = service.createSession(new CreateSessionDTO("52998224725", "Secret123"));

        assertThat(result).isEqualTo(SESSION);
    }

    @Test
    void createSession_gatewayRejectsCredentials_propagatesException() {
        when(personRepository.findByDocumentNumber("52998224725")).thenReturn(Optional.of(person));
        when(identityProviderGateway.createSession(new CreateSessionInputDTO("52998224725", "wrong")))
                .thenThrow(new InvalidCredentialsException());

        assertThatThrownBy(() -> service.createSession(new CreateSessionDTO("52998224725", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void createSession_personNotFound_throwsInvalidCredentialsWithoutCallingGateway() {
        when(personRepository.findByDocumentNumber("52998224725")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSession(new CreateSessionDTO("52998224725", "Secret123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(identityProviderGateway, never()).createSession(any());
    }

    @Test
    void refreshSession_validToken_returnsGatewaySession() {
        when(identityProviderGateway.refreshSession(new RefreshSessionInputDTO("refresh"))).thenReturn(SESSION);

        var result = service.refreshSession(new RefreshSessionDTO("refresh"));

        assertThat(result).isEqualTo(SESSION);
    }

    @Test
    void refreshSession_gatewayRejectsToken_propagatesException() {
        when(identityProviderGateway.refreshSession(new RefreshSessionInputDTO("expired")))
                .thenThrow(new InvalidRefreshTokenException());

        assertThatThrownBy(() -> service.refreshSession(new RefreshSessionDTO("expired")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void deleteSession_validToken_callsGateway() {
        service.deleteSession(new DeleteSessionDTO("refresh"));

        verify(identityProviderGateway).deleteSession(new DeleteSessionInputDTO("refresh"));
    }

    @Test
    void deleteSession_gatewayRejectsToken_propagatesException() {
        doThrow(new InvalidRefreshTokenException())
                .when(identityProviderGateway).deleteSession(new DeleteSessionInputDTO("expired"));

        assertThatThrownBy(() -> service.deleteSession(new DeleteSessionDTO("expired")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
