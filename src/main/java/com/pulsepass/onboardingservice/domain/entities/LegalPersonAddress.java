package com.pulsepass.onboardingservice.domain.entities;

import java.time.LocalDateTime;

public class LegalPersonAddress {

    private Long id;
    private String legalPersonAddressKey;
    private String postalCode;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LegalPersonAddress() {
    }

    public LegalPersonAddress(
            String legalPersonAddressKey,
            String postalCode,
            String street,
            String number,
            String complement,
            String neighborhood
    ) {
        this.legalPersonAddressKey = legalPersonAddressKey;
        this.postalCode = postalCode;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
    }

    public LegalPersonAddress(
            Long id,
            String legalPersonAddressKey,
            String postalCode,
            String street,
            String number,
            String complement,
            String neighborhood,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.legalPersonAddressKey = legalPersonAddressKey;
        this.postalCode = postalCode;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLegalPersonAddressKey() {
        return this.legalPersonAddressKey;
    }

    public void setLegalPersonAddressKey(String legalPersonAddressKey) {
        this.legalPersonAddressKey = legalPersonAddressKey;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return this.complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return this.neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
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
