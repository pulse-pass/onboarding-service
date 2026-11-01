package com.pulsepass.onboardingservice.unit.api.validators;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.onboardingservice.api.validators.InternalRequestValidator;
import com.pulsepass.onboardingservice.domain.exceptions.MissingSelectedAgentKeyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class InternalRequestValidatorTest {

    private final InternalRequestValidator validator = new InternalRequestValidator();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void validateSelectedAgentKey_nullOrBlank_throwsMissingSelectedAgentKey(String selectedAgentKey) {
        assertThatThrownBy(() -> validator.validateSelectedAgentKey(selectedAgentKey))
                .isInstanceOf(MissingSelectedAgentKeyException.class);
    }

    @Test
    void validateSelectedAgentKey_validValue_doesNotThrow() {
        assertThatCode(() -> validator.validateSelectedAgentKey("legal-key")).doesNotThrowAnyException();
    }
}
