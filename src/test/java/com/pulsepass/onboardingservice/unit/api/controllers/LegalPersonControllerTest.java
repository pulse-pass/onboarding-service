package com.pulsepass.onboardingservice.unit.api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.onboardingservice.api.controllers.LegalPersonController;
import com.pulsepass.onboardingservice.api.exceptions.GlobalExceptionHandler;
import com.pulsepass.onboardingservice.application.dtos.input.CreateLegalPersonDTO;
import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonAddressDTO;
import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonCollaboratorDTO;
import com.pulsepass.onboardingservice.application.dtos.input.definitions.PersonPhoneNumberDTO;
import com.pulsepass.onboardingservice.application.services.LegalPersonService;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyTypeEnum;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.domain.exceptions.LegalPersonAlreadyExistsException;
import com.pulsepass.onboardingservice.domain.exceptions.UserCreationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LegalPersonController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.messages.basename=messages",
        "spring.messages.fallback-to-system-locale=false"
})
class LegalPersonControllerTest {

    private static final String URL = "/onboarding/legal_persons";
    private static final String VALID_BODY = body("12345678000195", "OWNER", "52998224725", "Secret123");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LegalPersonService legalPersonService;

    private static String body(String cnpj, String role, String cpf, String password) {
        return """
                {
                  "document_number": %s,
                  "name": "Acme Events",
                  "email": "acme@mail.com",
                  "phone_number": {"country_code": "55", "area_code": "11", "number": "999999999"},
                  "legal_person_company_type": "LIMITED",
                  "monthly_revenue_amount": 25000.00,
                  "legal_person_address": {
                    "postal_code": "90110220",
                    "street": "Rua Ismael Chaves Barcelos",
                    "number": "60",
                    "complement": "Sala 2",
                    "neighborhood": "Praia de Belas"
                  },
                  "legal_person_collaborators": [
                    {
                      "collaborator_role": %s,
                      "document_number": %s,
                      "name": "John Doe",
                      "email": "john@mail.com",
                      "password": %s
                    }
                  ]
                }
                """.formatted(quote(cnpj), quote(role), quote(cpf), quote(password));
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    private static LegalPerson legalPerson() {
        var person = new Person("person-key", new PersonType(1L, "LEGAL", null),
                "12345678000195", "Acme Events");
        return new LegalPerson(person, "acme@mail.com", null, null, null);
    }

    @Test
    void createLegalPerson_validRequest_returns201WithSnakeCaseBody() throws Exception {
        when(legalPersonService.createLegalPerson(any(CreateLegalPersonDTO.class))).thenReturn(legalPerson());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("acme@mail.com"))
                .andExpect(jsonPath("$.person.person_key").value("person-key"))
                .andExpect(jsonPath("$.person.document_number").value("12345678000195"))
                .andExpect(jsonPath("$.person.name").value("Acme Events"))
                .andExpect(jsonPath("$.person_key").doesNotExist())
                .andExpect(jsonPath("$.legal_person_collaborators").doesNotExist());

        ArgumentCaptor<CreateLegalPersonDTO> captor = ArgumentCaptor.forClass(CreateLegalPersonDTO.class);
        verify(legalPersonService).createLegalPerson(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new CreateLegalPersonDTO(
                "12345678000195",
                "Acme Events",
                "acme@mail.com",
                new PersonPhoneNumberDTO("55", "11", "999999999"),
                LegalPersonCompanyTypeEnum.LIMITED,
                new BigDecimal("25000.00"),
                new LegalPersonAddressDTO("90110220", "Rua Ismael Chaves Barcelos", "60", "Sala 2", "Praia de Belas"),
                List.of(new LegalPersonCollaboratorDTO(
                        "OWNER", "52998224725", "John Doe", "john@mail.com", "Secret123"))
        ));
    }

    static Stream<Arguments> invalidBodies() {
        return Stream.of(
                Arguments.of("cnpj missing", body(null, "OWNER", "52998224725", "Secret123")),
                Arguments.of("cnpj 13 digits", body("1234567800019", "OWNER", "52998224725", "Secret123")),
                Arguments.of("cnpj with letters", body("1234567800019A", "OWNER", "52998224725", "Secret123")),
                Arguments.of("role missing", body("12345678000195", null, "52998224725", "Secret123")),
                Arguments.of("cpf missing", body("12345678000195", "OWNER", null, "Secret123")),
                Arguments.of("cpf 10 digits", body("12345678000195", "OWNER", "5299822472", "Secret123")),
                Arguments.of("password missing", body("12345678000195", "OWNER", "52998224725", null)),
                Arguments.of("password 7 chars", body("12345678000195", "OWNER", "52998224725", "Secret1")),
                Arguments.of("collaborators empty", """
                        {"document_number": "12345678000195", "name": "Acme", "email": "a@mail.com",
                         "phone_number": {"country_code": "55", "area_code": "11", "number": "999999999"},
                         "legal_person_company_type": "LIMITED", "monthly_revenue_amount": 25000.00,
                         "legal_person_address": {"postal_code": "90110220", "street": "Rua A", "number": "60",
                          "neighborhood": "Centro"},
                         "legal_person_collaborators": []}
                        """),
                Arguments.of("phone number missing", VALID_BODY.replaceFirst("\"phone_number\":[^\\n]*\\n", "")),
                Arguments.of("company type missing",
                        VALID_BODY.replace("\"legal_person_company_type\": \"LIMITED\",", "")),
                Arguments.of("company type unknown", VALID_BODY.replace("\"LIMITED\"", "\"UNKNOWN\"")),
                Arguments.of("revenue missing", VALID_BODY.replace("\"monthly_revenue_amount\": 25000.00,", "")),
                Arguments.of("revenue zero", VALID_BODY.replace("25000.00", "0")),
                Arguments.of("revenue negative", VALID_BODY.replace("25000.00", "-1")),
                Arguments.of("revenue three decimals", VALID_BODY.replace("25000.00", "10.123")),
                Arguments.of("revenue too many integer digits",
                        VALID_BODY.replace("25000.00", "123456789012345678")),
                Arguments.of("address missing",
                        VALID_BODY.replaceFirst("(?s)\"legal_person_address\": \\{.*?\\},", "")),
                Arguments.of("postal code 7 digits", VALID_BODY.replace("90110220", "9011022")),
                Arguments.of("postal code with hyphen", VALID_BODY.replace("90110220", "90110-220")),
                Arguments.of("street blank", VALID_BODY.replace("Rua Ismael Chaves Barcelos", " ")),
                Arguments.of("street too long", VALID_BODY.replace("Rua Ismael Chaves Barcelos", "a".repeat(256))),
                Arguments.of("number blank", VALID_BODY.replace("\"number\": \"60\"", "\"number\": \"\"")),
                Arguments.of("number too long", VALID_BODY.replace("\"60\"", "\"" + "1".repeat(21) + "\"")),
                Arguments.of("complement too long", VALID_BODY.replace("Sala 2", "a".repeat(256))),
                Arguments.of("neighborhood blank", VALID_BODY.replace("Praia de Belas", " ")),
                Arguments.of("malformed json", "{\"document_number\": "),
                Arguments.of("empty body", "")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBodies")
    void createLegalPerson_invalidBody_returns400WithBadRequestCode(String scenario, String content)
            throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verifyNoInteractions(legalPersonService);
    }

    @Test
    void createLegalPerson_serviceThrowsAlreadyExists_returns409() throws Exception {
        when(legalPersonService.createLegalPerson(any(CreateLegalPersonDTO.class)))
                .thenThrow(new LegalPersonAlreadyExistsException());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ONB00001"));
    }

    @Test
    void createLegalPerson_serviceThrowsUserCreation_returns422() throws Exception {
        when(legalPersonService.createLegalPerson(any(CreateLegalPersonDTO.class)))
                .thenThrow(new UserCreationException());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("ONB00006"));
    }

    @Test
    void unknownRoute_returns404WithGen00404() throws Exception {
        mockMvc.perform(get("/unknown_route"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GEN00404"))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void createRoute_withPut_returns405WithGen00405() throws Exception {
        mockMvc.perform(put(URL))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("GEN00405"))
                .andExpect(jsonPath("$.title").value("Method Not Allowed"));
    }

    @Test
    void createRoute_withTextPlain_returns415WithGen00415() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("GEN00415"))
                .andExpect(jsonPath("$.title").value("Unsupported Media Type"));
    }
}
