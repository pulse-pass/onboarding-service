package com.pulsepass.onboardingservice.unit.api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.onboardingservice.api.controllers.AuthController;
import com.pulsepass.onboardingservice.api.exceptions.GlobalExceptionHandler;
import com.pulsepass.onboardingservice.application.dtos.input.CreateSessionDTO;
import com.pulsepass.onboardingservice.application.dtos.input.DeleteSessionDTO;
import com.pulsepass.onboardingservice.application.dtos.input.RefreshSessionDTO;
import com.pulsepass.onboardingservice.application.services.AuthService;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidCredentialsException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidRefreshTokenException;
import com.pulsepass.onboardingservice.domain.exceptions.SessionException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.SessionOutputDTO;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.messages.basename=messages",
        "spring.messages.fallback-to-system-locale=false"
})
class AuthControllerTest {

    private static final String LOGIN_URL = "/onboarding/auth/login";
    private static final String REFRESH_URL = "/onboarding/auth/refresh";
    private static final String LOGOUT_URL = "/onboarding/auth/logout";
    private static final String LOGIN_BODY = "{\"document_number\": \"52998224725\", \"password\": \"Secret123\"}";
    private static final String TOKEN_BODY = "{\"refresh_token\": \"refresh-token\"}";
    private static final SessionOutputDTO SESSION =
            new SessionOutputDTO("access-token", 300, "refresh-token", 1800, "Bearer");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void createSession_validRequest_returns200WithSnakeCaseSession() throws Exception {
        when(authService.createSession(any(CreateSessionDTO.class))).thenReturn(SESSION);

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.expires_in").value(300))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"))
                .andExpect(jsonPath("$.refresh_expires_in").value(1800))
                .andExpect(jsonPath("$.token_type").value("Bearer"));

        ArgumentCaptor<CreateSessionDTO> captor = ArgumentCaptor.forClass(CreateSessionDTO.class);
        verify(authService).createSession(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new CreateSessionDTO("52998224725", "Secret123"));
    }

    static Stream<Arguments> invalidLoginBodies() {
        return Stream.of(
                Arguments.of("cpf missing", "{\"password\": \"Secret123\"}"),
                Arguments.of("cpf 10 digits", "{\"document_number\": \"5299822472\", \"password\": \"x\"}"),
                Arguments.of("cpf with letters", "{\"document_number\": \"5299822472A\", \"password\": \"x\"}"),
                Arguments.of("password missing", "{\"document_number\": \"52998224725\"}"),
                Arguments.of("password blank", "{\"document_number\": \"52998224725\", \"password\": \" \"}"),
                Arguments.of("malformed json", "{\"document_number\": "),
                Arguments.of("empty body", "")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidLoginBodies")
    void createSession_invalidBody_returns400WithBadRequestCode(String scenario, String content) throws Exception {
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"));

        verifyNoInteractions(authService);
    }

    @Test
    void createSession_serviceThrowsInvalidCredentials_returns401() throws Exception {
        when(authService.createSession(any(CreateSessionDTO.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"))
                .andExpect(jsonPath("$.message").value("Invalid CPF or password."));
    }

    @Test
    void createSession_serviceThrowsSessionException_returns422() throws Exception {
        when(authService.createSession(any(CreateSessionDTO.class))).thenThrow(new SessionException());

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("ONB00008"));
    }

    @Test
    void refreshSession_validRequest_returns200WithSnakeCaseSession() throws Exception {
        when(authService.refreshSession(any(RefreshSessionDTO.class))).thenReturn(SESSION);

        mockMvc.perform(post(REFRESH_URL).contentType(MediaType.APPLICATION_JSON).content(TOKEN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"));

        verify(authService).refreshSession(new RefreshSessionDTO("refresh-token"));
    }

    @Test
    void refreshSession_missingToken_returns400() throws Exception {
        mockMvc.perform(post(REFRESH_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"));

        verifyNoInteractions(authService);
    }

    @Test
    void refreshSession_serviceThrowsInvalidRefreshToken_returns401() throws Exception {
        when(authService.refreshSession(any(RefreshSessionDTO.class))).thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post(REFRESH_URL).contentType(MediaType.APPLICATION_JSON).content(TOKEN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"))
                .andExpect(jsonPath("$.message").value("Your session has expired. Please sign in again."));
    }

    @Test
    void deleteSession_validRequest_returns204WithoutBody() throws Exception {
        mockMvc.perform(post(LOGOUT_URL).contentType(MediaType.APPLICATION_JSON).content(TOKEN_BODY))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(authService).deleteSession(new DeleteSessionDTO("refresh-token"));
    }

    @Test
    void deleteSession_blankToken_returns400() throws Exception {
        mockMvc.perform(post(LOGOUT_URL).contentType(MediaType.APPLICATION_JSON).content("{\"refresh_token\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"));

        verifyNoInteractions(authService);
    }

    @Test
    void deleteSession_serviceThrowsInvalidRefreshToken_returns401() throws Exception {
        doThrow(new InvalidRefreshTokenException())
                .when(authService).deleteSession(any(DeleteSessionDTO.class));

        mockMvc.perform(post(LOGOUT_URL).contentType(MediaType.APPLICATION_JSON).content(TOKEN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));
    }
}
