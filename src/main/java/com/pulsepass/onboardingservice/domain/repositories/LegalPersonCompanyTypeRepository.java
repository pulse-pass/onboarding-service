package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import java.util.Optional;

public interface LegalPersonCompanyTypeRepository {

    Optional<LegalPersonCompanyType> findByEnumerator(String enumerator);
}
