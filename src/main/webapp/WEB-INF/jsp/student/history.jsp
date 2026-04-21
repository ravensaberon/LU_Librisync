<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Borrowing History</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Reading History</span>
            <div class="brand-title mt-2">Borrowing and return timeline</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/dashboard">Dashboard</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/catalog">Catalog</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/reservations">Reservations</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/profile">Profile</a>
            <a class="nav-pill active" href="${pageContext.request.contextPath}/student/history">Borrowing history</a>
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

    <section class="panel-card">
        <div class="section-title">Reading history overview</div>
        <div class="info-grid mb-4">
            <div class="info-tile">
                <span class="info-tile-label">Borrowed items</span>
                <span class="info-tile-value">${activeCount}</span>
            </div>
            <div class="info-tile">
                <span class="info-tile-label">Overdue items</span>
                <span class="info-tile-value">${overdueCount}</span>
            </div>
            <div class="info-tile">
                <span class="info-tile-label">Active reservations</span>
                <span class="info-tile-value">${reservationCount}</span>
            </div>
            <div class="info-tile">
                <span class="info-tile-label">Outstanding fines</span>
                <span class="info-tile-value">${outstandingFineTotal}</span>
            </div>
        </div>
        <div class="support-item mb-4">
            <strong>${borrowerStanding.statusLabel}</strong>
            <span>Active loans: ${borrowerStanding.activeLoansCount}/${borrowerStanding.maxActiveLoans} | Remaining slots: ${borrowerStanding.remainingLoanSlots}</span>
        </div>
        <div class="section-title">All issue records</div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Book</th>
                    <th>Issue date</th>
                    <th>Due date</th>
                    <th>Return date</th>
                    <th>Status</th>
                    <th>Fine</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${issueRecords}" var="issue">
                    <tr>
                        <td>${issue.book.title}</td>
                        <td>${issue.issueDate}</td>
                        <td>${issue.dueDate}</td>
                        <td>${issue.returnDate}</td>
                        <td><span class="tag-chip">${issue.status}</span></td>
                        <td>${issue.fineAmount}</td>
                        <td>
                            <c:choose>
                                <c:when test="${issue.returned}">
                                    <span class="muted-text">Completed</span>
                                </c:when>
                                <c:otherwise>
                                    <form method="post" action="${pageContext.request.contextPath}/student/issues/${issue.id}/return">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input type="hidden" name="redirectTo" value="/student/history">
                                        <button class="btn btn-warm" type="submit">Return book</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty issueRecords}">
                    <tr>
                        <td colspan="7" class="text-center muted-text">No history available yet.</td>
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
