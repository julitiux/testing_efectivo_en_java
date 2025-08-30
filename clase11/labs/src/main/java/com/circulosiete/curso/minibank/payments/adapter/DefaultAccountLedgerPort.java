package com.circulosiete.curso.minibank.payments.adapter;

import com.circulosiete.curso.minibank.payments.ports.out.AccountLedgerPort;
import org.springframework.stereotype.Component;

@Component
public class DefaultAccountLedgerPort implements AccountLedgerPort {
    @Override
    public String hold(String requestId, String accountId, String currency, String amount, String reason) {
        return "";
    }

    @Override
    public void post(String holdRef, String debitAccountId, String creditAccountId, String amount, String currency) {

    }

    @Override
    public void release(String holdRef) {

    }
}
