package com.taskapproacher.interfaces;

public interface GenericDAO<T> {
    T findById(Long id);
    void save(T entity);
    void update(T entity);
    void delete(Long id);
}
