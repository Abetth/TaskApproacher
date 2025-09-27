package com.taskapproacher.repository.task;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.interfaces.dao.GenericDAO;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskRepository implements GenericDAO<Task> {
    SessionFactory sessionFactory;

    @Autowired
    public TaskRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Task> findByID(UUID taskID) {
        Transaction transaction = null;
        Task task;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            task = session.find(Task.class, taskID);
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to find task by id: " + taskID, exception);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public Task save(Task task) {
        Transaction transaction;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(task);
                transaction.commit();
                return task;
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }
        } catch (Exception exception) {
            throw new HibernateException("[DB] Failed to save task: " + task, exception);
        }
    }

    @Override
    public Task update(Task task) {
        Transaction transaction;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                Task merged = session.merge(task);
                transaction.commit();
                return merged;
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }
        } catch (Exception exception) {
            throw new HibernateException("[DB] Failed to update task: " + task, exception);
        }
    }

    @Override
    public void delete(Task task) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.remove(task);
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new HibernateException("[DB] Failed to delete task: " + task.getID(), exception);
        }
    }
}
