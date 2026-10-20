<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Student Attendance Management System</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 900px; margin: 60px auto; padding: 0 20px; color: #1f2937; }
        h1 { color: #123b64; }
        .links { display: flex; flex-wrap: wrap; gap: 16px; margin-top: 28px; }
        .card { display: inline-block; min-width: 220px; padding: 22px; border-radius: 12px; background: #f1f5f9; text-decoration: none; color: #155e9a; font-weight: bold; }
        .card:hover { background: #e2e8f0; }
    </style>
</head>
<body>
    <h1>Student Attendance Management System</h1>
    <p>Welcome. Select a module to continue.</p>
    <div class="links">
        <a class="card" href="${pageContext.request.contextPath}/students">Student Management</a>
        <a class="card" href="${pageContext.request.contextPath}/attendance">Attendance Management</a>
    </div>
</body>
</html>
