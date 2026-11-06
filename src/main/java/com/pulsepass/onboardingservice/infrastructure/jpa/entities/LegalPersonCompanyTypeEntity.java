package com.pulsepass.onboardingservice.infrastructure.jpa.entities;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "legal_person_company_type")
public class LegalPersonCompanyTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enumerator", nullable = false, unique = true, length = 50)
    private String enumerator;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public LegalPersonCompanyTypeEntity() {
    }

    public LegalPersonCompanyTypeEntity(Long id, String enumerator, LocalDateTime createdAt) {
        this.id = id;
        this.enumerator = enumerator;
        this.createdAt = createdAt;
    }

    public static LegalPersonCompanyTypeEntity fromDomain(LegalPersonCompanyType domain) {
        return new LegalPersonCompanyTypeEntity(domain.getId(), domain.getEnumerator(), domain.getCreatedAt());
    }

    public LegalPersonCompanyType toDomain() {
        return new LegalPersonCompanyType(this.id, this.enumerator, this.createdAt);
    }
}
