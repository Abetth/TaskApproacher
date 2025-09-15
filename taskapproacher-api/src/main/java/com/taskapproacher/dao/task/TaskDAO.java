package com.taskapproacher.dao.task;

import com.taskapproacher.entity.task.Task;

import com.taskapproacher.hibernate.HibernateSessionFactoryUtil;
import com.taskapproacher.interfaces.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;

import org.hibernate.*;
import org.hibernate.exception.DataException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskDAO implements GenericDAO<Task> {
    SessionFactory sessionFactory;

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
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("Failed to find task by id");
        }
        return Optional.ofNullable(task);
    }

    @Override
    public Task save(Task entity) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(entity);
                transaction.commit();
                return entity;
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw new HibernateException("Wrong data format");
            }
        } catch (Exception exception) {
            throw new HibernateException("Failed to save entry: " + exception.getMessage());
        }
    }

    @Override
    public Task update(Task entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.merge(entity);
                transaction.commit();
                return entity;
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw new HibernateException("Failed to save changes");
            }
        } catch (Exception exception) {
            throw new HibernateException("Failed to update entry: " + exception.getMessage());
        }
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
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("Failed to delete entry");
        }
    }
}
