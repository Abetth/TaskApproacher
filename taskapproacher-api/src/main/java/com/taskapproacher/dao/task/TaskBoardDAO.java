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
    public Optional<TaskBoard> findByID(UUID taskBoardID) {
        Transaction transaction = null;
        TaskBoard taskBoard;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            taskBoard = session.get(TaskBoard.class, taskBoardID);
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new HibernateException("[DB] Failed to find task board by id: " + taskBoardID, exception);
        }

        return Optional.ofNullable(taskBoard);
    }

    @Override
    public List<Task> findRelatedEntitiesByID(UUID taskBoardID) {
        Transaction transaction = null;
        List<Task> tasks;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            tasks = session.createQuery(
                                   """
                                           FROM Task\s
                                            WHERE taskBoard.ID = :boardID
                                           """,
                                   Task.class)
                           .setParameter("boardID", taskBoardID)
                           .getResultList();
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to get tasks for board: " + taskBoardID, exception);
        }
        return tasks;
    }

    @Override
    public TaskBoard save(TaskBoard taskBoard) {
        Transaction transaction;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(taskBoard);
                transaction.commit();
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }
        } catch (Exception exception) {
            throw new HibernateException("[DB] Failed to save task board: " + taskBoard, exception);
        }
        return taskBoard;
    }

    @Override
    public TaskBoard update(TaskBoard taskBoard) {
        Transaction transaction;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            try {
                TaskBoard merged = session.merge(taskBoard);
                transaction.commit();
                return merged;
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }
        } catch (Exception exception) {
            throw new HibernateException("[DB] Failed to update task board: " + taskBoard, exception);
        }
    }

    @Override
    public int delete(UUID taskBoardID) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            int rowsAffected = session.createQuery(
                                              """
                                                      DELETE FROM TaskBoard\s
                                                       WHERE ID = :boardID
                                                      """
                                      )
                                      .setParameter("boardID", taskBoardID)
                                      .executeUpdate();
            transaction.commit();
            return rowsAffected;
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to delete task board: " + taskBoardID, exception);
        }
    }
}
