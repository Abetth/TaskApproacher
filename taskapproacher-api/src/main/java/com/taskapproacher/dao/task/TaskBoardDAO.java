package com.taskapproacher.dao.task;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.TaskBoardResponse;
import com.taskapproacher.hibernate.HibernateSessionFactoryUtil;
import com.taskapproacher.interfaces.GenericDAO;

import com.taskapproacher.interfaces.RelatedEntityDAO;
import jakarta.persistence.PersistenceException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.hibernate.Transaction;
import org.hibernate.exception.DataException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskBoardDAO implements GenericDAO<TaskBoard>, RelatedEntityDAO<Task, UUID> {

    private final SessionFactory sessionFactory;
    List<TaskBoard> taskBoards;

    public TaskBoardDAO() {
        sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
    }

    @Override
    public Optional<TaskBoard> findById(UUID uuid) {
        TaskBoard taskBoard = null;

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            taskBoard = session.get(TaskBoard.class, uuid);
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to find task board by id", e);
        }

        return Optional.of(taskBoard);
    }

    @Override
    public List<Task> findRelatedEntitiesByUUID(UUID uuid) {
        Transaction transaction = null;
        List<Task> tasks;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            tasks = session.createQuery("FROM Task WHERE taskBoard.id = :boardId", Task.class)
                    .setParameter("boardId", uuid)
                    .getResultList();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to get tasks", e);
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
            } catch (DataException e) {
                transaction.rollback();
                throw new RuntimeException("Wrong data format", e);
            }
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to save entry");
        }
        return entity;
    }

    @Override
    public TaskBoard update(TaskBoard entity) {
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
    public void delete(UUID uuid) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM TaskBoard WHERE id = :boardId")
                    .setParameter("boardId", uuid)
                    .executeUpdate();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to delete entry", e);
        }
    }
}
