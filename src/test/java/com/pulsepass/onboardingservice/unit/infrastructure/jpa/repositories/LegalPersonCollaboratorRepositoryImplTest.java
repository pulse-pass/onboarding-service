package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaLegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.LegalPersonCollaboratorRepositoryImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

class LegalPersonCollaboratorRepositoryImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private JpaLegalPersonCollaboratorRepository jpaRepository;
    private LegalPersonCollaboratorRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaLegalPersonCollaboratorRepository.class);
        repository = new LegalPersonCollaboratorRepositoryImpl(jpaRepository);
    }

    @Test
    void save_domain_savesEntityAndReturnsMappedDomain() {
        var legal = new LegalPerson(3L, new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", NOW, NOW), "acme@mail.com",
                new LegalPersonAddress(4L, "address-key", "90110220", "Rua Ismael Chaves Barcelos", "60",
                        "Sala 2", "Praia de Belas", NOW, NOW),
                new LegalPersonCompanyType(5L, "LIMITED", NOW), new BigDecimal("25000.00"), NOW, NOW);
        var natural = new NaturalPerson(2L, new Person(2L, "owner-key", new PersonType(2L, "NATURAL", NOW),
                "52998224725", "John Doe", NOW, NOW), "john@mail.com", NOW, NOW);
        var collaborator = new LegalPersonCollaborator(4L, "collab-key", legal, natural,
                new CollaboratorRole(1L, "OWNER", NOW), new LegalPersonCollaboratorStatus(1L, "ACTIVE", NOW), NOW, NOW);
        var domain = collaborator;
        when(jpaRepository.save(any(LegalPersonCollaboratorEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = repository.save(domain);

        var captor = ArgumentCaptor.forClass(LegalPersonCollaboratorEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain()).usingRecursiveComparison().isEqualTo(domain);
        assertThat(saved).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void existsByLegalPersonKeyAndNaturalPersonKey_linked_returnsTrue() {
        when(jpaRepository.existsByLegalPersonPersonPersonKeyAndNaturalPersonPersonPersonKey(
                "legal-key", "natural-key")).thenReturn(true);

        assertThat(repository.existsByLegalPersonKeyAndNaturalPersonKey("legal-key", "natural-key")).isTrue();
    }

    @Test
    void existsByLegalPersonKeyAndNaturalPersonKey_notLinked_returnsFalse() {
        when(jpaRepository.existsByLegalPersonPersonPersonKeyAndNaturalPersonPersonPersonKey(
                "legal-key", "natural-key")).thenReturn(false);

        assertThat(repository.existsByLegalPersonKeyAndNaturalPersonKey("legal-key", "natural-key")).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_existingPage_returnsMappedDomainsUsingZeroBasedPageable() {
        var legal = new LegalPerson(3L, new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", NOW, NOW), "acme@mail.com",
                new LegalPersonAddress(4L, "address-key", "90110220", "Rua Ismael Chaves Barcelos", "60",
                        "Sala 2", "Praia de Belas", NOW, NOW),
                new LegalPersonCompanyType(5L, "LIMITED", NOW), new BigDecimal("25000.00"), NOW, NOW);
        var natural = new NaturalPerson(2L, new Person(2L, "owner-key", new PersonType(2L, "NATURAL", NOW),
                "52998224725", "John Doe", NOW, NOW), "john@mail.com", NOW, NOW);
        var domain = new LegalPersonCollaborator(4L, "collab-key", legal, natural,
                new CollaboratorRole(1L, "OWNER", NOW), new LegalPersonCollaboratorStatus(1L, "ACTIVE", NOW), NOW, NOW);
        Page<LegalPersonCollaboratorEntity> page =
                new PageImpl<>(List.of(LegalPersonCollaboratorEntity.fromDomain(domain)));
        when(jpaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = repository.findAll("person-key", CollaboratorRoleEnum.OWNER, 3, 10);

        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(captor.getValue().getSort()).containsExactly(
                Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_emptyPage_returnsEmptyList() {
        when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<LegalPersonCollaboratorEntity>(List.of()));

        assertThat(repository.findAll("person-key", null, 1, 30)).isEmpty();
    }
}
