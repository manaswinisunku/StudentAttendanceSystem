package com.attendance.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.Attendance;
import com.attendance.model.AttendanceStatus;
import com.attendance.model.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "AttendanceServlet", urlPatterns = "/attendance")
public class AttendanceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        LocalDate date;
        try {
            String dateText = request.getParameter("date");
            date = (dateText == null || dateText.isBlank())
                    ? LocalDate.now() : LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date. Use YYYY-MM-DD.");
            return;
        }

        try {
            List<Student> students = studentDAO.findAll();
            List<Attendance> records = attendanceDAO.findByDate(date);
            String context = escape(request.getContextPath());
            String message = escape(request.getParameter("message"));
            PrintWriter out = response.getWriter();

            out.printf("""
                    <!DOCTYPE html><html lang="en"><head><meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Attendance Management</title>
                    <style>
                    body{font-family:Arial,sans-serif;max-width:1100px;margin:35px auto;padding:0 18px;color:#1f2937}
                    h1{color:#123b64}form{background:#f3f6f9;padding:18px;border-radius:10px;max-width:700px;margin-bottom:20px}
                    label{display:inline-block;min-width:145px;margin:8px 0}select,input{padding:8px;min-width:190px;max-width:95%%;box-sizing:border-box}
                    button{padding:10px 16px;background:#155e9a;color:white;border:0;border-radius:5px;cursor:pointer}
                    table{border-collapse:collapse;width:100%%;margin-top:18px}th,td{padding:10px;border:1px solid #d1d5db;text-align:left}th{background:#e8eef5}
                    a{color:#155e9a}.message{color:#176b36}
                    </style></head><body>
                    <h1>Attendance Management</h1>
                    <p><a href="%s/">Home</a> | <a href="%s/students">Student Management</a></p>
                    <p class="message">%s</p>
                    <h2>Mark Attendance</h2>
                    """, context, context, message);

            if (students.isEmpty()) {
                out.printf("<p>No students are registered yet. <a href=\"%s/students\">Add a student first</a>.</p>%n", context);
            } else {
                out.printf("<form action=\"%s/attendance\" method=\"post\">", context);
                out.println("<label for=\"studentId\">Student *</label><select id=\"studentId\" name=\"studentId\" required>");
                for (Student student : students) {
                    out.printf("<option value=\"%s\">%s - %s</option>%n",
                            escape(String.valueOf(student.getId())),
                            escape(student.getRollNumber()), escape(student.getName()));
                }
                out.println("</select><br>");
                out.printf("<label for=\"attendanceDate\">Attendance Date *</label><input id=\"attendanceDate\" type=\"date\" name=\"attendanceDate\" value=\"%s\" required><br>", date);
                out.println("<label for=\"status\">Status *</label><select id=\"status\" name=\"status\" required>");
                out.println("<option value=\"PRESENT\">Present</option><option value=\"ABSENT\">Absent</option><option value=\"LATE\">Late</option><option value=\"LEAVE\">Leave</option>");
                out.println("</select><br><br><button type=\"submit\">Save Attendance</button></form>");
            }

            out.printf("""
                    <h2>Attendance Records</h2>
                    <form action="%s/attendance" method="get">
                    <label for="viewDate">View Date</label><input id="viewDate" type="date" name="date" value="%s" required>
                    <button type="submit">View Records</button></form>
                    <table><thead><tr><th>Student ID</th><th>Roll Number</th><th>Name</th><th>Date</th><th>Status</th></tr></thead><tbody>
                    """, context, date);

            if (records.isEmpty()) {
                out.println("<tr><td colspan=\"5\">No attendance records for this date.</td></tr>");
            } else {
                for (Attendance record : records) {
                    Student student = record.getStudent();
                    out.printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>%n",
                            escape(String.valueOf(student.getId())),
                            escape(student.getRollNumber()), escape(student.getName()),
                            escape(String.valueOf(record.getAttendanceDate())),
                            escape(String.valueOf(record.getStatus())));
                }
            }
            out.println("</tbody></table></body></html>");
        } catch (RuntimeException exception) {
            log("Could not load attendance. Check the MySQL configuration and server logs.", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load attendance. Check hibernate.properties and the server Console.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Long studentId;
        LocalDate date;
        AttendanceStatus status;
        try {
            studentId = Long.valueOf(request.getParameter("studentId"));
            date = LocalDate.parse(request.getParameter("attendanceDate"));
            status = AttendanceStatus.valueOf(request.getParameter("status").toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid student ID, attendance date, or status.");
            return;
        }

        try {
            Student student = studentDAO.findById(studentId);
            if (student == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Student not found.");
                return;
            }

            Attendance existing = attendanceDAO.findByStudentAndDate(studentId, date);
            if (existing != null) {
                response.sendRedirect(request.getContextPath() + "/attendance?date=" + date
                        + "&message=Attendance%20already%20exists%20for%20this%20student%20and%20date");
                return;
            }

            attendanceDAO.save(new Attendance(student, date, status));
            response.sendRedirect(request.getContextPath() + "/attendance?date=" + date
                    + "&message=Attendance%20saved%20successfully");
        } catch (RuntimeException exception) {
            log("Unable to save attendance. A student can have only one record per date.", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to save attendance. Check database settings and whether a record already exists for the selected student and date.");
        }
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
