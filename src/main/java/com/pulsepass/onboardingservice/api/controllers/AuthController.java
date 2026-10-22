package com.pulsepass.onboardingservice.api.controllers;

import com.pulsepass.onboardingservice.api.responses.SessionPersonResponse;
import com.pulsepass.onboardingservice.api.responses.SessionResponse;
import com.pulsepass.onboardingservice.api.schemas.CreateSessionSchema;
import com.pulsepass.onboardingservice.api.schemas.DeleteSessionSchema;
import com.pulsepass.onboardingservice.api.schemas.RefreshSessionSchema;
import com.pulsepass.onboardingservice.application.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/onboarding/auth/login")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionSchema request) {
        var session = authService.createSession(request.toDTO());
        return ResponseEntity.ok(SessionResponse.fromSessionOutput(session));
    }

    @PostMapping("/onboarding/auth/refresh")
    public ResponseEntity<SessionResponse> refreshSession(@Valid @RequestBody RefreshSessionSchema request) {
        var session = authService.refreshSession(request.toDTO());
        return ResponseEntity.ok(SessionResponse.fromSessionOutput(session));
    }

    @PostMapping("/onboarding/auth/logout")
    public ResponseEntity<Void> deleteSession(@Valid @RequestBody DeleteSessionSchema request) {
        authService.deleteSession(request.toDTO());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/onboarding/auth/me")
    public ResponseEntity<SessionPersonResponse> getSession(
            @RequestHeader("selected-user-key") String selectedUserKey,
            @RequestHeader(value = "selected-agent-key", required = false) String selectedAgentKey,
            @RequestHeader("selected-user-roles") String selectedUserRoles
    ) {
        var session = authService.getSession(selectedUserKey, selectedAgentKey, selectedUserRoles);
        return ResponseEntity.ok(SessionPersonResponse.fromDTO(session));
    }
}
