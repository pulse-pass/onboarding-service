package com.pulsepass.onboardingservice.infrastructure.jpa.entities;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
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
@Table(name = "legal_person")
public class LegalPersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public LegalPersonEntity() {
    }

    public LegalPersonEntity(
            Long id,
            PersonEntity person,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.person = person;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LegalPersonEntity fromDomain(LegalPerson domain) {
        return new LegalPersonEntity(
                domain.getId(),
                PersonEntity.fromDomain(domain.getPerson()),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public LegalPerson toDomain() {
        return new LegalPerson(
                this.id,
                this.person.toDomain(),
                this.createdAt,
                this.updatedAt
        );
    }
}
