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
            <div class="brand-title mt-2">Issue and return books</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/books">Books</a>
            <a class="nav-pill active" href="${pageContext.request.contextPath}/admin/issues">Issue / Return</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/students">Students</a>
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

    <section class="panel-card mb-4">
        <div class="section-title">Issue a new book</div>
        <form method="post" action="${pageContext.request.contextPath}/admin/issues" class="row g-3">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

            <div class="col-md-4">
                <label class="form-label" for="bookId">Book</label>
                <select class="form-select" id="bookId" name="bookId" required>
                    <option value="">Select book</option>
                    <c:forEach items="${availableBooks}" var="book">
                        <option value="${book.id}">${book.title} (${book.availableQuantity} available)</option>
                    </c:forEach>
                </select>
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
            </div>
            <div class="col-12">
                <button class="btn btn-brand" type="submit"><i class="bi bi-box-arrow-right me-2"></i>Issue book</button>
            </div>
        </form>
    </section>

    <c:if test="${not empty editIssue}">
        <section class="panel-card mb-4">
            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                <div>
                    <div class="section-title mb-2">Edit issue record</div>
                    <p class="helper-copy">
                        Adjust due dates or internal remarks for an existing circulation record without deleting the transaction history.
                    </p>
                </div>
                <a class="action-link" href="${pageContext.request.contextPath}/admin/issues">Cancel editing</a>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/admin/issues/${editIssue.id}/update" class="row g-3">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
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
                    <a class="btn btn-warm" href="${pageContext.request.contextPath}/admin/issues">Back to circulation desk</a>
                </div>
            </form>
        </section>
    </c:if>

    <section class="panel-card mb-4">
        <div class="section-title">Active issue records</div>
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
                        <td>${issue.issueDate}</td>
                        <td>${issue.dueDate}</td>
                        <td><span class="tag-chip">${issue.status}</span></td>
                        <td>${issue.fineAmount}</td>
                        <td class="muted-text">${issue.remarks}</td>
                        <td class="table-actions">
                            <a class="icon-action" href="${pageContext.request.contextPath}/admin/issues?editId=${issue.id}" title="Edit issue record">
                                <i class="bi bi-pencil-square"></i>
                            </a>
                            <form method="post" action="${pageContext.request.contextPath}/admin/issues/${issue.id}/return">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button class="icon-action" type="submit" title="Mark returned">
                                    <i class="bi bi-arrow-return-left"></i>
                                </button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/admin/issues/${issue.id}/delete">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button class="icon-action danger" type="submit" title="Delete issue record" onclick="return confirm('Delete this issue record?');">
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
    </section>

    <section class="panel-card">
        <div class="section-title">Circulation history</div>
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
                        <td>${issue.issueDate}</td>
                        <td>${issue.dueDate}</td>
                        <td>${issue.returnDate}</td>
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
    </section>
</div>
</body>
</html>
