package com.pulsepass.onboardingservice.api.validators;

import com.pulsepass.onboardingservice.domain.exceptions.MissingSelectedAgentKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InternalRequestValidator {

    private static final Logger logger = LoggerFactory.getLogger(InternalRequestValidator.class);

    public void validateSelectedAgentKey(String selectedAgentKey) {
        if (selectedAgentKey == null || selectedAgentKey.isBlank()) {
            logger.warn("InternalRequestValidator.validateSelectedAgentKey - Missing selected agent key");
            throw new MissingSelectedAgentKeyException();
        }
    }
}
