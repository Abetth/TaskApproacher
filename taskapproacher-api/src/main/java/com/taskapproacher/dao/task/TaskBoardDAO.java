package com.taskapproacher.dao.task;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.interfaces.dao.GenericDAO;

import com.taskapproacher.interfaces.dao.RelatedEntityDAO;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskBoardDAO implements GenericDAO<TaskBoard>, RelatedEntityDAO<Task, UUID> {

    private final SessionFactory sessionFactory;

    @Autowired
    public TaskBoardDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<TaskBoard> findByID(UUID uuid) {
        Transaction transaction = null;
        TaskBoard taskBoard = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            taskBoard = session.get(TaskBoard.class, uuid);
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("Failed to find task board by id");
        }

        return Optional.ofNullable(taskBoard);
    }

    @Override
    public List<Task> findRelatedEntitiesByID(UUID uuid) {
        Transaction transaction = null;
        List<Task> tasks;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            tasks = session.createQuery("FROM Task WHERE taskBoard.ID = :boardID", Task.class)
                    .setParameter("boardID", uuid)
                    .getResultList();
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("Failed to get tasks");
        }
        return tasks;
    }

    @Override
    public TaskBoard save(TaskBoard entity) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(entity);
                transaction.commit();
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw new HibernateException("Wrong data");
            }
        } catch (Exception exception) {
            throw new HibernateException("Failed to save entry: " + exception.getMessage());
        }
        return entity;
    }

    @Override
    public TaskBoard update(TaskBoard entity) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            try {
                TaskBoard merged = session.merge(entity);
                transaction.commit();
                return merged;
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw new HibernateException("Failed to save changes");
            }
        } catch (Exception exception) {
            throw new HibernateException("Failed to update entry" + exception.getMessage());
        }
    }

    @Override
    public void delete(UUID uuid) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM TaskBoard WHERE ID = :boardID")
                    .setParameter("boardID", uuid)
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
