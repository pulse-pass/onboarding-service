package com.pulsepass.onboardingservice.api.schemas;

import com.pulsepass.onboardingservice.api.schemas.definitions.LegalPersonAddressSchema;
import com.pulsepass.onboardingservice.api.schemas.definitions.LegalPersonCollaboratorSchema;
import com.pulsepass.onboardingservice.api.schemas.definitions.PersonPhoneNumberSchema;
import com.pulsepass.onboardingservice.application.dtos.input.CreateLegalPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateLegalPersonSchema(
        @NotBlank @Pattern(regexp = "^\\d{14}$") String documentNumber,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) @Email String email,
        @NotNull @Valid PersonPhoneNumberSchema phoneNumber,
        @NotNull LegalPersonCompanyTypeEnum legalPersonCompanyType,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal monthlyRevenueAmount,
        @NotNull @Valid LegalPersonAddressSchema legalPersonAddress,
        @NotNull @Size(min = 1, max = 1) List<@NotNull @Valid LegalPersonCollaboratorSchema> legalPersonCollaborators
) {

    public CreateLegalPersonDTO toDTO() {
        return new CreateLegalPersonDTO(
                documentNumber,
                name,
                email,
                phoneNumber.toDTO(),
                legalPersonCompanyType,
                monthlyRevenueAmount,
                legalPersonAddress.toDTO(),
                legalPersonCollaborators.stream().map(LegalPersonCollaboratorSchema::toDTO).toList()
        );
    }
}
