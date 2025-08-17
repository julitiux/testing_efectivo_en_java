package com.circulosiete.curso.testing.clase03.repository;

import com.circulosiete.curso.testing.clase03.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Optional<Account> findById(UUID uuid);

    Account save(Account account);

    void delete(Account account);

    void deleteById(UUID uuid);

    Iterable<Account> findAll();

    boolean existsById(UUID uuid);

    long count();

    boolean exists(Account account);

    void saveAll(Iterable<? extends Account> accounts);

    void deleteInBatch(Iterable<? extends Account> accounts);

}
