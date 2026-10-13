package com.pulsepass.onboardingservice.infrastructure.jpa.entities;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorHistory;
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
@Table(name = "legal_person_collaborator_history")
public class LegalPersonCollaboratorHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legal_person_collaborator_history_key", nullable = false, unique = true, length = 36)
    private String legalPersonCollaboratorHistoryKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_person_collaborator_id", nullable = false)
    private LegalPersonCollaboratorEntity legalPersonCollaborator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_status_id")
    private LegalPersonCollaboratorStatusEntity oldStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_status_id", nullable = false)
    private LegalPersonCollaboratorStatusEntity newStatus;

    @Column(name = "history_data", nullable = false, columnDefinition = "json")
    private String historyData;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public LegalPersonCollaboratorHistoryEntity() {
    }

    public LegalPersonCollaboratorHistoryEntity(
            Long id,
            String legalPersonCollaboratorHistoryKey,
            LegalPersonCollaboratorEntity legalPersonCollaborator,
            LegalPersonCollaboratorStatusEntity oldStatus,
            LegalPersonCollaboratorStatusEntity newStatus,
            String historyData,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.legalPersonCollaboratorHistoryKey = legalPersonCollaboratorHistoryKey;
        this.legalPersonCollaborator = legalPersonCollaborator;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.historyData = historyData;
        this.createdAt = createdAt;
    }

    public static LegalPersonCollaboratorHistoryEntity fromDomain(LegalPersonCollaboratorHistory domain) {
        var oldStatus = domain.getOldStatus();
        return new LegalPersonCollaboratorHistoryEntity(
                domain.getId(),
                domain.getLegalPersonCollaboratorHistoryKey(),
                LegalPersonCollaboratorEntity.fromDomain(domain.getLegalPersonCollaborator()),
                oldStatus == null ? null : LegalPersonCollaboratorStatusEntity.fromDomain(oldStatus),
                LegalPersonCollaboratorStatusEntity.fromDomain(domain.getNewStatus()),
                domain.getHistoryData(),
                domain.getCreatedAt()
        );
    }

    public LegalPersonCollaboratorHistory toDomain() {
        return new LegalPersonCollaboratorHistory(
                this.id,
                this.legalPersonCollaboratorHistoryKey,
                this.legalPersonCollaborator.toDomain(),
                this.oldStatus == null ? null : this.oldStatus.toDomain(),
                this.newStatus.toDomain(),
                this.historyData,
                this.createdAt
        );
    }
}
