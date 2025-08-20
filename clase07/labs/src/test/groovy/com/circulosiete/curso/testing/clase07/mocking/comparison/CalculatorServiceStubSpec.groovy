package com.circulosiete.curso.testing.clase07.mocking.comparison

import spock.lang.Specification

class CalculatorServiceStubSpec extends Specification {

    def "sumNextTwo usa valores predefinidos del repository (Stub)"() {
        given:
            def repo = Stub(NumberRepository) {
                nextNumber() >>> [4, 9]   // primera llamada 4, segunda 9
            }
            def notifier = Stub(Notifier) // no nos importa si se llama o no
            def sut = new CalculatorService(repo, notifier, 10)

        when:
            def result = sut.sumNextTwo()

        then:
            result == 13
            // No verificamos interacciones; solo el valor retornado
    }

    def "doubleOfId con respuestas basadas en argumentos (Stub con closures)"() {
        given:
            def repo = Stub(NumberRepository) {
                byId(_ as Long) >> { long id -> (int) (id * 3) } // respuesta derivada del arg
            }
            def notifier = Stub(Notifier)
            def sut = new CalculatorService(repo, notifier, 10)

        expect:
            sut.doubleOfId(5L) == 30
            sut.doubleOfId(2L) == 12
    }

    /*
     * Claves del Stub
     *   >> fija una respuesta.
     *   >>> define respuestas secuenciales.

     * Las llamadas que no declares no fallan,
     * simplemente devuelven valores por defecto (0/false/null)
     * si son primitivas/objetos.
     */
}
