<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Student Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Student Portal</span>
            <div class="brand-title mt-2">Welcome, ${student.user.name}</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill active" href="${pageContext.request.contextPath}/student/dashboard">Dashboard</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/catalog">Catalog</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/reservations">Reservations</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/profile">Profile</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/history">Borrowing history</a>
            <form method="post" action="${pageContext.request.contextPath}/logout">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button class="nav-pill warm border-0" type="submit">Logout</button>
            </form>
        </div>
    </div>

    <section class="hero-card mb-4">
        <div class="row g-4 align-items-center">
            <div class="col-md-8">
                <h1 class="fw-bold mb-2">Library access at a glance</h1>
                <p class="muted-text mb-0">Student ID: <strong>${student.studentId}</strong> | Course: <strong>${student.course}</strong> | Year level: <strong>${student.yearLevel}</strong></p>
            </div>
            <div class="col-md-4 text-md-end">
                <span class="tag-chip">Digital-ready account</span>
            </div>
        </div>
    </section>

    <section class="stat-grid mb-4">
        <div class="metric-card">
            <div class="metric-value">${activeCount}</div>
            <div class="metric-label">Currently borrowed</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${overdueCount}</div>
            <div class="metric-label">Overdue items</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${historyCount}</div>
            <div class="metric-label">Total issue history</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${reservationCount}</div>
            <div class="metric-label">Active reservations</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${outstandingFineTotal}</div>
            <div class="metric-label">Outstanding fines</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${borrowerStanding.remainingLoanSlots}</div>
            <div class="metric-label">Remaining loan slots</div>
        </div>
    </section>

    <section class="panel-grid panel-grid-equal mb-4">
        <div class="panel-card">
            <div class="section-title">Borrowing standing</div>
            <div class="support-item">
                <strong>${borrowerStanding.statusLabel}</strong>
                <span>
                    Active loans: ${borrowerStanding.activeLoansCount}/${borrowerStanding.maxActiveLoans}
                    | Overdue items: ${borrowerStanding.overdueCount}
                    | Outstanding fines: ${borrowerStanding.outstandingFineAmount}
                </span>
            </div>
            <c:if test="${borrowerStanding.blocked}">
                <div class="support-list mt-3">
                    <c:forEach items="${borrowerStanding.blockers}" var="blocker">
                        <div class="support-item">
                            <strong>Action needed</strong>
                            <span>${blocker}</span>
                        </div>
                    </c:forEach>
                </div>
            </c:if>
        </div>

        <div class="panel-card">
            <div class="section-title">Recent fine activity</div>
            <ul class="list-clean">
                <c:forEach items="${studentFines}" var="fine" end="4">
                    <li class="d-flex justify-content-between align-items-center">
                        <span>${fine.issueRecord.book.title}</span>
                        <span class="tag-chip">${fine.amount} | ${fine.status}</span>
                    </li>
                </c:forEach>
                <c:if test="${empty studentFines}">
                    <li class="muted-text">No fines are currently recorded for your account.</li>
                </c:if>
            </ul>
        </div>
    </section>

    <section class="panel-card">
        <div class="section-title">Issued books and return schedule</div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Book</th>
                    <th>Issue date</th>
                    <th>Due date</th>
                    <th>Status</th>
                    <th>Fine</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${issueRecords}" var="issue">
                    <tr>
                        <td>${issue.book.title}</td>
                        <td>${issue.issueDate}</td>
                        <td>${issue.dueDate}</td>
                        <td><span class="tag-chip">${issue.status}</span></td>
                        <td>${issue.fineAmount}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty issueRecords}">
                    <tr>
                        <td colspan="5" class="text-center muted-text">No borrowed books yet.</td>
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
