package com.taskapproacher.interfaces;

public interface GenericDAO<E> {
    E findById(Long id);
    void save(E entity);
    void update(E entity);
    void delete(E entity);
}
