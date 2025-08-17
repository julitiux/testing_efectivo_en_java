package com.circulosiete.curso.testing.clase03.adapters.jdbc;

import com.circulosiete.curso.testing.clase03.Account;
import com.circulosiete.curso.testing.clase03.repository.AccountRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class JdbcAccountRepository implements AccountRepository {
    private final DataSource dataSource;

    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Account> findById(UUID uuid) {
        var select = "select * from account where id=?";
        try (
                var conn = dataSource.getConnection();
                var stmt = conn.prepareStatement(select);
        ) {
            stmt.setObject(1, uuid);
            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
                return Optional.empty();
            }
            var id = resultSet.getString("id");
            var balance = resultSet.getBigDecimal("balance_cents");
            return Optional.of(new Account(UUID.fromString(id), balance));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Account save(Account account) {
        String sql = """
                  INSERT INTO account (id, balance_cents)
                  VALUES (?, ?)
                  ON CONFLICT (id) DO UPDATE
                    SET balance_cents = ?                
                  RETURNING id, balance_cents
                """;
        try (
                var conn = dataSource.getConnection();
                var stmt = conn.prepareStatement(sql);
        ) {
            conn.setAutoCommit(false);
            stmt.setObject(1, account.getUuid());
            stmt.setBigDecimal(2, account.getBalance());
            stmt.setBigDecimal(3, account.getBalance());

            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Problema al guardar el registro");
                }
            }
            conn.commit();

            return account;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Account account) {

    }

    @Override
    public void deleteById(UUID uuid) {

    }

    @Override
    public Iterable<Account> findAll() {
        return null;
    }

    @Override
    public boolean existsById(UUID uuid) {
        return false;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public boolean exists(Account account) {
        return false;
    }

    @Override
    public void saveAll(Iterable<? extends Account> accounts) {

    }

    @Override
    public void deleteInBatch(Iterable<? extends Account> accounts) {

    }
}
