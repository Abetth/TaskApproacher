package com.taskapproacher.user.repository;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.interfaces.repository.GenericRepository;
import com.taskapproacher.common.interfaces.repository.RelatedEntityRepository;
import com.taskapproacher.task.model.TaskBoard;
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

@Repository
public class UserRepository implements GenericRepository<User>, RelatedEntityRepository<TaskBoard, UUID> {
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

                            FROM User
                            WHERE username = :username
                            """,
                    User.class);
            query.setParameter("username", username);
            User user = query.uniqueResult();

            transaction.commit();

            return Optional.ofNullable(user);
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
            transaction = session.beginTransaction();

            Query<Boolean> query = session.createQuery(
                    """
                            SELECT CASE WHEN EXISTS
                            (FROM User WHERE username = :username OR email = :email)
                            THEN TRUE ELSE FALSE END
                            """,
                    Boolean.class);
            query.setParameter("username", user.getUsername());
            query.setParameter("email", user.getEmail());
            Boolean isExists = query.getSingleResult();

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
            transaction = session.beginTransaction();

            Query<Boolean> query = session.createQuery(
                    """
                            SELECT CASE WHEN EXISTS
                            (FROM User WHERE username = :username)
                            THEN TRUE ELSE FALSE END
                            """,
                    Boolean.class);
            query.setParameter("username", username);
            Boolean isTaken = query.getSingleResult();

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
            transaction = session.beginTransaction();

            Query<Boolean> query = session.createQuery(
                    """

                            SELECT CASE WHEN EXISTS
                            (FROM User WHERE email = :email)
                            THEN TRUE ELSE FALSE END
                            """,
                    Boolean.class);
            query.setParameter("email", email);
            Boolean isTaken = query.getSingleResult();

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

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            User user = session.find(User.class, userID);

            transaction.commit();

            return Optional.ofNullable(user);
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to find user by id: " + userID, exception);
        }
    }

    @Override
    public List<TaskBoard> findRelatedEntitiesByID(UUID userID) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Query<TaskBoard> query = session.createQuery(
                                        """
                                                FROM TaskBoard
                                                WHERE user.ID = :id
                                                """,
                                        TaskBoard.class)
                                .setParameter("id", userID);
            List<TaskBoard> taskBoards = query.getResultList();

            transaction.commit();

            return taskBoards;
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("[DB] Failed to get task boards for user: " + userID, exception);
        }
    }

    @Override
    public User save(User user) {
        Transaction transaction;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(user);

                transaction.commit();

                return user;
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }
        } catch (Exception exception) {
            if (exception instanceof ConstraintViolationException CVexception) {
                throw new ConstraintViolationException(
                        ExceptionMessage.INVALID_USERNAME_LENGTH.toString(),
                        CVexception.getConstraintViolations()
                );
            }
            throw new HibernateException("[DB] Failed to save user: " + user.getUsername(), exception);
        }
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
            if (exception instanceof ConstraintViolationException CVexception) {
                throw new ConstraintViolationException(
                        ExceptionMessage.INVALID_USERNAME_LENGTH.toString(),
                        CVexception.getConstraintViolations()
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
