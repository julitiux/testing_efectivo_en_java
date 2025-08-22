Feature: Transferencias de cuenta a cuenta
    Como cliente de MiniBank
    Yo quiero transferir fondos entre cuentas
    Para que los saldos se actualicen de forma automática y segura.

    Background:
        Given el sistema está sano

    Scenario: Transferencia exitosa entre dos cuentas MXN
        Given Existen las siguientes cuentas:
            | name | balance | currency |
            | A    | 1000.00 | MXN      |
            | B    | 100.00  | MXN      |
        When Transfiero 250.00 MXN de la cuenta "A" a la cuenta "B" con clave de idempotencia "xfer-1"
        Then El código de respuesta debe ser 200
        And El balance "from" debe ser 750.00 "MXN"
        And El balance "to" debe ser 350.00 "MXN"

    Scenario: La idempotencia devuelve 409 en caso de solicitud duplicada
        Given Existen las siguientes cuentas:
            | name | balance | currency |
            | A    | 1000.00 | MXN      |
            | B    | 100.00  | MXN      |
        When Transfiero 100.00 MXN de la cuenta "A" a la cuenta "B" con clave de idempotencia "dup-1"
        And Repito la misma transferencia
        Then El segundo código de respuesta debe ser 409
        And Los saldos guardados deben ser:
            | name | balance |
            | A    | 900.00  |
            | B    | 200.00  |

    Scenario: Los fondos insuficientes arrojan un resultado de 422 y no hay cambios
        Given Existen las siguientes cuentas:
            | name | balance | currency |
            | A    | 1000.00 | MXN      |
            | B    | 100.00  | MXN      |
        When Transfiero 1500.00 MXN de la cuenta "A" a la cuenta "B" con clave de idempotencia "nsf-1"
        Then El código de respuesta debe ser 422
        And Los saldos guardados deben ser:
            | name | balance |
            | A    | 1000.00 |
            | B    | 100.00  |

    Scenario: Divisas diferentes arrojan un resultado de 400
        Given Existen las siguientes cuentas:
            | name | balance | currency |
            | A    | 1000.00 | MXN      |
            | B    | 0.00    | USD      |
        When Transfiero 10.00 MXN de la cuenta "A" a la cuenta "B" con clave de idempotencia "cur-1"
        Then El código de respuesta debe ser 400

    Scenario: Cuenta no encontrada genera 404
        When Envío una transferencia entre cuentas inexistentes con clave de idempotencia "nf-1"
        Then El código de respuesta debe ser 404
