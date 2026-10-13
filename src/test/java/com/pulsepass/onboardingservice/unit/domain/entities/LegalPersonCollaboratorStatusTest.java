package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LegalPersonCollaboratorStatusTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void new_allArgs_exposesValues() {
        var entity = new LegalPersonCollaboratorStatus(1L, "ENUM", NOW);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getEnumerator()).isEqualTo("ENUM");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void setters_newValues_updateFields() {
        var entity = new LegalPersonCollaboratorStatus();

        entity.setId(2L);
        entity.setEnumerator("OTHER");
        entity.setCreatedAt(NOW);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getEnumerator()).isEqualTo("OTHER");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
    }
}
