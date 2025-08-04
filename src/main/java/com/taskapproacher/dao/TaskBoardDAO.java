package com.taskapproacher.dao;

import com.taskapproacher.entities.TaskBoard;
import com.taskapproacher.hibernate.HibernateSessionFactoryUtil;

import com.taskapproacher.interfaces.GenericDAO;

import jakarta.persistence.PersistenceException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.DataException;

import java.util.List;
import java.util.Objects;

public class TaskBoardDAO implements GenericDAO<TaskBoard> {

    private final SessionFactory sessionFactory;
    List<TaskBoard> taskBoards;

    public TaskBoardDAO() {
        sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
    }

    public List<TaskBoard> findAll() {
        this.taskBoards = null;

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            taskBoards = session.createQuery("from TaskBoard ", TaskBoard.class).getResultList();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to get Task Boards", e);
        }

        return taskBoards;
    }

    @Override
    public TaskBoard findById(Long id) {
        TaskBoard taskBoard = null;

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            taskBoard = session.get(TaskBoard.class, id);
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to find task board by id", e);
        }

        return taskBoard;
    }

    @Override
    public void save(TaskBoard entity) {
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
            throw new RuntimeException("Failed to save task board");
        }
    }

    @Override
    public void update(TaskBoard entity) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            if (Objects.isNull(session.find(TaskBoard.class, entity.getId()))) {
                throw new RuntimeException("Database entry is missing");
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
    public void delete(TaskBoard entity) {
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
