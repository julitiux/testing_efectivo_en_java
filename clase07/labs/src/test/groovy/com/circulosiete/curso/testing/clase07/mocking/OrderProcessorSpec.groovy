package com.circulosiete.curso.testing.clase07.mocking

import spock.lang.Specification

class OrderProcessorSpec extends Specification {
    def "should call payment service once"() {
        given:
            def paymentService = Mock(PaymentService)
            def processor = new OrderProcessor(paymentService)

        when:
            processor.processOrder(100)

        then:
            1 * paymentService.pay(100) >> true
    }
}
