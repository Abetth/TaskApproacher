package com.taskapproacher.dao;

import com.taskapproacher.entity.Task;
import com.taskapproacher.entity.TaskBoard;
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

import java.util.List;
import java.util.Objects;

@Repository
public class TaskBoardDAO implements GenericDAO<TaskBoard>, RelatedEntityDAO<Task, Long> {

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
    public List<Task> findRelatedEntities(Long id) {
        Transaction transaction = null;
        List<Task> tasks;

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
                entity.setTasks(session.find(TaskBoard.class, entity.getId()).getTasks());
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
            session.createQuery("delete from TaskBoard where id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to delete entry", e);
        }
    }
}
