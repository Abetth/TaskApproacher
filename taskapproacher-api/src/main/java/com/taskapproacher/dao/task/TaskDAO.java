package com.taskapproacher.dao.task;

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
public class TaskDAO implements GenericDAO<Task> {
    SessionFactory sessionFactory;

    @Autowired
    public TaskDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Task> findByID(UUID taskID) {
        Transaction transaction = null;
        Task task;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            task = session.get(Task.class, taskID);
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
    public int delete(UUID taskID) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            int rowsAffected = session.createQuery(
                                              """
                                                      DELETE FROM Task\s
                                                       WHERE ID = :taskID
                                                       """
                                      )
                                      .setParameter("taskID", taskID)
                                      .executeUpdate();
            transaction.commit();
            return rowsAffected;
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new HibernateException("[DB] Failed to delete task: " + taskID, exception);
        }
    }
}
