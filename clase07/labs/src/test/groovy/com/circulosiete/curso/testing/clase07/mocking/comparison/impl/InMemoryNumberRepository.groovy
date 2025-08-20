package com.circulosiete.curso.testing.clase07.mocking.comparison.impl

import com.circulosiete.curso.testing.clase07.mocking.comparison.NumberRepository

class InMemoryNumberRepository implements NumberRepository {
    private final Iterator<Integer> it
    private final Map<Long, Integer> store = [:]

    InMemoryNumberRepository(List<Integer> seed = [1,2,3]) {
        this.it = seed.iterator()
        store[21L] = 100
    }

    @Override
    int nextNumber() {
        return it.hasNext() ? it.next() : 0
    }

    @Override
    int byId(long id) {
        return store.getOrDefault(id, 0)
    }
}
