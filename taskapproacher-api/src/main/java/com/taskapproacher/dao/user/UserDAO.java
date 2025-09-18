package com.taskapproacher.dao.user;

import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.interfaces.dao.GenericDAO;
import com.taskapproacher.interfaces.dao.RelatedEntityDAO;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserDAO implements GenericDAO<User>, RelatedEntityDAO<TaskBoardResponse, UUID> {
    SessionFactory sessionFactory;

    @Autowired
    public UserDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<User> findByUsername(String username) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class)
                    .setParameter("username", username);
            transaction.commit();

            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("Failed to find user by username");
        }
    }

    public boolean isUserExists(User user) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            Boolean isExists = false;
            transaction = session.beginTransaction();
            Query<Boolean> query = session.createQuery(
                    "SELECT CASE WHEN EXISTS (FROM User WHERE username = :username OR email = :email) THEN TRUE ELSE FALSE END"
            , Boolean.class);
            query.setParameter("username", user.getUsername());
            query.setParameter("email", user.getEmail());
            isExists = query.getSingleResult();
            transaction.commit();

            return isExists;
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("Failed to find user by name and mail");
        }
    }

    @Override
    public Optional<User> findByID(UUID userID) {
        Transaction transaction = null;
        User user = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            user = session.find(User.class, userID);
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("Failed to get user by id");
        }

        return Optional.ofNullable(user);
    }

    @Override
    public List<TaskBoardResponse> findRelatedEntitiesByID(UUID uuid) {
        Transaction transaction = null;
        List<TaskBoard> taskBoards;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            taskBoards = session.createQuery("FROM TaskBoard WHERE user.ID = :id", TaskBoard.class)
                    .setParameter("id", uuid)
                    .getResultList();
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("Failed to get task boards");
        }

        return taskBoards.stream().map(TaskBoardResponse::new).collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(user);
                transaction.commit();
            } catch (Exception exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw new HibernateException("Wrong data");
            }
        } catch (Exception exception) {
            throw new HibernateException("Failed to save user: " + exception.getMessage());
        }
        return user;
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;

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
                throw new HibernateException("Failed to save changes");
            }
        } catch (Exception exception) {
            throw new HibernateException("Failed to update entry" + exception.getMessage());
        }
    }

    @Override
    public void delete(UUID userID) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM User WHERE ID = :userID")
                    .setParameter("userID", userID)
                    .executeUpdate();
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new HibernateException("Failed to delete entry");
        }
    }
}
