package com.pulsepass.onboardingservice.infrastructure.jpa.entities;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "collaborator_role")
public class CollaboratorRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enumerator", nullable = false, unique = true, length = 50)
    private String enumerator;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public CollaboratorRoleEntity() {
    }

    public CollaboratorRoleEntity(Long id, String enumerator, LocalDateTime createdAt) {
        this.id = id;
        this.enumerator = enumerator;
        this.createdAt = createdAt;
    }

    public static CollaboratorRoleEntity fromDomain(CollaboratorRole domain) {
        return new CollaboratorRoleEntity(domain.getId(), domain.getEnumerator(), domain.getCreatedAt());
    }

    public CollaboratorRole toDomain() {
        return new CollaboratorRole(this.id, this.enumerator, this.createdAt);
    }
}
