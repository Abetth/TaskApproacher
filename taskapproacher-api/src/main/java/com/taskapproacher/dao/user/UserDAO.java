package com.taskapproacher.dao.user;

import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.hibernate.HibernateSessionFactoryUtil;
import com.taskapproacher.interfaces.GenericDAO;
import com.taskapproacher.interfaces.RelatedEntityDAO;
import jakarta.persistence.PersistenceException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.DataException;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserDAO implements GenericDAO<User>, RelatedEntityDAO<TaskBoardResponse, UUID> {
    SessionFactory sessionFactory;

    public UserDAO() {
        this.sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
    }

    public Optional<User> findByUsername(String username) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class)
                    .setParameter("username", username);
            return Optional.ofNullable(query.uniqueResult());
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to find user by username");
        }
    }

    public boolean isUserExists(User user) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            Boolean isExists = false;
            transaction = session.beginTransaction();
            Query<Boolean> query = session.createQuery(
                    "SELECT CASE WHEN EXISTS (FROM User WHERE username = :username AND email = :email) THEN TRUE ELSE FALSE END"
            , Boolean.class);
            query.setParameter("username", user.getUsername());
            query.setParameter("email", user.getEmail());
            isExists = query.getSingleResult();
            transaction.commit();

            return isExists;
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to find user by name and mail");
        }
    }

    @Override
    public Optional<User> findById(UUID userId) {
        Transaction transaction;
        User user = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            user = session.find(User.class, userId);
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to get user by id", e);
        }

        return Optional.of(user);
    }

    @Override
    public List<TaskBoardResponse> findRelatedEntitiesByID(UUID uuid) {
        Transaction transaction = null;
        List<TaskBoard> taskBoards;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            taskBoards = session.createQuery("FROM TaskBoard WHERE user.id = :id", TaskBoard.class)
                    .setParameter("id", uuid)
                    .getResultList();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to get task boards");
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
            } catch (DataException e) {
                transaction.rollback();
                throw new RuntimeException("Wrong data format", e);
            }
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to save user", e);
        }
        return user;
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            try {
                session.merge(user);
                transaction.commit();
            } catch (PersistenceException e) {
                transaction.rollback();
                throw new RuntimeException("Failed to save changes", e);
            }
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to update entry", e);
        }
        return user;
    }

    @Override
    public void delete(UUID userId) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM User WHERE id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            transaction.commit();
        } catch (HibernateException e) {
            throw new RuntimeException("Failed to delete entry", e);
        }
    }
}
