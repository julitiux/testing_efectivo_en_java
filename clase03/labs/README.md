# TDD: Transferencia bancaria con antifraude y límites

Desarrolla un servicio de transferencias con TDD. Siga el procedimiento `Red–Green–Refactor`.

### Etapas

1) Transferencia entre dos cuentas en memoria.
   * Épica 001, historia 1
2) Rechazar cuando el saldo sea insuficiente.
    * Épica 001, historia 2
3) Aplicar un límite diario de transferencia por cuenta de origen.
    * Épica 001, historia 3
4) Bloquear las cuentas de destino en lista negra (mediante un servicio de `FraudChecker`).
    * Épica 001, historia 4
5) Generar el ID y la marca de tiempo (timestamp) mediante los servicios (`IdGenerator`, `Clock`).
    * Épica 001, historia 5
6) Persistir las cuentas y transferencias en `PostgreSQL` (`Testcontainers`) con bloqueo optimista.
    * Épica 002, historia 6 y 7 - (**Tarea**)
7) Publicar eventos de dominio (en memoria) para `TransferCreated`. - (**Tarea**)

> Notas:
> * Las historias de usuario se encuentran en `src/main/spec/*.md`
> * Crear las siguientes clases
>   * `Account`
>   * `TransferCreated`
>   * `TransferService`
>   * Considerar crear una clase `Money` con métodos de conveniencia para las siguientes operaciones:
>     * `gte`
>     * `plus`
>     * `minus`
>   * Servicios colaboradores (interfaces):
>     * `Clock`
>     * `DailyLimitRepository`
>     * `FraudChecker`
>     * `IdGenerator`
>     * `TransferRepository`
>     * `AccountRepository`
>       * `JdbcAccountRepository`: Implementación de JDBC para pruebas de integración. 
> * Escribir las pruebas unitarias en una clase llamada `TransferServiceTest`
> * Escribir las pruebas de integración en una clase llamada: `JdbcAccountRepositoryIntegrationTest`

Ejecutar pruebas unitarias: `mvn -Dtest=TransferServiceTest test`  
Ejecutar pruebas de integración (requiere `Docker`): `mvn -Dtest=JdbcAccountRepositoryIntegrationTest test`
