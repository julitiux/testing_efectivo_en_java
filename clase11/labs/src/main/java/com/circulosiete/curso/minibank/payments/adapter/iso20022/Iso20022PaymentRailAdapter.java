package com.circulosiete.curso.minibank.payments.adapter.iso20022;

import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Iso20022PaymentRailAdapter implements PaymentRailPort {
    private final Iso20022Client client;
    private final Pacs008Mapper mapper;

    public TransferResponse send(TransferRequest req) {
        var pacs008 = mapper.toPacs008(req);
        var ack = client.submitCreditTransfer(pacs008);
        return new TransferResponse(ack.msgId(), ack.isAccepted(), ack.reason());
    }

    public StatusResponse status(String externalRef) {
        /* ... */
        return null;
    }

    public boolean cancel(String externalRef) {
        /* ... */
        return false;
    }
}
