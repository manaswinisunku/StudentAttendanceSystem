package com.attendance.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.attendance.model.Attendance;
import com.attendance.model.Student;

public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        Properties properties = new Properties();

        try (InputStream input = HibernateUtil.class.getClassLoader()
                .getResourceAsStream("hibernate.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                        "hibernate.properties was not found in src/main/resources.");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }

        // Optional environment/system-property overrides are useful on servers.
        override(properties, "hibernate.connection.url", "DB_URL");
        override(properties, "hibernate.connection.username", "DB_USER");
        override(properties, "hibernate.connection.password", "DB_PASSWORD");

        try {
            return new Configuration()
                    .setProperties(properties)
                    .addAnnotatedClass(Student.class)
                    .addAnnotatedClass(Attendance.class)
                    .buildSessionFactory();
        } catch (Throwable exception) {
            System.err.println("Hibernate SessionFactory initialization failed. "
                    + "Check hibernate.properties and the MySQL server.");
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static void override(Properties properties, String propertyName,
                                 String environmentName) {
        String value = System.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentName);
        }
        if (value != null && !value.isBlank()) {
            properties.setProperty(propertyName, value);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
        }
    }
}
