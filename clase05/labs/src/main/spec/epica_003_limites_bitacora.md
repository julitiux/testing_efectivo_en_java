# Historias de usuario — Transferencias (TDD)

---


## Épica: Límites y Registro de actividad

### 8) Acumular monto transferido del día

**Como** banco

**Quiero** llevar un registro del total enviado por cuenta cada día

**Para** evaluar el límite diario

#### Criterios de aceptación

```
Dado que el total enviado hoy es 40.00
Cuando transfiero 10.00
Entonces el total enviado hoy pasa a 50.00
```

#### Notas técnicas

* `DailyLimitRepository.append(from, amount, at)` tras éxito.
* Test doble: éxito y fallo por límite.

---

### 9) Registrar transferencia auditada

**Como** auditor

**Quiero** registrar cada transferencia con from, to, amount, at

**Para** cumplir trazabilidad

#### Criterios de aceptación

```
Dado una transferencia exitosa
Entonces se llama a TransferRepository.record(id, from, to, amount, at)
```

#### Notas técnicas

* Verificación en test unitario con Mockito.


---

## Notas de testing (guía rápida)

* **Unit**: `TransferService` (mocks para servicios), reglas de negocio y excepciones.
* **Integración**: `JdbcAccountRepository` con Postgres Testcontainers + Flyway.


