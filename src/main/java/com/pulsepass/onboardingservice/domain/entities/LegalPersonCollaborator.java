package com.pulsepass.onboardingservice.domain.entities;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class LegalPersonCollaborator {

    private Long id;
    private String legalPersonCollaboratorKey;
    private LegalPerson legalPerson;
    private NaturalPerson naturalPerson;
    private CollaboratorRole collaboratorRole;
    private LegalPersonCollaboratorStatus legalPersonCollaboratorStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LegalPersonCollaborator() {
    }

    public LegalPersonCollaborator(
            String legalPersonCollaboratorKey,
            LegalPerson legalPerson,
            NaturalPerson naturalPerson,
            CollaboratorRole collaboratorRole,
            LegalPersonCollaboratorStatus legalPersonCollaboratorStatus
    ) {
        this.legalPersonCollaboratorKey = legalPersonCollaboratorKey;
        this.legalPerson = legalPerson;
        this.naturalPerson = naturalPerson;
        this.collaboratorRole = collaboratorRole;
        this.legalPersonCollaboratorStatus = legalPersonCollaboratorStatus;
    }

    public LegalPersonCollaborator(
            Long id,
            String legalPersonCollaboratorKey,
            LegalPerson legalPerson,
            NaturalPerson naturalPerson,
            CollaboratorRole collaboratorRole,
            LegalPersonCollaboratorStatus legalPersonCollaboratorStatus,
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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLegalPersonCollaboratorKey() {
        return this.legalPersonCollaboratorKey;
    }

    public void setLegalPersonCollaboratorKey(String legalPersonCollaboratorKey) {
        this.legalPersonCollaboratorKey = legalPersonCollaboratorKey;
    }

    public LegalPerson getLegalPerson() {
        return this.legalPerson;
    }

    public void setLegalPerson(LegalPerson legalPerson) {
        this.legalPerson = legalPerson;
    }

    public NaturalPerson getNaturalPerson() {
        return this.naturalPerson;
    }

    public void setNaturalPerson(NaturalPerson naturalPerson) {
        this.naturalPerson = naturalPerson;
    }

    public CollaboratorRole getCollaboratorRole() {
        return this.collaboratorRole;
    }

    public void setCollaboratorRole(CollaboratorRole collaboratorRole) {
        this.collaboratorRole = collaboratorRole;
    }

    public LegalPersonCollaboratorStatus getLegalPersonCollaboratorStatus() {
        return this.legalPersonCollaboratorStatus;
    }

    public void setLegalPersonCollaboratorStatus(LegalPersonCollaboratorStatus legalPersonCollaboratorStatus) {
        this.legalPersonCollaboratorStatus = legalPersonCollaboratorStatus;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    public Map<String, Object> toHistoryData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("legal_person_collaborator_key", this.legalPersonCollaboratorKey);
        data.put("legal_person_key", this.legalPerson.getPerson().getPersonKey());
        data.put("natural_person_key", this.naturalPerson.getPerson().getPersonKey());
        data.put("collaborator_role", this.collaboratorRole.getEnumerator());
        data.put("legal_person_collaborator_status", this.legalPersonCollaboratorStatus.getEnumerator());
        return data;
    }
}
