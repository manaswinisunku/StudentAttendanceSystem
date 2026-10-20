CREATE DATABASE IF NOT EXISTS student_attendance
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_attendance;

-- Hibernate creates/updates the students and attendance tables
-- on startup because hibernate.hbm2ddl.auto=update.
