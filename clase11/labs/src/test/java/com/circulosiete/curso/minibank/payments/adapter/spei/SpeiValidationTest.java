package com.circulosiete.curso.minibank.payments.adapter.spei;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpeiValidationTest {

    @Test
    void valid_clabe_should_pass_checksum() {
        // Ejemplos válidos
        assertThat(SpeiValidation.isValidClabe("002010077777777771"))
            .isTrue();
        assertThat(SpeiValidation.isValidClabe("032180000118359719"))
            .isTrue();
    }

    @Test
    void invalid_clabe_should_fail_checksum() {
        assertThat(SpeiValidation.isValidClabe("002010077777777770")) // cambia dígito verificador
            .isFalse();
        assertThat(SpeiValidation.isValidClabe("137002777777777777")) // banco inválido + checksum
            .isFalse();
        assertThat(SpeiValidation.isValidClabe("123")) // longitud incorrecta
            .isFalse();
        assertThat(SpeiValidation.isValidClabe(null)).isFalse();
    }

    @Test
    void bank_code_should_be_first_3_digits() {
        assertThat(SpeiValidation.bankCode("002010077777777771"))
            .isEqualTo("002");
        assertThat(SpeiValidation.bankCode("032180000118359719"))
            .isEqualTo("032");
        assertThat(SpeiValidation.bankCode("12"))
            .isEqualTo("");
        assertThat(SpeiValidation.bankCode(null))
            .isEqualTo("");
    }
}
