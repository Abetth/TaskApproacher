package com.taskapproacher.config;

import com.taskapproacher.hibernate.HibernateSessionFactoryUtil;

import org.hibernate.SessionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.taskapproacher")
public class HibernateConfig {

    @Bean
    public SessionFactory sessionFactory() {
        return HibernateSessionFactoryUtil.getSessionFactory();
    }
}
