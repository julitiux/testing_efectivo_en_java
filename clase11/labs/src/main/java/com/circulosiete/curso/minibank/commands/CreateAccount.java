package com.circulosiete.curso.minibank.commands;

import com.circulosiete.curso.minibank.model.AccountType;

public record CreateAccount(
    String accountNumber,
    AccountType type,
    String currency
) {
}
