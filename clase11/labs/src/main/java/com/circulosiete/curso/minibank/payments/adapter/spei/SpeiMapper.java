package com.circulosiete.curso.minibank.payments.adapter.spei;

import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailPort;
import org.springframework.stereotype.Component;

@Component
public class SpeiMapper {
    public String toSpeiXml(PaymentRailPort.TransferRequest req) {
        return null;
    }
}
