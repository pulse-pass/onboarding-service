package com.pulsepass.onboardingservice.domain.entities;

import java.time.LocalDateTime;

public class LegalPerson {

    private Long id;
    private Person person;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LegalPerson() {
    }

    public LegalPerson(
            Person person
    ) {
        this.person = person;
    }

    public LegalPerson(
            Long id,
            Person person,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.person = person;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return this.person;
    }

    public void setPerson(Person person) {
        this.person = person;
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
}
