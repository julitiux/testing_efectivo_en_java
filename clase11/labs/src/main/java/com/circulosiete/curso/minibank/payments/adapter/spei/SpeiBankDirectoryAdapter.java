package com.circulosiete.curso.minibank.payments.adapter.spei;

import com.circulosiete.curso.minibank.payments.ports.out.BankDirectoryPort;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SpeiBankDirectoryAdapter implements BankDirectoryPort {
    private final Set<String> allowedBankCodes;

    public SpeiBankDirectoryAdapter(
        @Value("${spei.allowed-bank-codes:002,012,014,021,030,044,072,127,136,137,138,140}")
        String allowedCodesCsv
    ) {
        this.allowedBankCodes = Set.of(allowedCodesCsv.split("\\s*,\\s*"));
    }

    @Override
    public boolean isValidAccount(String account, String bankId, String currency) {
        if (!"MXN".equalsIgnoreCase(currency)) {
            return false; // acota a SPEI
        }
        if (!SpeiValidation.isValidClabe(account)) {
            return false;
        }

        // Validar clave institución: usar CLABE (preferente) o parámetro bankId si lo quieres comparar
        var codeFromClabe = SpeiValidation.bankCode(account);
        if (!allowedBankCodes.contains(codeFromClabe)) {
            return false;
        }

        return bankId == null ||
            bankId.isBlank() ||
            bankId.equals(codeFromClabe); // el bankId declarado no coincide con la CLABE
    }
}
