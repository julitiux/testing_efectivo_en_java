package com.circulosiete.curso.minibank.payments.adapter.iso20022;

public interface Iso20022Client {
    record Iso20022Response(
        String msgId,
        boolean isAccepted,
        String reason
    ) {
    }

    Iso20022Response submitCreditTransfer(Object pacs008);
}
