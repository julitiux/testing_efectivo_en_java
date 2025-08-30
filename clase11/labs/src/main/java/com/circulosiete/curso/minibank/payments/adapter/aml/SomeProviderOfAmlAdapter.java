package com.circulosiete.curso.minibank.payments.adapter.aml;

import com.circulosiete.curso.minibank.payments.ports.out.AmlPort;
import org.springframework.stereotype.Component;

@Component
public class SomeProviderOfAmlAdapter implements AmlPort {
    @Override
    public boolean passesAml(
        String debtorAccount,
        String creditorAccount,
        String amount,
        String currency
    ) {
        return false;
    }
}
