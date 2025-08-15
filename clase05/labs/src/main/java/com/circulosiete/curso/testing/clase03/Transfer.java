package com.circulosiete.curso.testing.clase03;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Transfer(
        UUID id,
        UUID from,
        UUID to,
        BigDecimal amount,
        LocalDateTime when
) {

}
