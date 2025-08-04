package com.taskapproacher.dao;

import com.taskapproacher.entities.Task;
import com.taskapproacher.entities.TaskBoard;
import com.taskapproacher.hibernate.HibernateSessionFactoryUtil;

import com.taskapproacher.interfaces.*;

import jakarta.persistence.PersistenceException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.DataException;

import java.util.List;
import java.util.Objects;

public class TaskDAO implements GenericDAO<Task>, RelatedEntityDAO<Task, TaskBoard> {
    SessionFactory sessionFactory;
    List<Task> tasks;

    public TaskDAO() {
        sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
    }

    @Override
    public List<Task> findByRelatedObject(TaskBoard relatedObject) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            tasks = session.createQuery("from Task where taskBoard.id = :boardId", Task.class)
                    .setParameter("boardId", relatedObject.getId())
                    .getResultList();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to get tasks", e);
        }
        return tasks;
    }

    @Override
    public Task findById(Long id) {
        Transaction transaction = null;
        Task task = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            task = session.get(Task.class, id);
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to find task by id", e);
        }
        return task;
    }

    @Override
    public void save(Task entity) {
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
    }

    @Override
    public void update(Task entity) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            if (Objects.isNull(session.find(Task.class, entity.getId()))) {
                throw new RuntimeException("Entry is missing");
            }

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
    }

    @Override
    public void delete(Task entity) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to delete entry", e);
        }
    }
}
