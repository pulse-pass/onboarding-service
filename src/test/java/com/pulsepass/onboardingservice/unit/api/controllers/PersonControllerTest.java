package com.pulsepass.onboardingservice.unit.api.controllers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.onboardingservice.api.controllers.PersonController;
import com.pulsepass.onboardingservice.api.exceptions.GlobalExceptionHandler;
import com.pulsepass.onboardingservice.api.validators.InternalRequestValidator;
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
import com.pulsepass.onboardingservice.domain.exceptions.UnauthorizedInternalRequestException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PersonController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.messages.basename=messages",
        "spring.messages.fallback-to-system-locale=false"
})
class PersonControllerTest {

    private static final String URL = "/onboarding/persons";
    private static final String INTERNAL_URL = "/onboarding/internal/persons";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 9, 14, 12);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private InternalRequestValidator internalRequestValidator;

    private static Person company() {
        return new Person(1L, "legal-key", new PersonType(1L, "LEGAL", null),
                "12345678000195", "Acme Events", CREATED_AT, CREATED_AT);
    }

    private static Person user() {
        return new Person(2L, "user-key", new PersonType(2L, "NATURAL", null),
                "52998224725", "John Doe", CREATED_AT, CREATED_AT);
    }

    @Test
    void getPerson_legalPerson_returns200WithNestedLegalPersonAndNullNaturalPerson() throws Exception {
        var company = company();
        when(personService.getPerson("legal-key", "legal-key", "owner-key")).thenReturn(new GetPersonDTO(
                company, new LegalPerson(company, "acme@mail.com", null, null, null), null));

        mockMvc.perform(get(URL + "/legal-key")
                        .header("selected-agent-key", "legal-key")
                        .header("selected-user-key", "owner-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person_key").value("legal-key"))
                .andExpect(jsonPath("$.person_type").value("LEGAL"))
                .andExpect(jsonPath("$.name").value("Acme Events"))
                .andExpect(jsonPath("$.document_number").value("12345678000195"))
                .andExpect(jsonPath("$.created_at").value("2026-10-09T14:12:00Z"))
                .andExpect(jsonPath("$.legal_person.email").value("acme@mail.com"))
                .andExpect(jsonPath("$.legal_person.owner").doesNotExist())
                .andExpect(jsonPath("$.natural_person").hasJsonPath())
                .andExpect(jsonPath("$.natural_person").doesNotExist())
                .andExpect(jsonPath("$.legal_person_key").doesNotExist());
    }

    @Test
    void getPerson_naturalPersonWithoutAgentKey_returns200WithNestedNaturalPersonAndNullLegalPerson()
            throws Exception {
        var user = user();
        when(personService.getPerson("user-key", null, "user-key"))
                .thenReturn(new GetPersonDTO(user, null, new NaturalPerson(user, "john@mail.com")));

        mockMvc.perform(get(URL + "/user-key").header("selected-user-key", "user-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person_key").value("user-key"))
                .andExpect(jsonPath("$.person_type").value("NATURAL"))
                .andExpect(jsonPath("$.legal_person").hasJsonPath())
                .andExpect(jsonPath("$.legal_person").doesNotExist())
                .andExpect(jsonPath("$.natural_person.email").value("john@mail.com"));
    }

    @Test
    void getPerson_missingUserKeyHeader_returns400WithRequiredHeader() throws Exception {
        mockMvc.perform(get(URL + "/user-key"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.extra_fields[0].field").value("selected-user-key"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("REQUIRED"));

        verifyNoInteractions(personService);
    }

    @Test
    void getPerson_serviceThrowsNotFound_returns404WithOnb00009() throws Exception {
        when(personService.getPerson("other-key", "legal-key", "owner-key")).thenThrow(new PersonNotFoundException());

        mockMvc.perform(get(URL + "/other-key")
                        .header("selected-agent-key", "legal-key")
                        .header("selected-user-key", "owner-key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ONB00009"));
    }

    @Test
    void getInternalPerson_legalPerson_returns200WithPhonesAndNestedLegalPerson() throws Exception {
        var company = company();
        var legalPerson = new LegalPerson(
                company,
                "acme@mail.com",
                new LegalPersonAddress("address-key", "90110220", "Rua Ismael Chaves Barcelos", "60", null,
                        "Praia de Belas"),
                new LegalPersonCompanyType(1L, "LIMITED", null),
                new BigDecimal("25000.00"));
        var phoneNumbers = List.of(new PersonPhoneNumber("phone-key", company, "55", "51", "999990000"));
        when(personService.getInternalPerson("legal-key"))
                .thenReturn(new GetInternalPersonDTO(company, phoneNumbers, legalPerson, null));

        mockMvc.perform(get(INTERNAL_URL + "/legal-key")
                        .header("internal-token", "token")
                        .header("selected-user-roles", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person_key").value("legal-key"))
                .andExpect(jsonPath("$.person_type").value("LEGAL"))
                .andExpect(jsonPath("$.name").value("Acme Events"))
                .andExpect(jsonPath("$.document_number").value("12345678000195"))
                .andExpect(jsonPath("$.created_at").value("2026-10-09T14:12:00Z"))
                .andExpect(jsonPath("$.phone_numbers[0].country_code").value("55"))
                .andExpect(jsonPath("$.phone_numbers[0].area_code").value("51"))
                .andExpect(jsonPath("$.phone_numbers[0].number").value("999990000"))
                .andExpect(jsonPath("$.legal_person.email").value("acme@mail.com"))
                .andExpect(jsonPath("$.legal_person.legal_person_company_type").value("LIMITED"))
                .andExpect(jsonPath("$.legal_person.monthly_revenue_amount").value(25000.00))
                .andExpect(jsonPath("$.legal_person.legal_person_address.legal_person_address_key")
                        .value("address-key"))
                .andExpect(jsonPath("$.legal_person.legal_person_address.postal_code").value("90110220"))
                .andExpect(jsonPath("$.legal_person.legal_person_address.street")
                        .value("Rua Ismael Chaves Barcelos"))
                .andExpect(jsonPath("$.legal_person.legal_person_address.number").value("60"))
                .andExpect(jsonPath("$.legal_person.legal_person_address.complement").hasJsonPath())
                .andExpect(jsonPath("$.legal_person.legal_person_address.neighborhood").value("Praia de Belas"))
                .andExpect(jsonPath("$.legal_person.owner").doesNotExist())
                .andExpect(jsonPath("$.natural_person").hasJsonPath())
                .andExpect(jsonPath("$.natural_person").doesNotExist());

        verify(internalRequestValidator).validateInternalTokenOrAdmin("token", "admin");
    }

    @Test
    void getInternalPerson_naturalPerson_returns200WithPhonesAndNestedNaturalPerson() throws Exception {
        var user = user();
        var phoneNumbers = List.of(new PersonPhoneNumber("phone-key", user, "55", "11", "999999999"));
        when(personService.getInternalPerson("user-key")).thenReturn(new GetInternalPersonDTO(
                user, phoneNumbers, null, new NaturalPerson(user, "john@mail.com")));

        mockMvc.perform(get(INTERNAL_URL + "/user-key").header("internal-token", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person_type").value("NATURAL"))
                .andExpect(jsonPath("$.phone_numbers[0].number").value("999999999"))
                .andExpect(jsonPath("$.legal_person").hasJsonPath())
                .andExpect(jsonPath("$.legal_person").doesNotExist())
                .andExpect(jsonPath("$.natural_person.email").value("john@mail.com"));
    }

    @Test
    void getInternalPerson_validatorThrows_returns401AndSkipsService() throws Exception {
        doThrow(new UnauthorizedInternalRequestException())
                .when(internalRequestValidator).validateInternalTokenOrAdmin(null, null);

        mockMvc.perform(get(INTERNAL_URL + "/legal-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(personService);
    }

    @Test
    void getInternalPerson_serviceThrowsNotFound_returns404WithOnb00009() throws Exception {
        when(personService.getInternalPerson("legal-key")).thenThrow(new PersonNotFoundException());

        mockMvc.perform(get(INTERNAL_URL + "/legal-key").header("internal-token", "token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ONB00009"));
    }
}
