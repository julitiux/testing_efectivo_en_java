package com.circulosiete.curso.testing.clase07.mocking.comparison

import spock.lang.Specification

class CalculatorServiceMockSpec extends Specification {

    def "sumNextTwo envía notificación si supera el umbral (Mock con verificaciones)"() {
        given:
            def repo = Mock(NumberRepository)
            def notifier = Mock(Notifier)
            def sut = new CalculatorService(repo, notifier, 10)

        when:
            def result = sut.sumNextTwo()

        then:
            // Expectativas + stubbing en línea
            2 * repo.nextNumber() >>> [6, 7]          // debe llamarse 2 veces, 6 y luego 7
            1 * notifier.send({ it.contains("sum=13") })
            result == 13

        and:
            0 * _                                      // no debe haber otras interacciones
    }

    def "sumNextTwo NO envía notificación si no supera el umbral"() {
        given:
            def repo = Mock(NumberRepository)
            def notifier = Mock(Notifier)
            def sut = new CalculatorService(repo, notifier, 20)

        when:
            def result = sut.sumNextTwo()

        then:
            2 * repo.nextNumber() >>> [8, 10]          // 8+10 = 18, bajo el umbral 20
            0 * notifier._                             // notifier no debe ser llamado
            result == 18
    }

    def "verificación con constraints de argumentos y rangos"() {
        given:
            def repo = Mock(NumberRepository)
            def notifier = Mock(Notifier)
            def sut = new CalculatorService(repo, notifier, 5)

        when:
            def value = sut.doubleOfId(21L)  // llama repo.byId(21L)

        then:
            1 * repo.byId({ it > 20L && it % 3 == 0 }) >> 7
            0 * notifier._
            value == 14
    }
    /*
     * Claves del Mock
     *   n * dependencia.metodo(args) verifica el conteo exacto.
     *   Usa predicados en los argumentos ({ it > 20 }).
     *   0 * _ asegura que no hubo llamadas extra.
     */
}
