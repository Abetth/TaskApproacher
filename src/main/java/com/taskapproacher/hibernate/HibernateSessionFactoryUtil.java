package com.taskapproacher.hibernate;

import com.taskapproacher.entities.Task;
import com.taskapproacher.entities.TaskBoard;
import org.hibernate.HibernateError;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactoryUtil {
    private static SessionFactory sessionFactory;

    private HibernateSessionFactoryUtil() {}

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration().configure();
                configuration.addAnnotatedClass(Task.class);
                configuration.addAnnotatedClass(TaskBoard.class);
                StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());
            } catch (HibernateError e) {
                throw new RuntimeException("Failed to initialize SessionFactory", e);
            }
        }
        return sessionFactory;
    }
}
