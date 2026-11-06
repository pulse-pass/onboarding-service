package com.pulsepass.onboardingservice.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LegalPerson {

    private Long id;
    private Person person;
    private String email;
    private LegalPersonAddress legalPersonAddress;
    private LegalPersonCompanyType legalPersonCompanyType;
    private BigDecimal monthlyRevenueAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LegalPerson() {
    }

    public LegalPerson(
            Person person,
            String email,
            LegalPersonAddress legalPersonAddress,
            LegalPersonCompanyType legalPersonCompanyType,
            BigDecimal monthlyRevenueAmount
    ) {
        this.person = person;
        this.email = email;
        this.legalPersonAddress = legalPersonAddress;
        this.legalPersonCompanyType = legalPersonCompanyType;
        this.monthlyRevenueAmount = monthlyRevenueAmount;
    }

    public LegalPerson(
            Long id,
            Person person,
            String email,
            LegalPersonAddress legalPersonAddress,
            LegalPersonCompanyType legalPersonCompanyType,
            BigDecimal monthlyRevenueAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.person = person;
        this.email = email;
        this.legalPersonAddress = legalPersonAddress;
        this.legalPersonCompanyType = legalPersonCompanyType;
        this.monthlyRevenueAmount = monthlyRevenueAmount;
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

    public LegalPersonAddress getLegalPersonAddress() {
        return this.legalPersonAddress;
    }

    public void setLegalPersonAddress(LegalPersonAddress legalPersonAddress) {
        this.legalPersonAddress = legalPersonAddress;
    }

    public LegalPersonCompanyType getLegalPersonCompanyType() {
        return this.legalPersonCompanyType;
    }

    public void setLegalPersonCompanyType(LegalPersonCompanyType legalPersonCompanyType) {
        this.legalPersonCompanyType = legalPersonCompanyType;
    }

    public BigDecimal getMonthlyRevenueAmount() {
        return this.monthlyRevenueAmount;
    }

    public void setMonthlyRevenueAmount(BigDecimal monthlyRevenueAmount) {
        this.monthlyRevenueAmount = monthlyRevenueAmount;
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
