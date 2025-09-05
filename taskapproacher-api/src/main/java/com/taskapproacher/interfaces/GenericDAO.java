package com.taskapproacher.interfaces;

import java.util.Optional;
import java.util.UUID;

public interface GenericDAO<T> {

    Optional<T> findByID(UUID uuid);
    T save(T entity);
    T update(T entity);
    void delete(UUID uuid);
}
