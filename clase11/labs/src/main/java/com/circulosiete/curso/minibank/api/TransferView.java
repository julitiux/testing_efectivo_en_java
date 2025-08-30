package com.circulosiete.curso.minibank.api;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.util.UUID;

@Value @Builder
public class TransferView {
    UUID transferId;
    UUID fromAccountId;
    UUID toAccountId;
    BigDecimal amount;
    String currency;
    AccountView from; // snapshot post-transfer
    AccountView to;   // snapshot post-transfer
}
