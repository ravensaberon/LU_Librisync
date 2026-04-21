<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reservation Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Reservation Queue</span>
            <div class="brand-title mt-2">Manage reservations</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/books">Books</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/issues">Issue / Return</a>
            <a class="nav-pill active" href="${pageContext.request.contextPath}/admin/reservations">Reservations</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/students">Students</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/fines">Fines</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/reports">Reports</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/references">Categories / Authors</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/profile">Profile</a>
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
        <h1 class="fw-bold mb-2">Reservation queue control</h1>
        <p class="muted-text mb-0">Watch pending requests, release ready reservations fairly, and issue books directly from the claim queue.</p>
    </section>

    <section class="stat-grid mb-4">
        <div class="metric-card">
            <div class="metric-value">${reservationCount}</div>
            <div class="metric-label">Reservation records</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${pendingReservationCount}</div>
            <div class="metric-label">Pending queue requests</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${readyReservationCount}</div>
            <div class="metric-label">Ready to claim</div>
        </div>
    </section>

    <section class="panel-card">
        <div class="section-title">All reservation records</div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Book</th>
                    <th>Student</th>
                    <th>Queue</th>
                    <th>Status</th>
                    <th>Reserved at</th>
                    <th>Claim until</th>
                    <th>Claim and issue</th>
                    <th>Cancel</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${reservations}" var="reservation">
                    <tr>
                        <td>${reservation.book.title}</td>
                        <td>${reservation.student.studentId} - ${reservation.student.user.name}</td>
                        <td>${reservation.queuePosition}</td>
                        <td><span class="tag-chip">${reservation.status}</span></td>
                        <td>${reservation.reservedAt}</td>
                        <td>${reservation.expiresAt}</td>
                        <td>
                            <c:choose>
                                <c:when test="${reservation.status.name() == 'READY'}">
                                    <form method="post" action="${pageContext.request.contextPath}/admin/reservations/${reservation.id}/claim" class="d-grid gap-2">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input class="form-control form-control-sm" name="dueDate" type="date" value="${defaultDueDate}" required>
                                        <input class="form-control form-control-sm" name="remarks" placeholder="Optional remarks">
                                        <button class="btn btn-brand" type="submit"><i class="bi bi-box-arrow-right me-2"></i>Issue now</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <span class="muted-text">Issue action unlocks when status becomes READY.</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:if test="${reservation.active}">
                                <form method="post" action="${pageContext.request.contextPath}/admin/reservations/${reservation.id}/cancel">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button class="btn btn-warm" type="submit">Cancel</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty reservations}">
                    <tr>
                        <td colspan="8" class="text-center muted-text">No reservation records available yet.</td>
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
