package com.circulosiete.curso.minibank.payments.ports.out;

public interface BankDirectoryPort {
    boolean isValidAccount(
        String account,
        String bankId,
        String currency
    );
}
