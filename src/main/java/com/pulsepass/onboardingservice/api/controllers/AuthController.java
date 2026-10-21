package com.pulsepass.onboardingservice.api.controllers;

import com.pulsepass.onboardingservice.api.responses.SessionResponse;
import com.pulsepass.onboardingservice.api.schemas.CreateSessionSchema;
import com.pulsepass.onboardingservice.api.schemas.DeleteSessionSchema;
import com.pulsepass.onboardingservice.api.schemas.RefreshSessionSchema;
import com.pulsepass.onboardingservice.application.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
}
