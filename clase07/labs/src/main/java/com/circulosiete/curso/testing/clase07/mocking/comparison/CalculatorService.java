package com.circulosiete.curso.testing.clase07.mocking.comparison;

public class CalculatorService {
    private final NumberRepository repository;
    private final Notifier notifier;
    private final int threshold;

    public CalculatorService(NumberRepository repository, Notifier notifier, int threshold) {
        this.repository = repository;
        this.notifier = notifier;
        this.threshold = threshold;
    }

    public int sumNextTwo() {
        int a = repository.nextNumber();
        int b = repository.nextNumber();
        int result = a + b;
        if (result > threshold) {
            notifier.send("sum=%d".formatted(result));
        }
        return result;
    }

    int doubleOfId(long id) {
        return repository.byId(id) * 2;
    }
}
