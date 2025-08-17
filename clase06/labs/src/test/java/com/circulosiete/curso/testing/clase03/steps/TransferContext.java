package com.circulosiete.curso.testing.clase03.steps;

import com.circulosiete.curso.testing.clase03.TransferService;
import com.circulosiete.curso.testing.clase03.repository.AccountRepository;

import javax.sql.DataSource;

public class TransferContext {
    private DataSource dataSource;
    private AccountRepository accountRepository;
    private TransferService transferService;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }

    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public TransferService getTransferService() {
        return transferService;
    }

    public void setTransferService(TransferService transferService) {
        this.transferService = transferService;
    }
}
