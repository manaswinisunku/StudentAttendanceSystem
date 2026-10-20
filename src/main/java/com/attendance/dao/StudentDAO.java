package com.attendance.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.attendance.model.Student;
import com.attendance.util.HibernateUtil;

public class StudentDAO {

    public Student save(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(student);
            transaction.commit();
            return student;
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public Student findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Student.class, id);
        }
    }

    public List<Student> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Student s order by s.name, s.rollNumber", Student.class)
                    .getResultList();
        }
    }

    public Student update(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Student merged = session.merge(student);
            transaction.commit();
            return merged;
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    private static void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            try {
                transaction.rollback();
            } catch (RuntimeException rollbackException) {
                System.err.println("Could not roll back transaction: "
                        + rollbackException.getMessage());
            }
        }
    }
}
