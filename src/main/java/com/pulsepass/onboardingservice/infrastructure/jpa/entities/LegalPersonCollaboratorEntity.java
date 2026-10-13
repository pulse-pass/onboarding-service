package com.pulsepass.onboardingservice.infrastructure.jpa.entities;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "legal_person_collaborator")
public class LegalPersonCollaboratorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legal_person_collaborator_key", nullable = false, unique = true, length = 36)
    private String legalPersonCollaboratorKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_person_id", nullable = false)
    private LegalPersonEntity legalPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "natural_person_id", nullable = false)
    private NaturalPersonEntity naturalPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_role_id", nullable = false)
    private CollaboratorRoleEntity collaboratorRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_person_collaborator_status_id", nullable = false)
    private LegalPersonCollaboratorStatusEntity legalPersonCollaboratorStatus;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public LegalPersonCollaboratorEntity() {
    }

    public LegalPersonCollaboratorEntity(
            Long id,
            String legalPersonCollaboratorKey,
            LegalPersonEntity legalPerson,
            NaturalPersonEntity naturalPerson,
            CollaboratorRoleEntity collaboratorRole,
            LegalPersonCollaboratorStatusEntity legalPersonCollaboratorStatus,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.legalPersonCollaboratorKey = legalPersonCollaboratorKey;
        this.legalPerson = legalPerson;
        this.naturalPerson = naturalPerson;
        this.collaboratorRole = collaboratorRole;
        this.legalPersonCollaboratorStatus = legalPersonCollaboratorStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LegalPersonCollaboratorEntity fromDomain(LegalPersonCollaborator domain) {
        return new LegalPersonCollaboratorEntity(
                domain.getId(),
                domain.getLegalPersonCollaboratorKey(),
                LegalPersonEntity.fromDomain(domain.getLegalPerson()),
                NaturalPersonEntity.fromDomain(domain.getNaturalPerson()),
                CollaboratorRoleEntity.fromDomain(domain.getCollaboratorRole()),
                LegalPersonCollaboratorStatusEntity.fromDomain(domain.getLegalPersonCollaboratorStatus()),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public LegalPersonCollaborator toDomain() {
        return new LegalPersonCollaborator(
                this.id,
                this.legalPersonCollaboratorKey,
                this.legalPerson.toDomain(),
                this.naturalPerson.toDomain(),
                this.collaboratorRole.toDomain(),
                this.legalPersonCollaboratorStatus.toDomain(),
                this.createdAt,
                this.updatedAt
        );
    }
}
