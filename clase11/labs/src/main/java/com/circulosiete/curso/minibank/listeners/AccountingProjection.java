package com.circulosiete.curso.minibank.listeners;

import com.circulosiete.curso.minibank.commands.AccountCredited;
import com.circulosiete.curso.minibank.commands.AccountDebited;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountingProjection {
    @EventListener
    public void on(AccountCredited e) {
        log.info("Account {} credited {} {}", e.accountId(), e.amount(), e.currency());
        // Aquí podrías emitir un evento de integración (Kafka), o actualizar un read-model.
    }

    @EventListener
    public void on(AccountDebited e) {
        log.info("Account {} debited {} {}", e.accountId(), e.amount(), e.currency());
    }
}
