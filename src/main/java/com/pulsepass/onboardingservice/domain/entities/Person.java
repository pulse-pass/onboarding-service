package com.pulsepass.onboardingservice.domain.entities;

import java.time.LocalDateTime;

public class Person {

    private Long id;
    private String personKey;
    private PersonType personType;
    private String documentNumber;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Person() {
    }

    public Person(
            String personKey,
            PersonType personType,
            String documentNumber,
            String name
    ) {
        this.personKey = personKey;
        this.personType = personType;
        this.documentNumber = documentNumber;
        this.name = name;
    }

    public Person(
            Long id,
            String personKey,
            PersonType personType,
            String documentNumber,
            String name,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.personKey = personKey;
        this.personType = personType;
        this.documentNumber = documentNumber;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPersonKey() {
        return this.personKey;
    }

    public void setPersonKey(String personKey) {
        this.personKey = personKey;
    }

    public PersonType getPersonType() {
        return this.personType;
    }

    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }

    public String getDocumentNumber() {
        return this.documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
