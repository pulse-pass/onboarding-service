package com.pulsepass.onboardingservice.domain.entities;

import com.pulsepass.onboardingservice.common.JsonHandlers;
import java.time.LocalDateTime;
import java.util.UUID;

public class LegalPersonCollaboratorHistory {

    private Long id;
    private String legalPersonCollaboratorHistoryKey;
    private LegalPersonCollaborator legalPersonCollaborator;
    private LegalPersonCollaboratorStatus oldStatus;
    private LegalPersonCollaboratorStatus newStatus;
    private String historyData;
    private LocalDateTime createdAt;

    public LegalPersonCollaboratorHistory() {
    }

    public LegalPersonCollaboratorHistory(
            String legalPersonCollaboratorHistoryKey,
            LegalPersonCollaborator legalPersonCollaborator,
            LegalPersonCollaboratorStatus oldStatus,
            LegalPersonCollaboratorStatus newStatus,
            String historyData
    ) {
        this.legalPersonCollaboratorHistoryKey = legalPersonCollaboratorHistoryKey;
        this.legalPersonCollaborator = legalPersonCollaborator;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.historyData = historyData;
    }

    public LegalPersonCollaboratorHistory(
            Long id,
            String legalPersonCollaboratorHistoryKey,
            LegalPersonCollaborator legalPersonCollaborator,
            LegalPersonCollaboratorStatus oldStatus,
            LegalPersonCollaboratorStatus newStatus,
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

    public static LegalPersonCollaboratorHistory fromLegalPersonCollaborator(
            LegalPersonCollaborator legalPersonCollaborator,
            LegalPersonCollaboratorStatus oldStatus
    ) {
        return new LegalPersonCollaboratorHistory(
                UUID.randomUUID().toString(),
                legalPersonCollaborator,
                oldStatus,
                legalPersonCollaborator.getLegalPersonCollaboratorStatus(),
                JsonHandlers.toJson(legalPersonCollaborator.toHistoryData())
        );
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLegalPersonCollaboratorHistoryKey() {
        return this.legalPersonCollaboratorHistoryKey;
    }

    public void setLegalPersonCollaboratorHistoryKey(String legalPersonCollaboratorHistoryKey) {
        this.legalPersonCollaboratorHistoryKey = legalPersonCollaboratorHistoryKey;
    }

    public LegalPersonCollaborator getLegalPersonCollaborator() {
        return this.legalPersonCollaborator;
    }

    public void setLegalPersonCollaborator(LegalPersonCollaborator legalPersonCollaborator) {
        this.legalPersonCollaborator = legalPersonCollaborator;
    }

    public LegalPersonCollaboratorStatus getOldStatus() {
        return this.oldStatus;
    }

    public void setOldStatus(LegalPersonCollaboratorStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public LegalPersonCollaboratorStatus getNewStatus() {
        return this.newStatus;
    }

    public void setNewStatus(LegalPersonCollaboratorStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getHistoryData() {
        return this.historyData;
    }

    public void setHistoryData(String historyData) {
        this.historyData = historyData;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
