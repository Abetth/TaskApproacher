package com.taskapproacher.common.interfaces.repository;

import java.util.Optional;
import java.util.UUID;

public interface GenericRepository<T> {
    Optional<T> findByID(UUID uuid);
    T save(T entity);
    T update(T entity);
    void delete(T entity);
}
