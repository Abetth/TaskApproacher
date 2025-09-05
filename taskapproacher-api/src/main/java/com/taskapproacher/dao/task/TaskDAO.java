package com.taskapproacher.dao.task;

import com.taskapproacher.entity.task.Task;

import com.taskapproacher.hibernate.HibernateSessionFactoryUtil;
import com.taskapproacher.interfaces.*;

import jakarta.persistence.PersistenceException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.DataException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskDAO implements GenericDAO<Task> {
    SessionFactory sessionFactory;
    List<Task> tasks;

    public TaskDAO() {
        sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
    }

    @Override
    public Optional<Task> findByID(UUID taskID) {
        Transaction transaction = null;
        Task task = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            task = session.get(Task.class, taskID);
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to find task by id", e);
        }
        return Optional.of(task);
    }

    @Override
    public Task save(Task entity) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(entity);
                transaction.commit();
            } catch (DataException e) {
                transaction.rollback();
                throw new RuntimeException("Wrong data format", e);
            }
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to save task", e);
        }
        return entity;
    }

    @Override
    public Task update(Task entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            try {
                session.merge(entity);
                transaction.commit();
            } catch (PersistenceException e) {
                transaction.rollback();
                throw new RuntimeException("Failed to save changes", e);
            }
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to update entry", e);
        }
        return entity;
    }

    @Override
    public void delete(UUID taskID) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM Task WHERE ID = :taskID")
                    .setParameter("taskID", taskID)
                    .executeUpdate();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to delete entry", e);
        }
    }
}
