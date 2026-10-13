package com.pulsepass.onboardingservice.unit.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.common.DocumentHandlers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DocumentHandlersTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "11144477735", "39053344705"})
    void isValidCpf_validNumber_returnsTrue(String cpf) {
        assertThat(DocumentHandlers.isValidCpf(cpf)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"52998224724", "52998224715", "5299822472", "529982247250", "5299822472A",
            "11111111111", "00000000000", "529.982.247-25"})
    @NullAndEmptySource
    void isValidCpf_invalidNumber_returnsFalse(String cpf) {
        assertThat(DocumentHandlers.isValidCpf(cpf)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678000195", "11222333000181", "11444777000161"})
    void isValidCnpj_validNumber_returnsTrue(String cnpj) {
        assertThat(DocumentHandlers.isValidCnpj(cnpj)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678000194", "12345678000185", "1234567800019", "123456780001955",
            "1234567800019A", "11111111111111", "00000000000000", "12.345.678/0001-95"})
    @NullAndEmptySource
    void isValidCnpj_invalidNumber_returnsFalse(String cnpj) {
        assertThat(DocumentHandlers.isValidCnpj(cnpj)).isFalse();
    }

    @Test
    void isValidCpf_cnpjNumber_returnsFalse() {
        assertThat(DocumentHandlers.isValidCpf("12345678000195")).isFalse();
    }

    @Test
    void constructor_utilityClass_isPrivate() throws Exception {
        var constructor = DocumentHandlers.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThat(constructor.newInstance()).isNotNull();
    }
}
