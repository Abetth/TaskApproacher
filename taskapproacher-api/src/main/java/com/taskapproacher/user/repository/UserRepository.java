package com.taskapproacher.user.repository;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.interfaces.repository.GenericRepository;
import com.taskapproacher.common.interfaces.repository.RelatedEntityRepository;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardResponse;
import com.taskapproacher.user.model.User;

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
import java.util.stream.Collectors;

@Repository
public class UserRepository implements GenericRepository<User>, RelatedEntityRepository<TaskBoardResponse, UUID> {
    SessionFactory sessionFactory;

    @Autowired
    public UserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<User> findByUsername(String username) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Query<User> query = session.createQuery(
                                               """

                                                       FROM User\s
                                                       WHERE username = :username
                                                       """,
                                               User.class)
                                       .setParameter("username", username);
            transaction.commit();

            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to find user by username: " + username, exception);
        }
    }

    public boolean isUserExists(User user) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            Boolean isExists;
            transaction = session.beginTransaction();
            Query<Boolean> query = session.createQuery(
                    """
                            SELECT CASE WHEN EXISTS\s
                            (FROM User WHERE username = :username OR email = :email)\s
                            THEN TRUE ELSE FALSE END
                            """,
                    Boolean.class);
            query.setParameter("username", user.getUsername());
            query.setParameter("email", user.getEmail());
            isExists = query.getSingleResult();
            transaction.commit();

            return isExists;
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to find user: " + user.getUsername(), exception);
        }
    }

    public boolean isUsernameAlreadyTaken(String username) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            Boolean isTaken;
            transaction = session.beginTransaction();
            Query<Boolean> query = session.createQuery(
                    """
                            SELECT CASE WHEN EXISTS\s
                            (FROM User WHERE username = :username)\s
                            THEN TRUE ELSE FALSE END
                            """,
                    Boolean.class);
            query.setParameter("username", username);
            isTaken = query.getSingleResult();
            transaction.commit();

            return isTaken;
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to find user with username: " + username, exception);
        }
    }

    public boolean isEmailAlreadyTaken(String email) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            Boolean isTaken;
            transaction = session.beginTransaction();
            Query<Boolean> query = session.createQuery(
                    """

                            SELECT CASE WHEN EXISTS\s
                            (FROM User WHERE email = :email)\s
                            THEN TRUE ELSE FALSE END
                            """,
                    Boolean.class);
            query.setParameter("email", email);
            isTaken = query.getSingleResult();
            transaction.commit();

            return isTaken;
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to find user with email" + email, exception);
        }
    }

    @Override
    public Optional<User> findByID(UUID userID) {
        Transaction transaction = null;
        User user;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            user = session.find(User.class, userID);
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to find user by id: " + userID, exception);
        }

        return Optional.ofNullable(user);
    }

    @Override
    public List<TaskBoardResponse> findRelatedEntitiesByID(UUID userID) {
        Transaction transaction = null;
        List<TaskBoard> taskBoards;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            taskBoards = session.createQuery(
                                        """
                                                FROM TaskBoard\s
                                                WHERE user.ID = :id
                                                """,
                                        TaskBoard.class)
                                .setParameter("id", userID)
                                .getResultList();
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to get task boards for user: " + userID, exception);
        }

        return taskBoards.stream().map(TaskBoardResponse::new).collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        Transaction transaction;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(user);
                transaction.commit();
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }
        } catch (Exception exception) {
            if (exception instanceof ConstraintViolationException) {
                throw new ConstraintViolationException(
                        ExceptionMessage.INVALID_USERNAME_LENGTH.toString(),
                        ((ConstraintViolationException) exception).getConstraintViolations()
                );
            }

            throw new HibernateException("[DB] Failed to save user: " + user.getUsername(), exception);
        }
        return user;
    }

    @Override
    public User update(User user) {
        Transaction transaction;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                User merged = session.merge(user);
                transaction.commit();
                return merged;
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }
        } catch (Exception exception) {
            if (exception instanceof ConstraintViolationException) {
                throw new ConstraintViolationException(
                        ExceptionMessage.INVALID_USERNAME_LENGTH.toString(),
                        ((ConstraintViolationException) exception).getConstraintViolations()
                );
            }

            throw new HibernateException("[DB] Failed to update user: " + user.getUsername(), exception);
        }
    }

    @Override
    public void delete(User user) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.remove(user);
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to delete user: " + user.getID(), exception);
        }
    }
}
