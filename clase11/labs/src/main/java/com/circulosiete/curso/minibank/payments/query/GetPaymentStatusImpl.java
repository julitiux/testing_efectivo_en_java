package com.circulosiete.curso.minibank.payments.query;

import com.circulosiete.curso.minibank.payments.ports.in.GetPaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class GetPaymentStatusImpl implements GetPaymentStatus {
    @Override
    public Result handle(Query query) {
        return null;
    }
}
