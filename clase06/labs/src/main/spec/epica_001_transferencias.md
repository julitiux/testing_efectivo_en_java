# Historias de usuario — Transferencias (TDD)

---

## Épica: Transferencias entre cuentas

### 1) Transferir fondos entre dos cuentas

**Como** cliente bancario

**Quiero** transferir dinero de mi cuenta a otra

**Para** pagar o enviar dinero de forma inmediata

#### Criterios de aceptación

```
Dado que mi cuenta origen tiene 50.00
Y la cuenta destino tiene 10.00
Cuando transfiero 20.00 al destino
Entonces el saldo de origen es 30.00
Y el saldo de destino es 30.00
Y se registra un evento `TransferCreated` con `id` y `timestamp`
```

#### Notas técnicas

* Implementar en `TransferServiceTest.transfers_between_accounts_and_records_event`.
* Persistencia de cuentas mediante repositorio; evento en memoria.

---

### 2) Rechazar por fondos insuficientes

**Como** cliente

**Quiero** que el sistema impida transferir más de mi saldo

**Para** evitar sobregiros

#### Criterios de aceptación

```
Dado que mi cuenta origen tiene 5.00
Cuando intento transferir 6.00
Entonces la operación falla con "Insufficient funds"
Y no se generan cambios de saldo
Y no se registra transferencia
```

#### Notas técnicas

* Excepción en `Account.withdraw`.
* Test de dominio ya incluido.

---

### 3) Límite diario de transferencias por cuenta

**Como** banco

**Quiero** imponer un límite diario acumulado por cuenta origen

**Para** mitigar riesgo y cumplir políticas internas

#### Criterios de aceptación

```
Dado que hoy ya transferí 90.00 y el límite diario es 100.00
Cuando intento transferir 11.00
Entonces la operación falla con "Daily limit exceeded"
Y no hay cambios en los saldos
```

#### Notas técnicas

* Consulta a `DailyLimitRepository.todayTransferred`; actualización con `append` si procede.

---

### 4) Bloquear destino en lista negra (antifraude)

**Como** área de riesgos

**Quiero** bloquear transferencias a cuentas en lista negra

**Para** prevenir fraude

#### Criterios de aceptación

```
Dado que el destino está en blacklist
Cuando intento transferir cualquier monto
Entonces la operación falla con "Destination blacklisted"
Y no hay cambios de saldo
```

#### Notas técnicas

* `FraudChecker.isBlacklisted(to)` debe ser consultado **antes** de cargar límites o tocar saldos.

---

### 5) Generar id y timestamp desde los servicios

**Como** auditor

**Quiero** que cada transferencia tenga id único y hora exacta

**Para** trazabilidad y conciliación

#### Criterios de aceptación

```
Dado que el reloj retorna 2025-01-01T00:00:00Z
Y el generador de ids retorna UUID fijo
Cuando realizo una transferencia
Entonces el registro usa ese UUID y timestamp
```

#### Notas técnicas

* Inyección de `Clock`/`IdGenerator` para determinismo en tests.
