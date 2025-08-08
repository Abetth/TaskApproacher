package com.taskapproacher.interfaces;

import java.util.UUID;

public interface GenericDAO<T> {
    T findById(UUID uuid);
    void save(T entity);
    void update(T entity);
    void delete(UUID uuid);
}
