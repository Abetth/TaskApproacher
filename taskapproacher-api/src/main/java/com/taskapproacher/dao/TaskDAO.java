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
import java.util.UUID;

@Repository
public class TaskDAO implements GenericDAO<Task> {
    SessionFactory sessionFactory;
    List<Task> tasks;

    public TaskDAO() {
        sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
    }

    @Override
    public Task findById(UUID taskId) {
        Transaction transaction = null;
        Task task = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            task = session.get(Task.class, taskId);
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
    public void delete(UUID taskId) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("delete from Task where id = :taskId")
                    .setParameter("taskId", taskId)
                    .executeUpdate();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to delete entry", e);
        }
    }
}
