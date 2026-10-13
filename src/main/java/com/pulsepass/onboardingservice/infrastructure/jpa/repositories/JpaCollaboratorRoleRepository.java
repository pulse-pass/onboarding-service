package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.CollaboratorRoleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCollaboratorRoleRepository extends JpaRepository<CollaboratorRoleEntity, Long> {

    Optional<CollaboratorRoleEntity> findByEnumerator(String enumerator);
}
