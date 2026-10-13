package com.pulsepass.onboardingservice.infrastructure.jpa.entities;

import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
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
@Table(name = "person_phone_number")
public class PersonPhoneNumberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_phone_number_key", nullable = false, unique = true, length = 36)
    private String personPhoneNumberKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @Column(name = "country_code", nullable = false, length = 3)
    private String countryCode;

    @Column(name = "area_code", nullable = false, length = 2)
    private String areaCode;

    @Column(name = "number", nullable = false, length = 9)
    private String number;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public PersonPhoneNumberEntity() {
    }

    public PersonPhoneNumberEntity(
            Long id,
            String personPhoneNumberKey,
            PersonEntity person,
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

    public static PersonPhoneNumberEntity fromDomain(PersonPhoneNumber domain) {
        return new PersonPhoneNumberEntity(
                domain.getId(),
                domain.getPersonPhoneNumberKey(),
                PersonEntity.fromDomain(domain.getPerson()),
                domain.getCountryCode(),
                domain.getAreaCode(),
                domain.getNumber(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public PersonPhoneNumber toDomain() {
        return new PersonPhoneNumber(
                this.id,
                this.personPhoneNumberKey,
                this.person.toDomain(),
                this.countryCode,
                this.areaCode,
                this.number,
                this.createdAt,
                this.updatedAt
        );
    }
}
