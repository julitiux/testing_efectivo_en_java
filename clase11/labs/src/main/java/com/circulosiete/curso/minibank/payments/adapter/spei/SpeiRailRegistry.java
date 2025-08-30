package com.circulosiete.curso.minibank.payments.adapter.spei;


import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailPort;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpeiRailRegistry implements PaymentRailRegistry {
    private final SpeiPaymentRailAdapter spei;

    @Override
    public PaymentRailPort resolve(String toBankId, String currency) {
        // Para MXN => SPEI. (Puedes agregar otras monedas/rieles aquí).
        return spei;
    }
}
