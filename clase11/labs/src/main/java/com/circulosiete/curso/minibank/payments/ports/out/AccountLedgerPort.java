package com.circulosiete.curso.minibank.payments.ports.out;

/**
 * Defines the interface for interacting with the account ledger system.
 * The account ledger system is responsible for handling operations
 * related to holding, posting, and releasing funds, which support
 * financial transaction flows in systems such as payment processing.
 */
public interface AccountLedgerPort {
    // hold → post → release/rollback
    String hold(
        String requestId,
        String accountId,
        String currency,
        String amount,
        String reason
    );

    void post(
        String holdRef,
        String debitAccountId,
        String creditAccountId,
        String amount,
        String currency
    );

    void release(String holdRef);
}
