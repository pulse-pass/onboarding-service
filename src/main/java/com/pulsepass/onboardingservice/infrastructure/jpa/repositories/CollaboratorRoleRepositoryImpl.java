package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.repositories.CollaboratorRoleRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.CollaboratorRoleEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CollaboratorRoleRepositoryImpl implements CollaboratorRoleRepository {

    private final JpaCollaboratorRoleRepository jpaRepository;

    public CollaboratorRoleRepositoryImpl(JpaCollaboratorRoleRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CollaboratorRole> findByEnumerator(String enumerator) {
        return jpaRepository.findByEnumerator(enumerator).map(CollaboratorRoleEntity::toDomain);
    }
}
