-- Habilita función para UUIDs si está disponible (opcional pero recomendado)
-- Usa pgcrypto (gen_random_uuid) o uuid-ossp (uuid_generate_v4)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================
-- TABLE: account
-- =========================================
CREATE TABLE IF NOT EXISTS account (
    id              UUID PRIMARY KEY,
    account_number  VARCHAR(34)  NOT NULL,
    type            VARCHAR(20)  NOT NULL,  -- AccountType (Enum como STRING)
    status          VARCHAR(20)  NOT NULL,  -- AccountStatus (Enum como STRING)
    amount          NUMERIC(19,4) NOT NULL, -- Money.amount (embedded)
    currency        VARCHAR(3)    NOT NULL, -- Money.currency (embedded)
    customer_id     UUID          NOT NULL,
    overdraft_limit NUMERIC(19,4),
    version         BIGINT        NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uq_account_number UNIQUE (account_number)
    );

-- Índices según @Table(indexes=...)
CREATE INDEX IF NOT EXISTS idx_account_customer ON account (customer_id);
CREATE INDEX IF NOT EXISTS idx_account_status   ON account (status);

-- =========================================
-- TABLE: processed_operation
-- =========================================
CREATE TABLE IF NOT EXISTS processed_operation (
    id          UUID PRIMARY KEY,
    account_id  UUID         NOT NULL,
    request_id  VARCHAR(64)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_processed_operation_account_request UNIQUE (account_id, request_id)
    );



-- Tabla principal
CREATE TABLE IF NOT EXISTS transfer (
                                        id               UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- o uuid_generate_v4()
    request_id       VARCHAR(64)  NOT NULL,
    from_account_id  UUID         NOT NULL,
    to_account_id    UUID         NOT NULL,
    amount           NUMERIC(19,4) NOT NULL,
    currency         CHAR(3)      NOT NULL,
    status           VARCHAR(16)  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at     TIMESTAMPTZ  NULL,

    -- Restricción de unicidad como en @UniqueConstraint(name="transfer_request", columnNames="request_id")
    CONSTRAINT transfer_request UNIQUE (request_id),

    -- Enum por cadena: valida valores permitidos
    CONSTRAINT transfer_status_chk CHECK (status IN ('PENDING', 'COMPLETED')),

    -- Asegura ISO 4217 de 3 letras (mayúsculas)
    CONSTRAINT transfer_currency_chk CHECK (currency ~ '^[A-Z]{3}$'),

    -- Evita from y to iguales
    CONSTRAINT transfer_distinct_accounts_chk CHECK (from_account_id <> to_account_id),

    -- completed_at solo si está COMPLETED
    CONSTRAINT transfer_completed_at_consistency_chk
    CHECK ((status = 'COMPLETED' AND completed_at IS NOT NULL)
    OR (status = 'PENDING'   AND completed_at IS NULL))
    );

-- Índices útiles para consultas típicas
CREATE INDEX IF NOT EXISTS idx_transfer_request_id       ON transfer (request_id);
CREATE INDEX IF NOT EXISTS idx_transfer_from_account_id  ON transfer (from_account_id);
CREATE INDEX IF NOT EXISTS idx_transfer_to_account_id    ON transfer (to_account_id);
CREATE INDEX IF NOT EXISTS idx_transfer_status_created   ON transfer (status, created_at DESC);

