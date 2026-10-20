package com.attendance.dao;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.attendance.model.Attendance;
import com.attendance.util.HibernateUtil;

public class AttendanceDAO {

    public Attendance save(Attendance attendance) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(attendance);
            transaction.commit();
            return attendance;
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public List<Attendance> findByStudentId(Long studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select a from Attendance a join fetch a.student "
                    + "where a.student.id = :studentId "
                    + "order by a.attendanceDate desc",
                    Attendance.class)
                    .setParameter("studentId", studentId)
                    .getResultList();
        }
    }

    public List<Attendance> findByDate(LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select a from Attendance a join fetch a.student "
                    + "where a.attendanceDate = :date "
                    + "order by a.student.name, a.student.rollNumber",
                    Attendance.class)
                    .setParameter("date", date)
                    .getResultList();
        }
    }

    public Attendance findByStudentAndDate(Long studentId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Attendance> results = session.createQuery(
                    "select a from Attendance a join fetch a.student "
                    + "where a.student.id = :studentId "
                    + "and a.attendanceDate = :date",
                    Attendance.class)
                    .setParameter("studentId", studentId)
                    .setParameter("date", date)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
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
