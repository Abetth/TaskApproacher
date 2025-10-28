package com.taskapproacher.task.repository;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.interfaces.repository.GenericRepository;
import com.taskapproacher.common.interfaces.repository.RelatedEntityRepository;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;

import jakarta.validation.ConstraintViolationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskBoardRepository implements GenericRepository<TaskBoard>, RelatedEntityRepository<Task, UUID> {
    private final SessionFactory sessionFactory;

    @Autowired
    public TaskBoardRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<TaskBoard> findByID(UUID taskBoardID) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            TaskBoard taskBoard = session.find(TaskBoard.class, taskBoardID);

            transaction.commit();

            return Optional.ofNullable(taskBoard);
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw new HibernateException("[DB] Failed to find task board by id: " + taskBoardID, exception);
        }

    }

    @Override
    public List<Task> findRelatedEntitiesByID(UUID taskBoardID) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Query<Task> query = session.createQuery(
                                               """
                                                       FROM Task
                                                       WHERE taskBoard.ID = :boardID
                                                       """,
                                               Task.class)
                                       .setParameter("boardID", taskBoardID);
            List<Task> tasks = query.getResultList();

            transaction.commit();

            return tasks;
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to get tasks for board: " + taskBoardID, exception);
        }
    }

    @Override
    public TaskBoard save(TaskBoard taskBoard) {
        Transaction transaction;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(taskBoard);

                transaction.commit();

                return taskBoard;
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }
        } catch (Exception exception) {
            if (exception instanceof ConstraintViolationException CVexception) {
                throw new ConstraintViolationException(
                        ExceptionMessage.INVALID_TASK_BOARD_TITLE_LENGTH.toString(),
                        CVexception.getConstraintViolations()
                );
            }
            throw new HibernateException("[DB] Failed to save task board: " + taskBoard, exception);
        }
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
            if (exception instanceof ConstraintViolationException CVexception) {
                throw new ConstraintViolationException(
                        ExceptionMessage.INVALID_TASK_BOARD_TITLE_LENGTH.toString(),
                        CVexception.getConstraintViolations()
                );
            }
            throw new HibernateException("[DB] Failed to update task board: " + taskBoard, exception);
        }
    }

    @Override
    public void delete(TaskBoard taskBoard) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            session.remove(taskBoard);

            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to delete task board: " + taskBoard.getID(), exception);
        }
    }
}
