<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Issue and Return</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Circulation Desk</span>
            <div class="brand-title mt-2">Issue and Return Books</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/books">Books</a>
            <a class="nav-pill active" href="${pageContext.request.contextPath}/admin/issues">Issue / Return</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/students">Students</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/fines">Fines</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/reports">Reports</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/references">Categories / Authors</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/profile">Profile</a>
            <form method="post" action="${pageContext.request.contextPath}/logout">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button class="nav-pill warm border-0" type="submit" aria-label="Logout" title="Logout"><span class="nav-pill-icon"><i class="bi bi-power" aria-hidden="true"></i></span><span class="nav-pill-label">Logout</span></button>
            </form>
        </div>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <section class="stat-grid mb-4">
        <div class="metric-card">
            <div class="metric-value">${activeIssueCount}</div>
            <div class="metric-label">Active Desk Records</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${overdueIssueCount}</div>
            <div class="metric-label">Overdue Active Records</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${historyCount}</div>
            <div class="metric-label">Total Circulation History</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${pendingReturnRequestCount}</div>
            <div class="metric-label">Pending Return Confirmations</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${maxLoanDays}</div>
            <div class="metric-label">Max Loan Days</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${maxActiveLoans}</div>
            <div class="metric-label">Max Active Loans Per Borrower</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${reservationCount}</div>
            <div class="metric-label">Reservation Desk Records</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${readyReservationCount}</div>
            <div class="metric-label">Reservations Ready To Issue</div>
        </div>
    </section>

    <section class="hero-card mb-4">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
            <div>
                <span class="tag-chip">Circulation Workspace</span>
                <h1 class="fw-bold mt-3 mb-2">Issue, Return, And Release Books</h1>
                <p class="muted-text mb-0">Manage direct issue transactions, reservation pickup releases, and circulation history from one admin desk view.</p>
            </div>
            <button class="btn btn-brand" type="button" data-bs-toggle="modal" data-bs-target="#issueBookFormModal">
                <i class="bi bi-journal-plus me-2"></i>Issue A New Book
            </button>
        </div>
    </section>

    <c:if test="${not empty editIssue}">
        <section class="panel-card mb-4">
            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                <div>
                    <div class="section-title mb-2">Edit Issue Record</div>
                    <p class="helper-copy">
                        Adjust due dates or internal remarks for an existing circulation record without deleting the transaction history.
                    </p>
                </div>
                <a class="action-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.page}">Cancel editing</a>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/admin/issues/${editIssue.id}/update" class="row g-3">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="activePage" value="${activeIssuesPage.page}">
                <input type="hidden" name="historyPage" value="${issueHistoryPage.page}">
                <div class="col-md-6">
                    <label class="form-label">Book</label>
                    <input class="form-control" value="${editIssue.book.title}" readonly>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Student</label>
                    <input class="form-control" value="${editIssue.student.studentId} - ${editIssue.student.user.name}" readonly>
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="editDueDate">Due date</label>
                    <input class="form-control" id="editDueDate" name="dueDate" type="date" value="${editIssueDueDate}" required>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Status</label>
                    <input class="form-control" value="${editIssue.status}" readonly>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Issue code</label>
                    <input class="form-control" value="${editIssue.qrIssueCode}" readonly>
                </div>
                <div class="col-12">
                    <label class="form-label" for="editRemarks">Remarks</label>
                    <textarea class="form-control" id="editRemarks" name="remarks" rows="3">${editIssue.remarks}</textarea>
                </div>
                <div class="col-12 d-flex flex-wrap gap-2">
                    <button class="btn btn-brand" type="submit"><i class="bi bi-save2 me-2"></i>Update issue record</button>
                    <a class="btn btn-warm" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.page}">Back To Circulation Desk</a>
                </div>
            </form>
        </section>
    </c:if>

    <section class="dashboard-tab-shell mb-4" data-issue-desk-tabs>
        <div class="dashboard-tab-nav" role="tablist" aria-label="Issue and return sections">
            <button class="dashboard-tab-button is-active" type="button" role="tab" aria-selected="true" aria-controls="issue-active-panel" data-issue-desk-tab-button data-issue-desk-tab-target="issue-active-panel">
                <i class="bi bi-arrow-left-right"></i>
                <span>Active Issue Records</span>
            </button>
            <button class="dashboard-tab-button" type="button" role="tab" aria-selected="false" aria-controls="issue-reservation-panel" data-issue-desk-tab-button data-issue-desk-tab-target="issue-reservation-panel">
                <i class="bi bi-bookmark-check"></i>
                <span>Reservation Desk Release</span>
            </button>
            <button class="dashboard-tab-button" type="button" role="tab" aria-selected="false" aria-controls="issue-history-panel" data-issue-desk-tab-button data-issue-desk-tab-target="issue-history-panel">
                <i class="bi bi-clock-history"></i>
                <span>Circulation History</span>
            </button>
        </div>
    </section>

    <section class="panel-card mb-4 dashboard-tab-panel is-active" id="issue-active-panel" role="tabpanel" data-issue-desk-tab-panel>
        <div class="section-title">Active Issue Records</div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Issue code</th>
                    <th>Book</th>
                    <th>Student</th>
                    <th>Issued by</th>
                    <th>Issue date</th>
                    <th>Due date</th>
                    <th>Status</th>
                    <th>Fine</th>
                    <th>Remarks</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${activeIssues}" var="issue">
                    <tr>
                        <td>${issue.qrIssueCode}</td>
                        <td>${issue.book.title}</td>
                        <td>${issue.student.studentId} - ${issue.student.user.name}</td>
                        <td>${issue.issuedBy.name}</td>
                        <td>${issue.issueDateDisplay}</td>
                        <td>${issue.dueDateDisplay}</td>
                        <td>
                            <div class="d-flex flex-wrap gap-2">
                                <span class="tag-chip">${issue.status}</span>
                                <c:if test="${issue.returnRequested}">
                                    <span class="tag-chip warn">Return Requested</span>
                                </c:if>
                            </div>
                        </td>
                        <td>${issue.fineAmount}</td>
                        <td class="muted-text">
                            <c:if test="${issue.returnRequested}">
                                Student asked for desk return confirmation.
                                <c:if test="${not empty issue.remarks}"><br></c:if>
                            </c:if>
                            ${issue.remarks}
                        </td>
                        <td class="table-actions">
                            <button class="icon-action" type="button" title="View QR code" data-bs-toggle="modal" data-bs-target="#issueQrModal" data-issue-code="${issue.qrIssueCode}" data-book-title="${issue.book.title}" data-student-name="${issue.student.user.name}">
                                <i class="bi bi-qr-code"></i>
                            </button>
                            <a class="icon-action" href="${pageContext.request.contextPath}/admin/issues?editId=${issue.id}&activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.page}" title="Edit issue record">
                                <i class="bi bi-pencil-square"></i>
                            </a>
                            <form method="post" action="${pageContext.request.contextPath}/admin/issues/${issue.id}/return">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <input type="hidden" name="activePage" value="${activeIssuesPage.page}">
                                <input type="hidden" name="historyPage" value="${issueHistoryPage.page}">
                                <button class="icon-action" type="submit" title="Confirm return at desk">
                                    <i class="bi bi-arrow-return-left"></i>
                                </button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/admin/issues/${issue.id}/delete">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <input type="hidden" name="activePage" value="${activeIssuesPage.page}">
                                <input type="hidden" name="historyPage" value="${issueHistoryPage.page}">
                                <button class="icon-action danger" type="submit" title="Delete issue record">
                                    <i class="bi bi-trash3"></i>
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty activeIssues}">
                    <tr>
                        <td colspan="10" class="text-center muted-text">There are no active issue records right now.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
        <c:if test="${activeIssuesPage.totalPages > 1}">
            <nav class="mt-4" aria-label="Active issue record pages">
                <ul class="pagination justify-content-center mb-0">
                    <li class="page-item <c:if test='${!activeIssuesPage.hasPrevious}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.previousPage}&historyPage=${issueHistoryPage.page}<c:if test='${not empty editIssue}'>&editId=${editIssue.id}</c:if>">Previous</a>
                    </li>
                    <c:forEach begin="${activeIssuesPage.startPage}" end="${activeIssuesPage.endPage}" var="pageNumber">
                        <li class="page-item <c:if test='${pageNumber == activeIssuesPage.page}'>active</c:if>">
                            <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${pageNumber}&historyPage=${issueHistoryPage.page}<c:if test='${not empty editIssue}'>&editId=${editIssue.id}</c:if>">${pageNumber}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item <c:if test='${!activeIssuesPage.hasNext}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.nextPage}&historyPage=${issueHistoryPage.page}<c:if test='${not empty editIssue}'>&editId=${editIssue.id}</c:if>">Next</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </section>

    <section class="panel-card mb-4 dashboard-tab-panel" id="issue-reservation-panel" role="tabpanel" data-issue-desk-tab-panel hidden>
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
            <div>
                <div class="section-title mb-1">Reservation Desk Release</div>
                <p class="helper-copy mb-0">Pending and ready reservations are handled here so circulation, pickup confirmation, and issue recording stay in one workspace.</p>
            </div>
            <button class="btn btn-warm scanner-trigger" type="button" data-bs-toggle="modal" data-bs-target="#reservationQrScannerModal">
                <i class="bi bi-qr-code-scan me-2"></i>Scan Student Pickup QR
            </button>
        </div>

        <%-- ── Borrow Requests ── --%>
        <div class="section-title mb-2">Borrow Requests</div>
        <p class="helper-copy mb-3">Walk-in requests submitted by students. Approve or deny before the book is released.</p>
        <div class="table-responsive mb-2">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Book</th>
                    <th>Student</th>
                    <th>Status</th>
                    <th>Requested at</th>
                    <th>Claim by</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${borrowRequests}" var="reservation">
                    <tr>
                        <td>${reservation.book.title}</td>
                        <td>${reservation.student.studentId} &ndash; ${reservation.student.user.name}</td>
                        <td>
                            <c:choose>
                                <c:when test="${reservation.status.name() == 'PENDING_APPROVAL'}">
                                    <span class="tag-chip warn">Pending approval</span>
                                </c:when>
                                <c:when test="${reservation.status.name() == 'READY'}">
                                    <span class="tag-chip">Ready</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="tag-chip">${reservation.status}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>${reservation.reservedAtDisplay}</td>
                        <td>${reservation.expiresAtDisplay}</td>
                        <td>
                            <c:choose>
                                <c:when test="${reservation.status.name() == 'PENDING_APPROVAL'}">
                                    <div class="d-flex gap-2">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/reservations/${reservation.id}/approve">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <input type="hidden" name="borrowPage" value="${borrowRequestsPage.page}">
                                            <input type="hidden" name="queuePage" value="${queueReservationsPage.page}">
                                            <button class="btn btn-brand btn-sm" type="submit"><i class="bi bi-check-lg me-1"></i>Approve</button>
                                        </form>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/reservations/${reservation.id}/deny">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <input type="hidden" name="borrowPage" value="${borrowRequestsPage.page}">
                                            <input type="hidden" name="queuePage" value="${queueReservationsPage.page}">
                                            <button class="btn btn-warm btn-sm" type="submit"><i class="bi bi-x-lg me-1"></i>Deny</button>
                                        </form>
                                    </div>
                                </c:when>
                                <c:when test="${reservation.status.name() == 'READY'}">
                                    <div class="d-flex align-items-center gap-2 flex-wrap">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/reservations/${reservation.id}/claim" class="d-flex align-items-center gap-2 flex-wrap">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <input type="hidden" name="borrowPage" value="${borrowRequestsPage.page}">
                                            <input type="hidden" name="queuePage" value="${queueReservationsPage.page}">
                                            <input class="form-control form-control-sm" style="width:140px" name="dueDate" type="date" value="${defaultDueDate}" required>
                                            <input class="form-control form-control-sm" style="width:140px" name="remarks" placeholder="Remarks (optional)">
                                            <button class="btn btn-brand btn-sm" type="submit"><i class="bi bi-box-arrow-right me-1"></i>Confirm pickup</button>
                                        </form>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/reservations/${reservation.id}/cancel">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <input type="hidden" name="borrowPage" value="${borrowRequestsPage.page}">
                                            <input type="hidden" name="queuePage" value="${queueReservationsPage.page}">
                                            <button class="btn btn-warm btn-sm" type="submit">Cancel</button>
                                        </form>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <span class="muted-text small">—</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty borrowRequests}">
                    <tr>
                        <td colspan="6" class="text-center muted-text">No active borrow requests.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
        <c:if test="${borrowRequestsPage.totalPages > 1}">
            <nav class="mb-4" aria-label="Admin borrow request pages">
                <ul class="pagination justify-content-center mb-0">
                    <li class="page-item <c:if test='${!borrowRequestsPage.hasPrevious}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.page}&reservationBorrowPage=${borrowRequestsPage.previousPage}&reservationQueuePage=${queueReservationsPage.page}#reservation-desk">Previous</a>
                    </li>
                    <c:forEach begin="${borrowRequestsPage.startPage}" end="${borrowRequestsPage.endPage}" var="pageNumber">
                        <li class="page-item <c:if test='${pageNumber == borrowRequestsPage.page}'>active</c:if>">
                            <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.page}&reservationBorrowPage=${pageNumber}&reservationQueuePage=${queueReservationsPage.page}#reservation-desk">${pageNumber}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item <c:if test='${!borrowRequestsPage.hasNext}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.page}&reservationBorrowPage=${borrowRequestsPage.nextPage}&reservationQueuePage=${queueReservationsPage.page}#reservation-desk">Next</a>
                    </li>
                </ul>
            </nav>
        </c:if>

        <hr class="my-4">

        <%-- ── Queue Reservations ── --%>
        <div class="section-title mb-2">Queue Reservations</div>
        <p class="helper-copy mb-3">Students who reserved a book in advance. Release when their copy is ready.</p>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Book</th>
                    <th>Student</th>
                    <th>Queue</th>
                    <th>Status</th>
                    <th>Reserved at</th>
                    <th>Claim by</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${queueReservations}" var="reservation">
                    <tr>
                        <td>${reservation.book.title}</td>
                        <td>${reservation.student.studentId} &ndash; ${reservation.student.user.name}</td>
                        <td>${reservation.queuePosition}</td>
                        <td><span class="tag-chip">${reservation.status}</span></td>
                        <td>${reservation.reservedAtDisplay}</td>
                        <td>${reservation.expiresAtDisplay}</td>
                        <td>
                            <c:choose>
                                <c:when test="${reservation.status.name() == 'READY'}">
                                    <div class="d-flex align-items-center gap-2 flex-wrap">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/reservations/${reservation.id}/claim" class="d-flex align-items-center gap-2 flex-wrap">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <input type="hidden" name="borrowPage" value="${borrowRequestsPage.page}">
                                            <input type="hidden" name="queuePage" value="${queueReservationsPage.page}">
                                            <input class="form-control form-control-sm" style="width:140px" name="dueDate" type="date" value="${defaultDueDate}" required>
                                            <input class="form-control form-control-sm" style="width:140px" name="remarks" placeholder="Remarks (optional)">
                                            <button class="btn btn-brand btn-sm" type="submit"><i class="bi bi-box-arrow-right me-1"></i>Confirm pickup</button>
                                        </form>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/reservations/${reservation.id}/cancel">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <input type="hidden" name="borrowPage" value="${borrowRequestsPage.page}">
                                            <input type="hidden" name="queuePage" value="${queueReservationsPage.page}">
                                            <button class="btn btn-warm btn-sm" type="submit">Cancel</button>
                                        </form>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="d-flex gap-2 align-items-center">
                                        <span class="muted-text small">Waiting for a copy to become available.</span>
                                        <c:if test="${reservation.active}">
                                            <form method="post" action="${pageContext.request.contextPath}/admin/reservations/${reservation.id}/cancel">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                <input type="hidden" name="borrowPage" value="${borrowRequestsPage.page}">
                                                <input type="hidden" name="queuePage" value="${queueReservationsPage.page}">
                                                <button class="btn btn-warm btn-sm" type="submit">Cancel</button>
                                            </form>
                                        </c:if>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty queueReservations}">
                    <tr>
                        <td colspan="7" class="text-center muted-text">No queue reservations yet.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
        <c:if test="${queueReservationsPage.totalPages > 1}">
            <nav class="mt-4" aria-label="Admin reservation queue pages">
                <ul class="pagination justify-content-center mb-0">
                    <li class="page-item <c:if test='${!queueReservationsPage.hasPrevious}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.page}&reservationBorrowPage=${borrowRequestsPage.page}&reservationQueuePage=${queueReservationsPage.previousPage}#reservation-desk">Previous</a>
                    </li>
                    <c:forEach begin="${queueReservationsPage.startPage}" end="${queueReservationsPage.endPage}" var="pageNumber">
                        <li class="page-item <c:if test='${pageNumber == queueReservationsPage.page}'>active</c:if>">
                            <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.page}&reservationBorrowPage=${borrowRequestsPage.page}&reservationQueuePage=${pageNumber}#reservation-desk">${pageNumber}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item <c:if test='${!queueReservationsPage.hasNext}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.page}&reservationBorrowPage=${borrowRequestsPage.page}&reservationQueuePage=${queueReservationsPage.nextPage}#reservation-desk">Next</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </section>

    <section class="panel-card dashboard-tab-panel" id="issue-history-panel" role="tabpanel" data-issue-desk-tab-panel hidden>
        <div class="section-title">Circulation History</div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Issue code</th>
                    <th>Book</th>
                    <th>Student</th>
                    <th>Issued by</th>
                    <th>Issue date</th>
                    <th>Due date</th>
                    <th>Return date</th>
                    <th>Status</th>
                    <th>Fine</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${issueHistory}" var="issue">
                    <tr>
                        <td>${issue.qrIssueCode}</td>
                        <td>${issue.book.title}</td>
                        <td>${issue.student.studentId} - ${issue.student.user.name}</td>
                        <td>${issue.issuedBy.name}</td>
                        <td>${issue.issueDateDisplay}</td>
                        <td>${issue.dueDateDisplay}</td>
                        <td>${issue.returnDateDisplay}</td>
                        <td><span class="tag-chip">${issue.status}</span></td>
                        <td>${issue.fineAmount}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty issueHistory}">
                    <tr>
                        <td colspan="9" class="text-center muted-text">No circulation history available yet.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
        <c:if test="${issueHistoryPage.totalPages > 1}">
            <nav class="mt-4" aria-label="Circulation history pages">
                <ul class="pagination justify-content-center mb-0">
                    <li class="page-item <c:if test='${!issueHistoryPage.hasPrevious}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.previousPage}<c:if test='${not empty editIssue}'>&editId=${editIssue.id}</c:if>">Previous</a>
                    </li>
                    <c:forEach begin="${issueHistoryPage.startPage}" end="${issueHistoryPage.endPage}" var="pageNumber">
                        <li class="page-item <c:if test='${pageNumber == issueHistoryPage.page}'>active</c:if>">
                            <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${pageNumber}<c:if test='${not empty editIssue}'>&editId=${editIssue.id}</c:if>">${pageNumber}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item <c:if test='${!issueHistoryPage.hasNext}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/issues?activePage=${activeIssuesPage.page}&historyPage=${issueHistoryPage.nextPage}<c:if test='${not empty editIssue}'>&editId=${editIssue.id}</c:if>">Next</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </section>

    <div class="modal fade" id="issueBookFormModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-xl modal-dialog-scrollable">
            <div class="modal-content border-0 shadow-lg">
                <div class="modal-header modal-header-brand">
                    <div>
                        <span class="modal-kicker">Issue Book</span>
                        <h2 class="h4 mb-1 mt-2">Create A New Circulation Record</h2>
                        <p class="modal-subtitle mb-0">Choose the book and borrower, set the due date, and confirm the issue transaction from this modal.</p>
                    </div>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/admin/issues" class="row g-3">
                    <div class="modal-body">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                        <div class="row g-3">
                            <div class="col-md-4">
                                <label class="form-label" for="bookId">Book</label>
                                <select class="form-select" id="bookId" name="bookId" required>
                                    <option value="">Select book</option>
                                    <c:forEach items="${availableBooks}" var="book">
                                        <option value="${book.id}" data-barcode="${book.barcode}" data-isbn="${book.isbn}">${book.title} (${book.availableQuantity} available)</option>
                                    </c:forEach>
                                </select>
                                <div class="d-flex flex-wrap gap-2 mt-2">
                                    <button class="btn btn-warm scanner-trigger" type="button" data-bs-toggle="modal" data-bs-target="#issueBookScannerModal">
                                        <i class="bi bi-upc-scan"></i>Scan book QR
                                    </button>
                                    <span class="form-note">Use the camera to scan the LU Librisync QR label on the book and match it directly to this circulation form.</span>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label" for="studentId">Student</label>
                                <select class="form-select" id="studentId" name="studentId" required>
                                    <option value="">Select student</option>
                                    <c:forEach items="${students}" var="student">
                                        <option value="${student.id}">${student.studentId} - ${student.user.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label" for="dueDate">Due date</label>
                                <input class="form-control" id="dueDate" name="dueDate" type="date" value="${defaultDueDate}" required>
                            </div>
                            <div class="col-12">
                                <label class="form-label" for="remarks">Remarks</label>
                                <input class="form-control" id="remarks" name="remarks" placeholder="Optional note for this issue transaction">
                                <div class="form-note mt-2">Circulation policy currently allows up to ${maxLoanDays} days per issue and ${maxActiveLoans} active loans per borrower.</div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-warm" type="button" data-bs-dismiss="modal">Close</button>
                        <button class="btn btn-brand" type="submit"><i class="bi bi-box-arrow-right me-2"></i>Issue Book</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="modal fade" id="issueBookScannerModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content border-0 shadow-lg">
                <div class="modal-header modal-header-brand">
                    <div>
                        <span class="modal-kicker">Circulation Scan</span>
                        <h2 class="h4 mb-1 mt-2">Match a book copy instantly</h2>
                        <p class="modal-subtitle mb-0">Scan the LU Librisync QR label with your camera or upload a saved QR image from the phone gallery.</p>
                    </div>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="scanner-shell">
                        <video id="issueScannerVideo" autoplay muted playsinline></video>
                        <div class="scanner-overlay"></div>
                        <div class="scanner-target scanner-target-qr"></div>
                    </div>
                    <div class="scanner-status" id="issueScannerStatus">
                        Camera scanner is preparing. Hold the book QR code inside the highlighted frame.
                    </div>
                    <div class="scanner-upload">
                        <div class="scanner-upload-actions">
                            <label class="btn btn-warm mb-0" for="issueScannerUpload">
                                <i class="bi bi-image me-2"></i>Upload QR from gallery
                            </label>
                            <input class="d-none" id="issueScannerUpload" type="file" accept="image/*">
                            <span class="form-note">Choose a downloaded QR image instead of pointing the code at the camera.</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="issueQrModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow-lg">
                <div class="modal-header modal-header-brand">
                    <div>
                        <span class="modal-kicker">QR Issue Code</span>
                        <h2 class="h4 mb-1 mt-2">Scan-ready issue transaction</h2>
                        <p class="modal-subtitle mb-0">Use this QR code for quick verification during release, return, or desk-side circulation lookup.</p>
                    </div>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="qr-card">
                        <div class="qr-code-shell mb-3" id="issueQrCanvas"></div>
                        <div class="qr-code-meta">
                            <div>
                                <span class="info-tile-label">Issue code</span>
                                <span class="qr-code-value" id="issueQrValue">No code selected yet.</span>
                            </div>
                            <div class="row g-2 mt-1">
                                <div class="col-12">
                                    <div class="info-tile">
                                        <span class="info-tile-label">Book</span>
                                        <span class="info-tile-value" id="issueQrBookTitle">Not selected</span>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <div class="info-tile">
                                        <span class="info-tile-label">Borrower</span>
                                        <span class="info-tile-value" id="issueQrStudentName">Not selected</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="d-flex flex-wrap gap-2 mt-3">
                            <button class="btn btn-brand" id="downloadIssueQrButton" type="button" disabled>Download PNG</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="reservationQrScannerModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content border-0 shadow-lg">
                <div class="modal-header modal-header-brand">
                    <div>
                        <span class="modal-kicker">Pickup Scan</span>
                        <h2 class="h4 mb-1 mt-2">Scan A Student Reservation QR</h2>
                        <p class="modal-subtitle mb-0">Set the due date once, then scan the student's QR so the desk release can be recorded automatically.</p>
                    </div>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <form id="reservationQrClaimForm" method="post" action="${pageContext.request.contextPath}/admin/reservations/claim-by-qr" class="row g-3 mb-3">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <input type="hidden" name="borrowPage" value="${borrowRequestsPage.page}">
                        <input type="hidden" name="queuePage" value="${queueReservationsPage.page}">
                        <input type="hidden" name="qrCode" id="reservationQrCodeField">
                        <div class="col-md-4">
                            <label class="form-label" for="reservationScannerDueDate">Due date</label>
                            <input class="form-control" id="reservationScannerDueDate" name="dueDate" type="date" value="${defaultDueDate}" required>
                        </div>
                        <div class="col-md-8">
                            <label class="form-label" for="reservationScannerRemarks">Remarks</label>
                            <input class="form-control" id="reservationScannerRemarks" name="remarks" placeholder="Optional remarks for the issued copy">
                        </div>
                    </form>

                    <%-- Scanner view (shown while scanning) --%>
                    <div id="reservationScannerView">
                        <div class="scanner-shell">
                            <video id="reservationScannerVideo" autoplay muted playsinline></video>
                            <div class="scanner-overlay"></div>
                            <div class="scanner-target scanner-target-qr"></div>
                        </div>
                        <div class="scanner-status" id="reservationScannerStatus">
                            Camera scanner is preparing. Hold the student pickup QR inside the highlighted frame.
                        </div>
                        <div class="scanner-upload">
                            <div class="scanner-upload-actions">
                                <label class="btn btn-warm mb-0" for="reservationScannerUpload">
                                    <i class="bi bi-image me-2"></i>Upload QR from gallery
                                </label>
                                <input class="d-none" id="reservationScannerUpload" type="file" accept="image/*">
                                <span class="form-note">You can also choose a saved screenshot of the student's QR code.</span>
                            </div>
                        </div>
                    </div>

                    <%-- Preview view (shown after scan, before submit) --%>
                    <div id="reservationScannerPreview" hidden>
                        <div class="support-item mb-3">
                            <strong>QR code detected</strong>
                            <span id="reservationPreviewCode" class="d-block mt-1" style="font-family:monospace;font-size:.9rem;word-break:break-all;color:var(--primary-900)"></span>
                        </div>
                        <p class="muted-text mb-3">Review the details above. If correct, click <strong>Confirm &amp; Issue</strong> to complete the desk release. Otherwise, click <strong>Re-scan</strong> to try again.</p>
                        <div class="d-flex gap-2">
                            <button class="btn btn-brand" type="button" id="reservationPreviewConfirmBtn">
                                <i class="bi bi-box-arrow-right me-2"></i>Confirm &amp; Issue
                            </button>
                            <button class="btn btn-warm" type="button" id="reservationPreviewRescanBtn">
                                <i class="bi bi-arrow-repeat me-2"></i>Re-scan
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/vendor/qrious.min.js"></script>
<script src="${pageContext.request.contextPath}/vendor/jsQR.js"></script>
<script src="${pageContext.request.contextPath}/js/qr-tools.js"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        const issueBookFormModalElement = document.getElementById("issueBookFormModal");
        if (issueBookFormModalElement) {
            const shouldOpenIssueBookModal = ${not empty error and empty editIssue ? 'true' : 'false'};
            if (shouldOpenIssueBookModal) {
                bootstrap.Modal.getOrCreateInstance(issueBookFormModalElement).show();
            }
        }

        const deskTabButtons = document.querySelectorAll("[data-issue-desk-tab-button]");
        const deskTabPanels = document.querySelectorAll("[data-issue-desk-tab-panel]");

        function activateDeskTab(targetId) {
            Array.prototype.forEach.call(deskTabButtons, function (button) {
                const isActive = button.getAttribute("data-issue-desk-tab-target") === targetId;
                button.classList.toggle("is-active", isActive);
                button.setAttribute("aria-selected", isActive ? "true" : "false");
                button.setAttribute("tabindex", isActive ? "0" : "-1");
            });

            Array.prototype.forEach.call(deskTabPanels, function (panel) {
                const isActive = panel.id === targetId;
                panel.hidden = !isActive;
                panel.classList.toggle("is-active", isActive);
            });
        }

        Array.prototype.forEach.call(deskTabButtons, function (button) {
            button.addEventListener("click", function () {
                activateDeskTab(button.getAttribute("data-issue-desk-tab-target"));
            });
        });

        if (window.location.hash === "#reservation-desk") {
            activateDeskTab("issue-reservation-panel");
        } else if (window.location.hash === "#circulation-history") {
            activateDeskTab("issue-history-panel");
        } else {
            activateDeskTab("issue-active-panel");
        }

        const scannerModalElement = document.getElementById("issueBookScannerModal");
        const bookSelectElement = document.getElementById("bookId");
        const uploadInput = document.getElementById("issueScannerUpload");
        const scanner = window.LuLibrisyncQr.createScanner({
            videoElement: document.getElementById("issueScannerVideo"),
            statusElement: document.getElementById("issueScannerStatus"),
            formats: ["qr_code"],
            liveMessage: "Scanner is live. Aim the camera at the book QR code and hold it inside the frame.",
            qrFallbackMessage: "QR-only scanning is active on this browser. Aim the camera at a LU Librisync QR book label.",
            unsupportedMessage: "This browser cannot decode live QR codes. You can still choose the book manually.",
            permissionMessage: "Camera access was blocked or unavailable. Please allow camera use, then try again.",
            fileSuccessMessage: "QR image decoded successfully. Matching the book now.",
            onDetected: selectBookFromCode,
            onScanError: function () {
                window.LuLibrisyncQr.setStatus(
                    document.getElementById("issueScannerStatus"),
                    "Camera access is active, but the current frame could not be decoded yet.",
                    true
                );
            }
        });

        function selectBookFromCode(rawValue) {
            const detectedCode = (rawValue || "").trim().toLowerCase();
            if (!detectedCode) {
                return;
            }

            const matchingOption = Array.from(bookSelectElement.options).find(function (option) {
                return (option.dataset.barcode || "").trim().toLowerCase() === detectedCode
                    || (option.dataset.isbn || "").trim().toLowerCase() === detectedCode;
            });

            if (!matchingOption) {
                scanner.setStatus("Scanned code " + rawValue + " was not found in the currently available book list.", true);
                return;
            }

            bookSelectElement.value = matchingOption.value;
            scanner.setStatus("Matched and selected: " + matchingOption.textContent, false);
            const scannerModal = bootstrap.Modal.getInstance(scannerModalElement);
            if (scannerModal) {
                scannerModal.hide();
            }
        }

        scannerModalElement.addEventListener("shown.bs.modal", function () {
            scanner.start();
        });
        scannerModalElement.addEventListener("hidden.bs.modal", function () {
            scanner.stop();
            scanner.setStatus("Camera scanner is preparing. Hold the book QR code inside the highlighted frame.", false);
            uploadInput.value = "";
        });

        uploadInput.addEventListener("change", function (event) {
            const selectedFile = event.target.files && event.target.files[0];
            if (!selectedFile) {
                return;
            }

            scanner.decodeFile(selectedFile);
            uploadInput.value = "";
        });

        const qrModalElement = document.getElementById("issueQrModal");
        const qrCanvasElement = document.getElementById("issueQrCanvas");
        const qrValueElement = document.getElementById("issueQrValue");
        const qrBookTitleElement = document.getElementById("issueQrBookTitle");
        const qrStudentNameElement = document.getElementById("issueQrStudentName");
        const downloadIssueQrButton = document.getElementById("downloadIssueQrButton");
        let currentIssueQrCanvas = null;

        qrModalElement.addEventListener("show.bs.modal", function (event) {
            const trigger = event.relatedTarget;
            const issueCode = trigger.getAttribute("data-issue-code") || "";
            const bookTitle = trigger.getAttribute("data-book-title") || "Not selected";
            const studentName = trigger.getAttribute("data-student-name") || "Not selected";

            qrValueElement.textContent = issueCode;
            qrBookTitleElement.textContent = bookTitle;
            qrStudentNameElement.textContent = studentName;
            currentIssueQrCanvas = window.LuLibrisyncQr.renderQr(qrCanvasElement, issueCode, {
                size: 220,
                emptyText: "No QR code available.",
                errorText: "Unable to render this QR code."
            });
            downloadIssueQrButton.disabled = !currentIssueQrCanvas;
            downloadIssueQrButton.dataset.filename = window.LuLibrisyncQr.normalizeFilename(issueCode, "issue-code") + ".png";
        });

        downloadIssueQrButton.addEventListener("click", function () {
            if (!currentIssueQrCanvas) {
                return;
            }

            window.LuLibrisyncQr.downloadCanvas(currentIssueQrCanvas, downloadIssueQrButton.dataset.filename);
        });

        const reservationScannerModalElement = document.getElementById("reservationQrScannerModal");
        const reservationScannerUploadInput = document.getElementById("reservationScannerUpload");
        const reservationQrCodeField = document.getElementById("reservationQrCodeField");
        const reservationClaimForm = document.getElementById("reservationQrClaimForm");
        const reservationScannerView = document.getElementById("reservationScannerView");
        const reservationScannerPreview = document.getElementById("reservationScannerPreview");
        const reservationPreviewCode = document.getElementById("reservationPreviewCode");
        const reservationPreviewConfirmBtn = document.getElementById("reservationPreviewConfirmBtn");
        const reservationPreviewRescanBtn = document.getElementById("reservationPreviewRescanBtn");

        const reservationScanner = window.LuLibrisyncQr.createScanner({
            videoElement: document.getElementById("reservationScannerVideo"),
            statusElement: document.getElementById("reservationScannerStatus"),
            formats: ["qr_code"],
            liveMessage: "Scanner is live. Aim the camera at the student's pickup QR and hold it steady.",
            qrFallbackMessage: "QR-only scanning is active on this browser. Aim the camera at the student's reservation QR.",
            unsupportedMessage: "This browser cannot decode live QR codes. You can still upload a saved QR image.",
            permissionMessage: "Camera access was blocked or unavailable. Please allow camera use, then try again.",
            fileSuccessMessage: "QR image decoded. Review the details below.",
            onDetected: showScanPreview,
            onScanError: function () {
                window.LuLibrisyncQr.setStatus(
                    document.getElementById("reservationScannerStatus"),
                    "Camera access is active, but the current frame could not be decoded yet.",
                    true
                );
            }
        });

        function showScanPreview(rawValue) {
            const detectedCode = (rawValue || "").trim();
            if (!detectedCode) {
                return;
            }
            reservationScanner.stop();
            reservationQrCodeField.value = detectedCode;
            reservationPreviewCode.textContent = detectedCode;
            reservationScannerView.hidden = true;
            reservationScannerPreview.hidden = false;
        }

        function resetToScanner() {
            reservationQrCodeField.value = "";
            reservationPreviewCode.textContent = "";
            reservationScannerPreview.hidden = true;
            reservationScannerView.hidden = false;
            reservationScanner.start();
        }

        reservationPreviewConfirmBtn.addEventListener("click", function () {
            reservationClaimForm.requestSubmit();
        });

        reservationPreviewRescanBtn.addEventListener("click", function () {
            resetToScanner();
        });

        reservationScannerModalElement.addEventListener("shown.bs.modal", function () {
            reservationScannerPreview.hidden = true;
            reservationScannerView.hidden = false;
            reservationScanner.start();
        });

        reservationScannerModalElement.addEventListener("hidden.bs.modal", function () {
            reservationScanner.stop();
            reservationScanner.setStatus("Camera scanner is preparing. Hold the student pickup QR inside the highlighted frame.", false);
            reservationScannerUploadInput.value = "";
            reservationQrCodeField.value = "";
            reservationPreviewCode.textContent = "";
            reservationScannerPreview.hidden = true;
            reservationScannerView.hidden = false;
        });

        reservationScannerUploadInput.addEventListener("change", function (event) {
            const selectedFile = event.target.files && event.target.files[0];
            if (!selectedFile) {
                return;
            }

            reservationScanner.decodeFile(selectedFile);
            reservationScannerUploadInput.value = "";
        });
    });
</script>
</body>
</html>


