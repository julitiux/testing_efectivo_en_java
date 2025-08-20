package com.circulosiete.curso.testing.clase07.mocking.comparison

import com.circulosiete.curso.testing.clase07.mocking.comparison.impl.InMemoryNumberRepository
import spock.lang.Specification

class CalculatorServiceSpySpec extends Specification {

    def "Spy ejecuta lógica real pero permite ajustar un método"() {
        given:
            def realRepo = new InMemoryNumberRepository([3, 9])
            def repo = Spy(realRepo)                      // por defecto ejecuta lo real
            def notifier = Mock(Notifier)
            def sut = new CalculatorService(repo, notifier, 10)

        when:
            def result = sut.sumNextTwo()

        then:
            // Observamos que realmente llamó nextNumber dos veces
            2 * repo.nextNumber() >> { callRealMethod() } // usa real, pero contamos llamadas
            1 * notifier.send("sum=12")
            result == 12
    }

    def "Spy con sobrescritura selectiva por argumento"() {
        given:
            def repo = Spy(new InMemoryNumberRepository()) {
                // Si el id es 21, sobreescribimos la respuesta; para los demás, real
                byId(21L) >> 7
                byId(_ as Long) >> { long id -> callRealMethod() } // fallback: real
            }
            def notifier = Mock(Notifier)
            def sut = new CalculatorService(repo, notifier, 10)
        and:
            0 * notifier.send(_)
        expect:

            sut.doubleOfId(21L) == 14      // usa la sobrescritura
            sut.doubleOfId(1L) == 0       // usa el real (no hay entry para 1L, retorna 0)
    }
    /*
     * Claves del Spy
     *   Spy(impl) ejecuta la implementación real por defecto.
     *   Puedes mezclar callRealMethod() con >> para parchear selectivamente.
     *   Aún puedes verificar interacciones (n * repo.metodo(_)).
     */
}
