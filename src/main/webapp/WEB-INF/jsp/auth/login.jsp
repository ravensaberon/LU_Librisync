<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>LU Librisync Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="auth-shell">
    <div class="auth-card hero-card">
        <section class="auth-story">
            <span class="tag-chip warn">Library Access Portal</span>
            <h1 class="mt-3 mb-3 fw-bold">LU Librisync</h1>
            <p class="fs-5">Official access point for the library management system.</p>
            <div class="mt-4">
                <p class="mb-2">Use your assigned account to access:</p>
                <ul class="mb-0">
                    <li>Book circulation and return records</li>
                    <li>Library catalog and availability tracking</li>
                    <li>Student and administrator services</li>
                </ul>
            </div>
            <div class="mt-4">
                <p class="mb-1">For account concerns or access issues, please contact the library administrator.</p>
                <p class="mb-0">Authorized users only.</p>
            </div>
        </section>

        <section class="auth-form-wrap">
            <h2 class="fw-bold mb-2">Sign in</h2>
            <p class="muted-text mb-4">Enter your registered credentials to continue.</p>

            <c:if test="${not empty param.error}">
                <div class="alert alert-danger">Invalid email or password.</div>
            </c:if>
            <c:if test="${not empty param.logout}">
                <div class="alert alert-success">You have been logged out.</div>
            </c:if>
            <c:if test="${not empty param.registered}">
                <div class="alert alert-success">Registration complete. You can now sign in.</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/login">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                <div class="mb-3">
                    <label class="form-label" for="email">Email</label>
                    <input class="form-control form-control-lg" id="email" name="email" type="email" required>
                </div>

                <div class="mb-4">
                    <label class="form-label" for="password">Password</label>
                    <input class="form-control form-control-lg" id="password" name="password" type="password" required>
                </div>

                <button class="btn btn-brand w-100 mb-3" type="submit">Login</button>
            </form>

            <p class="mb-0">No student account yet? <a href="${pageContext.request.contextPath}/register">Register here</a>.</p>
        </section>
    </div>
</div>
</body>
</html>
