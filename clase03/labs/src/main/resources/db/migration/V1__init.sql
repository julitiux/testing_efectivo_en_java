-- Flyway migration
create table if not exists account(
  id uuid primary key,
  balance_cents bigint not null,
  version bigint not null default 0
);

create table if not exists transfer(
  id uuid primary key,
  from_id uuid not null,
  to_id uuid not null,
  amount_cents bigint not null,
  at timestamp not null
);
