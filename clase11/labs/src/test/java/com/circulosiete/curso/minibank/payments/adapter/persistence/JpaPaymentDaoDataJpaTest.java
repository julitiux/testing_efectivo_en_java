package com.circulosiete.curso.minibank.payments.adapter.persistence;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
class JpaPaymentDaoDataJpaTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    JpaPaymentDao jpaPaymentDao;

    @Test
    void save_and_find_by_request_and_external_ref() {
        var paymentJpa = new PaymentJpa();
        paymentJpa.setRequestId("req-abc");
        paymentJpa.setFromAccountId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        paymentJpa.setToBankAccount("002010077777777771");
        paymentJpa.setToBankId("002");
        paymentJpa.setCurrency("MXN");
        paymentJpa.setAmount(new BigDecimal("100.00"));

        var saved = jpaPaymentDao.save(paymentJpa);
        assertThat(saved.getId())
            .isNotNull();

        // buscar por requestId
        var byReq = jpaPaymentDao.findByRequestId("req-abc");
        assertThat(byReq)
            .isPresent();

        // asignar externalRef y re-guardar
        saved.setExternalRef("SPEI-req-abc");
        jpaPaymentDao.save(saved);

        // buscar por externalRef
        var byExt = jpaPaymentDao.findByExternalRef("SPEI-req-abc");
        assertThat(byExt).isPresent();
        assertThat(byExt.get().getCurrency()).isEqualTo("MXN");
        assertThat(byExt.get().getAmount()).isEqualByComparingTo("100.00");
    }
}

