package com.pulsepass.onboardingservice.unit.infrastructure.jpa.specifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.specifications.LegalPersonCollaboratorSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class LegalPersonCollaboratorSpecificationTest {

    private Root<LegalPersonCollaboratorEntity> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder criteriaBuilder;
    private Predicate conjunction;
    private Path<Object> legalPersonPath;
    private Path<Object> personPath;
    private Path<Object> personKeyPath;
    private Path<Object> roleEnumeratorPath;

    @BeforeEach
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        criteriaBuilder = mock(CriteriaBuilder.class);
        conjunction = mock(Predicate.class);
        legalPersonPath = mock(Path.class);
        personPath = mock(Path.class);
        personKeyPath = mock(Path.class);
        roleEnumeratorPath = mock(Path.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);
        when(root.get("legalPerson")).thenReturn(legalPersonPath);
        when(legalPersonPath.get("person")).thenReturn(personPath);
        when(personPath.get("personKey")).thenReturn(personKeyPath);
        var rolePath = mock(Path.class);
        when(root.get("collaboratorRole")).thenReturn(rolePath);
        when(rolePath.get("enumerator")).thenReturn(roleEnumeratorPath);
    }

    @Test
    void withFilters_legalPersonKeyGiven_filtersByEqualOnLegalPersonPersonKey() {
        var equalPredicate = mock(Predicate.class);
        var combined = mock(Predicate.class);
        when(criteriaBuilder.equal(personKeyPath, "legal")).thenReturn(equalPredicate);
        when(criteriaBuilder.and(conjunction, equalPredicate)).thenReturn(combined);

        var result = LegalPersonCollaboratorSpecification.withFilters("legal", null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(combined);
        verify(criteriaBuilder).equal(personKeyPath, "legal");
    }

    @Test
    void withFilters_collaboratorRoleGiven_filtersByEqualOnRoleEnumerator() {
        var equalPredicate = mock(Predicate.class);
        var combined = mock(Predicate.class);
        when(criteriaBuilder.equal(roleEnumeratorPath, "OWNER")).thenReturn(equalPredicate);
        when(criteriaBuilder.and(conjunction, equalPredicate)).thenReturn(combined);

        var result = LegalPersonCollaboratorSpecification.withFilters(null, CollaboratorRoleEnum.OWNER)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(combined);
        verify(criteriaBuilder).equal(roleEnumeratorPath, "OWNER");
    }

    @Test
    void withFilters_bothGiven_combinesBothWithAnd() {
        var keyPredicate = mock(Predicate.class);
        var rolePredicate = mock(Predicate.class);
        var keyCombined = mock(Predicate.class);
        var bothCombined = mock(Predicate.class);
        when(criteriaBuilder.equal(personKeyPath, "legal")).thenReturn(keyPredicate);
        when(criteriaBuilder.equal(roleEnumeratorPath, "MEMBER")).thenReturn(rolePredicate);
        when(criteriaBuilder.and(conjunction, keyPredicate)).thenReturn(keyCombined);
        when(criteriaBuilder.and(keyCombined, rolePredicate)).thenReturn(bothCombined);

        var result = LegalPersonCollaboratorSpecification.withFilters("legal", CollaboratorRoleEnum.MEMBER)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(bothCombined);
    }

    @Test
    void withFilters_nullOrEmptyFilters_returnsConjunctionOnly() {
        var nullResult = LegalPersonCollaboratorSpecification.withFilters(null, null)
                .toPredicate(root, query, criteriaBuilder);
        var emptyResult = LegalPersonCollaboratorSpecification.withFilters("", null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(nullResult).isSameAs(conjunction);
        assertThat(emptyResult).isSameAs(conjunction);
        verify(criteriaBuilder, never()).equal(any(), any(Object.class));
    }
}
