# Student Attendance Management System

A Maven WAR web application using Java 17, Tomcat 10.1, Hibernate ORM 6.6, Jakarta Servlet 6, and MySQL.

## Included features

- Student registration and student listing
- Attendance entry with status: PRESENT, ABSENT, LATE, or LEAVE
- Attendance records filtered by date
- One attendance record per student per date (unique database constraint)
- Hibernate entity mapping and DAO layer
- JSP homepage with links to both modules
- No XML-based Hibernate configuration; Hibernate settings are in `src/main/resources/hibernate.properties`

## Requirements

- JDK 17 or later
- Eclipse IDE for Enterprise Java and Web Developers (or another Maven-capable IDE)
- Apache Tomcat 10.1
- MySQL Server 8.x or compatible

## Setup

1. Open MySQL Workbench and run `database/create_database.sql`, or run:

   ```sql
   CREATE DATABASE student_attendance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Open `src/main/resources/hibernate.properties` and set the correct MySQL username and password. The default username is `root` and the default password is blank. Do not leave the default if your MySQL account uses a password.
3. In Eclipse, choose **File → Import → Maven → Existing Maven Projects**. Select the extracted `StudentAttendanceSystem` folder and finish the import.
4. Right-click the project → **Maven → Update Project…** and select the project.
5. Right-click the project → **Properties → Web Project Settings** and set **Context root** to `StudentAttendanceSystem` (if Eclipse shows a different value).
6. Add the project to a Tomcat 10.1 server: in the **Servers** tab, right-click Tomcat → **Add and Remove…** → add this project.
7. Start Tomcat. Hibernate creates or updates the `students` and `attendance` tables after the first request that initializes Hibernate.

## URLs

With context root `StudentAttendanceSystem`:

- Homepage: `http://localhost:8080/StudentAttendanceSystem/`
- Student management: `http://localhost:8080/StudentAttendanceSystem/students`
- Attendance management: `http://localhost:8080/StudentAttendanceSystem/attendance`

If you changed the context root in Eclipse, replace `StudentAttendanceSystem` in the URLs with the configured value.

## Database configuration alternatives

Instead of editing the properties file, you can override connection settings with environment variables before starting Tomcat:

- `DB_URL` (optional; default URL is in `hibernate.properties`)
- `DB_USER`
- `DB_PASSWORD`

The app expects the database itself (`student_attendance`) to exist first. Hibernate `update` creates/updates entity tables but does not create the database. For a production deployment, use a managed schema migration and do not rely on `hbm2ddl.auto=update`.

## Troubleshooting

- **404 for every page:** Confirm that the project is added to Tomcat and that the browser URL matches the Eclipse Web Project Settings → Context root. Try `/index.jsp` directly.
- **404 only for `/students` or `/attendance`:** Check the Eclipse Console for deployment errors and confirm that the servlet source files are under `src/main/java/com/attendance/servlet`.
- **HTTP 500 / database errors:** Confirm MySQL is running, the database exists, and the credentials in `hibernate.properties` are correct. Read the full exception in the Eclipse Console.
- **Maven dependency errors:** Right-click the project → Maven → Update Project, tick **Force Update of Snapshots/Releases** if needed, then wait for the dependencies to download.
- **Java version errors:** Ensure Eclipse uses JDK 17 or newer and that the project compiler compliance level is 17.

This project is a starter implementation for local academic use. Add authentication, authorization, CSRF protection, and production-grade error handling before exposing it to real users or networks.
