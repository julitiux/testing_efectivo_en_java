package com.circulosiete.curso.minibank.payments.adapter.spei;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpeiBankDirectoryAdapterTest {

    @Test
    void should_validate_clabe_and_bank_code_and_currency_mxn() {
        var adapter = new SpeiBankDirectoryAdapter("002,012,014,021,030,032,044,072");

        // CLABE válida, banco permitido, currency MXN
        var ok = adapter.isValidAccount("002010077777777771", "002", "MXN");
        assertThat(ok)
            .isTrue();

        // CLABE válida pero bankId declarado no coincide con CLABE -> false
        var mismatch = adapter.isValidAccount("002010077777777771", "012", "MXN");
        assertThat(mismatch)
            .isFalse();

        // Moneda distinta a MXN -> false
        var wrongCurrency = adapter.isValidAccount("002010077777777771", "002", "USD");
        assertThat(wrongCurrency)
            .isFalse();

        // CLABE inválida -> false
        var badClabe = adapter.isValidAccount("002010077777777770", "002", "MXN");
        assertThat(badClabe)
            .isFalse();

        // Banco no permitido en lista -> false
        var notAllowedBank = new SpeiBankDirectoryAdapter("012,014");
        assertThat(notAllowedBank.isValidAccount("002010077777777771", "002", "MXN"))
            .isFalse();

        // Si bankId viene vacío, se acepta el de la CLABE (si está en lista)
        var noBankId = adapter.isValidAccount("032180000118359719", "", "MXN");
        assertThat(noBankId)
            .isTrue();
    }
}

