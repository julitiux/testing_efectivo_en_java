package com.circulosiete.curso.minibank.payments.adapter.spei;

import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpeiPaymentRailAdapter implements PaymentRailPort {

    private final SpeiClient client; // REST/SOAP/XML
    private final SpeiMapper mapper; // ACL a tu modelo

    public TransferResponse send(TransferRequest req) {
        final var xml = mapper.toSpeiXml(req);
        final var speiResp = client.sendPayment(xml);
        final var accepted = speiResp.code().equals("00");

        return new TransferResponse(
            speiResp.trackingKey(),
            accepted,
            speiResp.description()
        );
    }

    public StatusResponse status(String externalRef) {
        final var status = client.queryStatus(externalRef);

        return new StatusResponse(
            externalRef,
            status.status(),
            status.raw()
        );
    }

    public boolean cancel(String externalRef) {
        return client.cancel(externalRef)
            .getStatusCode()
            .is2xxSuccessful();
    }
}
