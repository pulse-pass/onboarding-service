package com.pulsepass.onboardingservice.domain.entities;

import java.time.LocalDateTime;

public class NaturalPerson {

    private Long id;
    private Person person;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NaturalPerson() {
    }

    public NaturalPerson(
            Person person,
            String email
    ) {
        this.person = person;
        this.email = email;
    }

    public NaturalPerson(
            Long id,
            Person person,
            String email,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.person = person;
        this.email = email;
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

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
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
