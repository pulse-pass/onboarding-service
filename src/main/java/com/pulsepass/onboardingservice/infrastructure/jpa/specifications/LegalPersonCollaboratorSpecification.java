package com.pulsepass.onboardingservice.infrastructure.jpa.specifications;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import org.springframework.data.jpa.domain.Specification;

public final class LegalPersonCollaboratorSpecification {

    private LegalPersonCollaboratorSpecification() {
    }

    public static Specification<LegalPersonCollaboratorEntity> withFilters(
            String legalPersonKey, CollaboratorRoleEnum collaboratorRole) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (legalPersonKey != null && !legalPersonKey.isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("legalPerson").get("person").get("personKey"), legalPersonKey)
                );
            }
            if (collaboratorRole != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(
                                root.get("collaboratorRole").get("enumerator"), collaboratorRole.name())
                );
            }
            return predicate;
        };
    }
}
