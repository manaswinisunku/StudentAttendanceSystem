package com.attendance.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.attendance.dao.StudentDAO;
import com.attendance.model.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "StudentServlet", urlPatterns = "/students")
public class StudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            List<Student> students = studentDAO.findAll();
            String context = escape(request.getContextPath());
            String message = escape(request.getParameter("message"));
            PrintWriter out = response.getWriter();

            out.printf("""
                    <!DOCTYPE html>
                    <html lang="en"><head><meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Student Management</title>
                    <style>
                    body{font-family:Arial,sans-serif;max-width:1100px;margin:35px auto;padding:0 18px;color:#1f2937}
                    h1{color:#123b64}form{background:#f3f6f9;padding:18px;border-radius:10px;max-width:650px}
                    label{display:inline-block;min-width:145px;margin:8px 0}input{padding:8px;max-width:95%%;box-sizing:border-box}
                    button{padding:10px 16px;background:#155e9a;color:white;border:0;border-radius:5px;cursor:pointer}
                    table{border-collapse:collapse;width:100%%;margin-top:18px}th,td{padding:10px;border:1px solid #d1d5db;text-align:left}
                    th{background:#e8eef5}a{color:#155e9a}.message{color:#176b36}
                    </style></head><body>
                    <h1>Student Attendance Management System</h1>
                    <p><a href="%s/">Home</a> | <a href="%s/attendance">Attendance Management</a></p>
                    <p class="message">%s</p>
                    <h2>Add Student</h2>
                    <form action="%s/students" method="post">
                    <label for="rollNumber">Roll Number *</label><input id="rollNumber" name="rollNumber" maxlength="50" required><br>
                    <label for="name">Full Name *</label><input id="name" name="name" maxlength="100" required><br>
                    <label for="email">Email *</label><input id="email" name="email" type="email" maxlength="150" required><br>
                    <label for="department">Department</label><input id="department" name="department" maxlength="100"><br>
                    <label for="yearOfStudy">Year of Study</label><input id="yearOfStudy" name="yearOfStudy" type="number" min="1" max="10"><br><br>
                    <button type="submit">Add Student</button></form>
                    <h2>Registered Students (%d)</h2>
                    <table><thead><tr><th>ID</th><th>Roll Number</th><th>Name</th><th>Email</th><th>Department</th><th>Year</th></tr></thead><tbody>
                    """, context, context, message, context, students.size());

            if (students.isEmpty()) {
                out.println("<tr><td colspan=\"6\">No students yet. Use the form above to add one.</td></tr>");
            } else {
                for (Student student : students) {
                    out.printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>%n",
                            escape(String.valueOf(student.getId())),
                            escape(student.getRollNumber()),
                            escape(student.getName()),
                            escape(student.getEmail()),
                            escape(student.getDepartment()),
                            student.getYearOfStudy() == null ? "" : escape(student.getYearOfStudy().toString()));
                }
            }
            out.println("</tbody></table></body></html>");
        } catch (RuntimeException exception) {
            log("Could not load students. Check the MySQL configuration and server logs.", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load students. Check hibernate.properties and the server Console.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String rollNumber = trim(request.getParameter("rollNumber"));
        String name = trim(request.getParameter("name"));
        String email = trim(request.getParameter("email"));
        String department = trim(request.getParameter("department"));
        String yearText = trim(request.getParameter("yearOfStudy"));

        if (rollNumber.isEmpty() || name.isEmpty() || email.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Roll number, name, and email are required.");
            return;
        }

        Integer year = null;
        try {
            if (!yearText.isEmpty()) {
                year = Integer.valueOf(yearText);
                if (year < 1 || year > 10) {
                    throw new NumberFormatException("Year out of range");
                }
            }
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Year of study must be between 1 and 10.");
            return;
        }

        try {
            Student student = new Student(rollNumber, name, email,
                    department.isEmpty() ? null : department, year);
            studentDAO.save(student);
            response.sendRedirect(request.getContextPath()
                    + "/students?message=Student%20added%20successfully");
        } catch (RuntimeException exception) {
            log("Unable to save student. Roll number and email must be unique.", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to save student. Check the database connection and make sure the roll number and email are unique.");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
