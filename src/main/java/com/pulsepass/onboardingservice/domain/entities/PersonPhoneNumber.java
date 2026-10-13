package com.pulsepass.onboardingservice.domain.entities;

import java.time.LocalDateTime;

public class PersonPhoneNumber {

    private Long id;
    private String personPhoneNumberKey;
    private Person person;
    private String countryCode;
    private String areaCode;
    private String number;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PersonPhoneNumber() {
    }

    public PersonPhoneNumber(
            String personPhoneNumberKey,
            Person person,
            String countryCode,
            String areaCode,
            String number
    ) {
        this.personPhoneNumberKey = personPhoneNumberKey;
        this.person = person;
        this.countryCode = countryCode;
        this.areaCode = areaCode;
        this.number = number;
    }

    public PersonPhoneNumber(
            Long id,
            String personPhoneNumberKey,
            Person person,
            String countryCode,
            String areaCode,
            String number,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.personPhoneNumberKey = personPhoneNumberKey;
        this.person = person;
        this.countryCode = countryCode;
        this.areaCode = areaCode;
        this.number = number;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPersonPhoneNumberKey() {
        return this.personPhoneNumberKey;
    }

    public void setPersonPhoneNumberKey(String personPhoneNumberKey) {
        this.personPhoneNumberKey = personPhoneNumberKey;
    }

    public Person getPerson() {
        return this.person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAreaCode() {
        return this.areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
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
