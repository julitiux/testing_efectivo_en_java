# Historias de usuario — Transferencias (TDD)

---


## Épica: Persistencia y Concurrencia

### 6) Persistir y leer cuentas desde la base de datos

**Como** operador de sistemas

**Quiero** almacenar cuentas y consultar sus saldos

**Para** soportar reinicios y concurrencia

#### Criterios de aceptación

```
Dado una cuenta persistida con 10.00
Cuando la leo
Entonces obtengo balance de 10.00
```

#### Notas técnicas

* Migraciones `Flyway` (`V1__init.sql`).
* IntegrationTesting con Testcontainers (`JdbcAccountRepositoryIntegrationTest`).

---

### 7) Bloqueo optimista en actualización de cuenta

**Como** banco

**Quiero** evitar condiciones de carrera al actualizar saldos

**Para** mantener integridad de datos

#### Criterios de aceptación

```
Dado que dos procesos leen la misma cuenta (versión N)
Cuando ambos intentan guardar con versión N
Entonces uno actualiza a N+1 y el otro falla con "Optimistic lock failed"
```

#### Notas técnicas

* `update ... where id=? and version=?`; incremento de `version`.