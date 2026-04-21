<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Reservations</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Reservation Queue</span>
            <div class="brand-title mt-2">My reservations</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/dashboard">Dashboard</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/catalog">Catalog</a>
            <a class="nav-pill active" href="${pageContext.request.contextPath}/student/reservations">Reservations</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/profile">Profile</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/history">Borrowing history</a>
            <form method="post" action="${pageContext.request.contextPath}/logout">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button class="nav-pill warm border-0" type="submit">Logout</button>
            </form>
        </div>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <section class="hero-card mb-4">
        <h1 class="fw-bold mb-2">Reservation queue status</h1>
        <p class="muted-text mb-0">Track your pending holds and borrow ready reservations as soon as a copy is released to you.</p>
    </section>

    <section class="panel-card">
        <div class="section-title">Reserved books</div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Book</th>
                    <th>Queue position</th>
                    <th>Status</th>
                    <th>Reserved at</th>
                    <th>Claim until</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${reservations}" var="reservation">
                    <tr>
                        <td>${reservation.book.title}</td>
                        <td>${reservation.queuePosition}</td>
                        <td><span class="tag-chip">${reservation.status}</span></td>
                        <td>${reservation.reservedAt}</td>
                        <td>${reservation.expiresAt}</td>
                        <td>
                            <div class="d-flex flex-wrap gap-2">
                                <c:if test="${reservation.status.name() == 'READY'}">
                                    <form method="post" action="${pageContext.request.contextPath}/student/reservations/${reservation.id}/claim">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button class="btn btn-brand" type="submit">Borrow now</button>
                                    </form>
                                    <span class="muted-text align-self-center">Due date: ${defaultBorrowDueDate}</span>
                                </c:if>
                                <c:if test="${reservation.active}">
                                    <form method="post" action="${pageContext.request.contextPath}/student/reservations/${reservation.id}/cancel">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button class="btn btn-warm" type="submit">Cancel</button>
                                    </form>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty reservations}">
                    <tr>
                        <td colspan="6" class="text-center muted-text">You do not have any reservations yet.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
