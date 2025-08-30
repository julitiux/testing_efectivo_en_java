package com.circulosiete.curso.minibank.payments.adapter.spei;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SpeiClient {
    record SpeiResponse(
        String code,
        String trackingKey,
        String description
    ) {
    }

    record SpeiStatusResponse(
        String status,
        String raw
    ) {
    }

    public SpeiStatusResponse queryStatus(String externalRef) {
        return new SpeiStatusResponse(
            "",
            ""
        );
    }

    public ResponseEntity<Object> cancel(String externalRef) {
        return ResponseEntity
            .ok()
            .build();
    }


    public SpeiResponse sendPayment(Object xml) {
        return new SpeiResponse(
            "00",
            "",
            ""
        );
    }
}
