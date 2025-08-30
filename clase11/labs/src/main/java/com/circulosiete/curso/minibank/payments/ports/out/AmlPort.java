package com.circulosiete.curso.minibank.payments.ports.out;

/**
 * Defines a port for Anti-Money Laundering (AML) validation checks within the payment processing system.
 * Implementations of this interface are responsible for determining whether a financial transaction
 * complies with AML regulations.
 */
public interface AmlPort {
    boolean passesAml(
        String debtorAccount,
        String creditorAccount,
        String amount,
        String currency
    );
}
