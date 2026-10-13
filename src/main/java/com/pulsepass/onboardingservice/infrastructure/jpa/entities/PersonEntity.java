package com.pulsepass.onboardingservice.infrastructure.jpa.entities;

import com.pulsepass.onboardingservice.domain.entities.Person;
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
@Table(name = "person")
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_key", nullable = false, unique = true, length = 36)
    private String personKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_type_id", nullable = false)
    private PersonTypeEntity personType;

    @Column(name = "document_number", nullable = false, unique = true, length = 14)
    private String documentNumber;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public PersonEntity() {
    }

    public PersonEntity(
            Long id,
            String personKey,
            PersonTypeEntity personType,
            String documentNumber,
            String name,
            String email,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.personKey = personKey;
        this.personType = personType;
        this.documentNumber = documentNumber;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PersonEntity fromDomain(Person domain) {
        return new PersonEntity(
                domain.getId(),
                domain.getPersonKey(),
                PersonTypeEntity.fromDomain(domain.getPersonType()),
                domain.getDocumentNumber(),
                domain.getName(),
                domain.getEmail(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public Person toDomain() {
        return new Person(
                this.id,
                this.personKey,
                this.personType.toDomain(),
                this.documentNumber,
                this.name,
                this.email,
                this.createdAt,
                this.updatedAt
        );
    }
}
