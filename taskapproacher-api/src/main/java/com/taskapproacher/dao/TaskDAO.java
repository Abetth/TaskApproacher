package com.taskapproacher.dao;

import com.taskapproacher.entity.Task;
import com.taskapproacher.entity.TaskBoard;

import com.taskapproacher.hibernate.HibernateSessionFactoryUtil;
import com.taskapproacher.interfaces.*;

import jakarta.persistence.PersistenceException;

import jakarta.transaction.Transactional;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class TaskDAO implements GenericDAO<Task>, RelatedEntityDAO<Task, TaskBoard> {
    SessionFactory sessionFactory;
    List<Task> tasks;

    public TaskDAO() {
        sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
    }

    @Override
    public List<Task> findByRelatedBoard(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            tasks = session.createQuery("from Task where taskBoard.id = :boardId", Task.class)
                    .setParameter("boardId", id)
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
    public void delete(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("delete from Task where id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to delete entry", e);
        }
    }
}
