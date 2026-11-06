package com.pulsepass.onboardingservice.infrastructure.jpa.entities;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "legal_person_address")
public class LegalPersonAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legal_person_address_key", nullable = false, unique = true, length = 36)
    private String legalPersonAddressKey;

    @Column(name = "postal_code", nullable = false, length = 8)
    private String postalCode;

    @Column(name = "street", nullable = false, length = 255)
    private String street;

    @Column(name = "number", nullable = false, length = 20)
    private String number;

    @Column(name = "complement", length = 255)
    private String complement;

    @Column(name = "neighborhood", nullable = false, length = 255)
    private String neighborhood;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public LegalPersonAddressEntity() {
    }

    public LegalPersonAddressEntity(
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

    public static LegalPersonAddressEntity fromDomain(LegalPersonAddress domain) {
        return new LegalPersonAddressEntity(
                domain.getId(),
                domain.getLegalPersonAddressKey(),
                domain.getPostalCode(),
                domain.getStreet(),
                domain.getNumber(),
                domain.getComplement(),
                domain.getNeighborhood(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public LegalPersonAddress toDomain() {
        return new LegalPersonAddress(
                this.id,
                this.legalPersonAddressKey,
                this.postalCode,
                this.street,
                this.number,
                this.complement,
                this.neighborhood,
                this.createdAt,
                this.updatedAt
        );
    }
}
