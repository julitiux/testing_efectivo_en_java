package com.circulosiete.curso.testing.clase07.calculadora


import spock.lang.Specification

class CalculatorSpec extends Specification {

    def "should return correct sum"() {
        given: "a calculator"
            def calculator = new Calculadora()

        when: "two numbers are added"
            def result = calculator.sum(2, 3)

        then: "the result should be correct"
            result == 5
    }

    def "sum of #a and #b should be #expected"(int a, int b, int expected) {
        given:
            def calculator = new Calculadora()

        expect:
            calculator.sum(a, b) == expected

        where:
            a  | b  || expected
            1  | 2  || 3
            3  | 5  || 8
            10 | -2 || 8
    }
}
