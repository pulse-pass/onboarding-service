package com.pulsepass.onboardingservice.unit.api.controllers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.onboardingservice.api.controllers.LegalPersonCollaboratorController;
import com.pulsepass.onboardingservice.api.exceptions.GlobalExceptionHandler;
import com.pulsepass.onboardingservice.api.validators.InternalRequestValidator;
import com.pulsepass.onboardingservice.application.services.LegalPersonCollaboratorService;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.exceptions.MissingSelectedAgentKeyException;
import com.pulsepass.onboardingservice.domain.exceptions.PersonNotFoundException;
import com.pulsepass.onboardingservice.domain.exceptions.UnauthorizedInternalRequestException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LegalPersonCollaboratorController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.messages.basename=messages",
        "spring.messages.fallback-to-system-locale=false"
})
class LegalPersonCollaboratorControllerTest {

    private static final String URL = "/onboarding/legal_persons/legal-key/collaborators";
    private static final String INTERNAL_URL = "/onboarding/internal/legal_persons/legal-key/collaborators";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LegalPersonCollaboratorService legalPersonCollaboratorService;

    @MockitoBean
    private InternalRequestValidator internalRequestValidator;

    private static LegalPersonCollaborator collaborator() {
        var legalPerson = new LegalPerson(new Person("legal-key", null, "12345678000195", "Acme"),
                "acme@mail.com", null, null, null);
        var naturalPerson = new NaturalPerson(
                new Person("owner-key", null, "52998224725", "Jane Doe"), "jane@mail.com");
        return new LegalPersonCollaborator("collaborator-key", legalPerson, naturalPerson,
                new CollaboratorRole(1L, "OWNER", null), new LegalPersonCollaboratorStatus(1L, "ACTIVE", null));
    }

    @Test
    void listLegalPersonCollaborators_ownAgentKey_returns200WithPaginatedItems() throws Exception {
        when(legalPersonCollaboratorService.listLegalPersonCollaborators(
                "legal-key", "legal-key", CollaboratorRoleEnum.OWNER, 2, 10)).thenReturn(List.of(collaborator()));

        mockMvc.perform(get(URL)
                        .header("selected-agent-key", "legal-key")
                        .param("collaborator_role", "OWNER")
                        .param("page", "2")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].legal_person_collaborator_key").value("collaborator-key"))
                .andExpect(jsonPath("$.data[0].collaborator_role").value("OWNER"))
                .andExpect(jsonPath("$.data[0].legal_person_collaborator_status").value("ACTIVE"))
                .andExpect(jsonPath("$.data[0].natural_person.email").value("jane@mail.com"))
                .andExpect(jsonPath("$.data[0].natural_person.person.person_key").value("owner-key"))
                .andExpect(jsonPath("$.data[0].natural_person.person.name").value("Jane Doe"))
                .andExpect(jsonPath("$.data[0].natural_person.person.document_number").value("52998224725"))
                .andExpect(jsonPath("$.pagination.page").value(2))
                .andExpect(jsonPath("$.pagination.page_size").value(10));
    }

    @Test
    void listLegalPersonCollaborators_noQuery_usesDefaultsAndReturnsEmptyData() throws Exception {
        when(legalPersonCollaboratorService.listLegalPersonCollaborators("legal-key", "legal-key", null, 1, 30))
                .thenReturn(List.of());

        mockMvc.perform(get(URL).header("selected-agent-key", "legal-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.page_size").value(30));
    }

    @Test
    void listLegalPersonCollaborators_keyOfAnotherAgent_returns404() throws Exception {
        when(legalPersonCollaboratorService.listLegalPersonCollaborators("legal-key", "other-key", null, 1, 30))
                .thenThrow(new PersonNotFoundException());

        mockMvc.perform(get(URL).header("selected-agent-key", "other-key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ONB00009"));
    }

    @Test
    void listLegalPersonCollaborators_invalidCollaboratorRole_returns400() throws Exception {
        mockMvc.perform(get(URL)
                        .header("selected-agent-key", "legal-key")
                        .param("collaborator_role", "BOSS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.extra_fields[0].field").value("collaborator_role"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("INVALID_FORMAT"));

        verifyNoInteractions(legalPersonCollaboratorService);
    }

    @Test
    void listLegalPersonCollaborators_pageSizeAboveLimit_returns400() throws Exception {
        mockMvc.perform(get(URL)
                        .header("selected-agent-key", "legal-key")
                        .param("page_size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extra_fields[0].field").value("page_size"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("SIZE"));

        verifyNoInteractions(legalPersonCollaboratorService);
    }

    @Test
    void listInternalLegalPersonCollaborators_validToken_returns200AndValidatesFirst() throws Exception {
        when(legalPersonCollaboratorService.listInternalLegalPersonCollaborators(
                "legal-key", CollaboratorRoleEnum.MEMBER, 1, 30)).thenReturn(List.of(collaborator()));

        mockMvc.perform(get(INTERNAL_URL)
                        .header("internal-token", "token")
                        .header("selected-user-roles", "admin")
                        .param("collaborator_role", "MEMBER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].legal_person_collaborator_key").value("collaborator-key"))
                .andExpect(jsonPath("$.data[0].natural_person.person.person_key").value("owner-key"))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.page_size").value(30));

        verify(internalRequestValidator).validateInternalTokenOrAdmin("token", "admin");
    }

    @Test
    void listLegalPersonCollaborators_withoutSelectedAgentKey_returns401WithoutQuerying() throws Exception {
        doThrow(new MissingSelectedAgentKeyException())
                .when(internalRequestValidator).validateSelectedAgentKey(null);

        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(legalPersonCollaboratorService);
    }

    @Test
    void listInternalLegalPersonCollaborators_unauthorized_returns401WithoutQuerying() throws Exception {
        doThrow(new UnauthorizedInternalRequestException())
                .when(internalRequestValidator).validateInternalTokenOrAdmin(null, null);

        mockMvc.perform(get(INTERNAL_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(legalPersonCollaboratorService);
    }

    @Test
    void listInternalLegalPersonCollaborators_invalidCollaboratorRole_returns400() throws Exception {
        mockMvc.perform(get(INTERNAL_URL)
                        .header("internal-token", "token")
                        .param("collaborator_role", "BOSS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extra_fields[0].field").value("collaborator_role"));

        verifyNoInteractions(legalPersonCollaboratorService);
    }

    @Test
    void listInternalLegalPersonCollaborators_pageSizeAboveLimit_returns400() throws Exception {
        mockMvc.perform(get(INTERNAL_URL)
                        .header("internal-token", "token")
                        .param("page_size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extra_fields[0].field").value("page_size"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("SIZE"));

        verifyNoInteractions(legalPersonCollaboratorService);
    }
}
