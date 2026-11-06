package com.pulsepass.onboardingservice.unit.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.application.dtos.output.GetInternalPersonDTO;
import com.pulsepass.onboardingservice.application.dtos.output.GetPersonDTO;
import com.pulsepass.onboardingservice.application.services.PersonService;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.domain.exceptions.PersonNotFoundException;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonRepository;
import com.pulsepass.onboardingservice.domain.repositories.NaturalPersonRepository;
import com.pulsepass.onboardingservice.domain.repositories.PersonPhoneNumberRepository;
import com.pulsepass.onboardingservice.domain.repositories.PersonRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private NaturalPersonRepository naturalPersonRepository;
    @Mock
    private LegalPersonRepository legalPersonRepository;
    @Mock
    private PersonPhoneNumberRepository personPhoneNumberRepository;

    private PersonService service;

    private final PersonType legalType = new PersonType(1L, "LEGAL", null);
    private final PersonType naturalType = new PersonType(2L, "NATURAL", null);

    private final Person company = new Person("legal-1", legalType, "12345678000195", "One");
    private final Person user = new Person("user-1", naturalType, "11144477735", "User One");
    private final LegalPersonAddress address = new LegalPersonAddress(
            "address-1", "90110220", "Rua A", "60", null, "Centro");
    private final LegalPersonCompanyType limitedCompanyType = new LegalPersonCompanyType(1L, "LIMITED", null);
    private final LegalPerson legalPerson = new LegalPerson(
            company, "one@mail.com", address, limitedCompanyType, new BigDecimal("25000.00"));
    private final NaturalPerson naturalPerson = new NaturalPerson(user, "u1@mail.com");

    @BeforeEach
    void setUp() {
        service = new PersonService(
                personRepository,
                naturalPersonRepository,
                legalPersonRepository,
                personPhoneNumberRepository
        );
    }

    private void stubLegalPerson() {
        when(personRepository.findByPersonKey("legal-1")).thenReturn(Optional.of(company));
        when(legalPersonRepository.findByPersonKey("legal-1")).thenReturn(Optional.of(legalPerson));
    }

    private void stubNaturalPerson() {
        when(personRepository.findByPersonKey("user-1")).thenReturn(Optional.of(user));
        when(naturalPersonRepository.findByPersonKey("user-1")).thenReturn(Optional.of(naturalPerson));
    }

    @Test
    void getPerson_ownAgentKey_returnsLegalPerson() {
        stubLegalPerson();

        GetPersonDTO result = service.getPerson("legal-1", "legal-1", "owner-1");

        assertThat(result).isEqualTo(new GetPersonDTO(company, legalPerson, null));
    }

    @Test
    void getPerson_ownUserKey_returnsNaturalPerson() {
        stubNaturalPerson();

        GetPersonDTO result = service.getPerson("user-1", "legal-1", "user-1");

        assertThat(result).isEqualTo(new GetPersonDTO(user, null, naturalPerson));
    }

    @Test
    void getPerson_userKeyWithoutAgentKey_returnsNaturalPerson() {
        stubNaturalPerson();

        GetPersonDTO result = service.getPerson("user-1", null, "user-1");

        assertThat(result).isEqualTo(new GetPersonDTO(user, null, naturalPerson));
    }

    @Test
    void getPerson_keyDifferentFromAgentAndUser_throwsNotFoundWithoutQuerying() {
        assertThatThrownBy(() -> service.getPerson("other-1", "legal-1", "owner-1"))
                .isInstanceOf(PersonNotFoundException.class);

        verifyNoInteractions(personRepository, legalPersonRepository, naturalPersonRepository,
                personPhoneNumberRepository);
    }

    @Test
    void getPerson_unknownKey_throwsNotFound() {
        when(personRepository.findByPersonKey("legal-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPerson("legal-1", "legal-1", "owner-1"))
                .isInstanceOf(PersonNotFoundException.class);

        verifyNoInteractions(legalPersonRepository, naturalPersonRepository);
    }

    @Test
    void getInternalPerson_legalPerson_returnsLegalPersonAndPhones() {
        stubLegalPerson();
        var phoneNumbers = List.of(new PersonPhoneNumber("phone-1", company, "55", "51", "999990000"));
        when(personPhoneNumberRepository.findAllByPersonKey("legal-1")).thenReturn(phoneNumbers);

        GetInternalPersonDTO result = service.getInternalPerson("legal-1");

        assertThat(result).isEqualTo(new GetInternalPersonDTO(company, phoneNumbers, legalPerson, null));
    }

    @Test
    void getInternalPerson_naturalPerson_returnsNaturalPersonAndPhones() {
        stubNaturalPerson();
        var phoneNumbers = List.of(new PersonPhoneNumber("phone-2", user, "55", "11", "999999999"));
        when(personPhoneNumberRepository.findAllByPersonKey("user-1")).thenReturn(phoneNumbers);

        GetInternalPersonDTO result = service.getInternalPerson("user-1");

        assertThat(result).isEqualTo(new GetInternalPersonDTO(user, phoneNumbers, null, naturalPerson));
    }

    @Test
    void getInternalPerson_unknownKey_throwsNotFoundWithoutQueryingTheRest() {
        when(personRepository.findByPersonKey("legal-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getInternalPerson("legal-1"))
                .isInstanceOf(PersonNotFoundException.class);

        verify(personPhoneNumberRepository, never()).findAllByPersonKey(any());
        verifyNoInteractions(legalPersonRepository, naturalPersonRepository);
    }
}
