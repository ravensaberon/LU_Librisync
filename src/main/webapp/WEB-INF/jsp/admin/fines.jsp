<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Fine Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Fine Ledger</span>
            <div class="brand-title mt-2">Payment and waiver control</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/books">Books</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/issues">Issue / Return</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/reservations">Reservations</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/students">Students</a>
            <a class="nav-pill active" href="${pageContext.request.contextPath}/admin/fines">Fines</a>
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
        <div class="hero-card-grid">
            <div>
                <span class="tag-chip">Financial Accountability</span>
                <h1 class="fw-bold mt-3 mb-2">Manage overdue charges with a real ledger</h1>
                <p class="muted-text mb-0">Track unpaid balances, mark penalties as settled, and record waived charges without losing the circulation history behind each fine.</p>
            </div>
            <div class="hero-side-note">
                <div class="hero-side-title">Filtered fine records</div>
                <strong class="hero-side-value">${filteredFineCount}</strong>
                <span class="hero-side-caption">Outstanding in current list: ${filteredOutstandingTotal}</span>
            </div>
        </div>
    </section>

    <section class="stat-grid mb-4">
        <div class="metric-card">
            <div class="metric-value">${outstandingFineCount}</div>
            <div class="metric-label">Outstanding records</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${outstandingFineTotal}</div>
            <div class="metric-label">Remaining balance</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${paidFineCount}</div>
            <div class="metric-label">Paid fine records</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${paidFineTotal}</div>
            <div class="metric-label">Collected amount</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${waivedFineCount}</div>
            <div class="metric-label">Waived fine records</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${waivedFineTotal}</div>
            <div class="metric-label">Waived amount</div>
        </div>
    </section>

    <section class="panel-grid mb-4">
        <div class="panel-card">
            <div class="section-title">Filter fine ledger</div>
            <form method="get" action="${pageContext.request.contextPath}/admin/fines" class="row g-3 align-items-end">
                <div class="col-md-5">
                    <label class="form-label" for="studentKeyword">Student search</label>
                    <input class="form-control" id="studentKeyword" name="studentKeyword" value="${studentKeyword}" placeholder="Student ID, name, or email">
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="status">Fine status</label>
                    <select class="form-select" id="status" name="status">
                        <option value="">All statuses</option>
                        <c:forEach items="${fineStatuses}" var="fineStatus">
                            <option value="${fineStatus}" <c:if test="${selectedStatus == fineStatus}">selected</c:if>>${fineStatus}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-3 d-grid">
                    <button class="btn btn-brand" type="submit">
                        <i class="bi bi-funnel me-2"></i>Apply filters
                    </button>
                </div>
            </form>
        </div>

        <div class="panel-card">
            <div class="section-title">Recommended desk workflow</div>
            <div class="support-list">
                <div class="support-item">
                    <strong>Review the borrower context</strong>
                    <span>Use student search and the issue code in this ledger before accepting payment or waiving a penalty.</span>
                </div>
                <div class="support-item">
                    <strong>Record staff-side decisions clearly</strong>
                    <span>Paid and waived actions write into the audit trail so accountability is visible in the admin reports module.</span>
                </div>
            </div>
        </div>
    </section>

    <section class="panel-card">
        <div class="section-title">Fine records</div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Student</th>
                    <th>Book / Issue</th>
                    <th>Ledger</th>
                    <th>Status</th>
                    <th>Dates</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${fines}" var="fine">
                    <tr>
                        <td>
                            <strong>${fine.student.user.name}</strong>
                            <div class="muted-text">${fine.student.studentId}</div>
                        </td>
                        <td>
                            <strong>${fine.issueRecord.book.title}</strong>
                            <div class="muted-text text-truncate" style="max-width: 150px;">${fine.issueRecord.qrIssueCode}</div>
                        </td>
                        <td>
                            <div class="fw-bold">Total: ${fine.amount}</div>
                            <div class="text-success small">Paid: ${fine.paidAmount}</div>
                            <div class="text-danger small">Rem: ${fine.remainingAmount}</div>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${fine.status.name() == 'UNPAID'}">
                                    <span class="tag-chip warn">UNPAID</span>
                                </c:when>
                                <c:when test="${fine.status.name() == 'PARTIALLY_PAID'}">
                                    <span class="tag-chip info">PARTIAL</span>
                                </c:when>
                                <c:when test="${fine.status.name() == 'PAID'}">
                                    <span class="tag-chip">PAID</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="tag-chip subtle">WAIVED</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="small">
                            <div>Calc: ${fine.calculatedAt}</div>
                            <c:if test="${not empty fine.paidAt}">
                                <div class="text-success">Settled: ${fine.paidAt}</div>
                            </c:if>
                        </td>
                        <td class="table-actions">
                            <c:if test="${fine.status.name() == 'UNPAID' || fine.status.name() == 'PARTIALLY_PAID'}">
                                <button class="icon-action" type="button" title="Record payment"
                                        data-bs-toggle="modal" data-bs-target="#paymentModal${fine.id}">
                                    <i class="bi bi-cash-coin"></i>
                                </button>
                                <form method="post" class="d-inline"
                                      action="${pageContext.request.contextPath}/admin/fines/${fine.id}/waive"
                                      data-confirm-title="Waive remaining fine?"
                                      data-confirm-text="This will cancel the remaining balance for this penalty."
                                      data-confirm-button-text="Yes, waive fine"
                                      data-confirm-cancel-text="Keep as is">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button class="icon-action" type="submit" title="Waive fine">
                                        <i class="bi bi-receipt-cutoff"></i>
                                    </button>
                                </form>
                            </c:if>
                            <button class="icon-action" type="button" title="View history"
                                    data-bs-toggle="modal" data-bs-target="#historyModal${fine.id}">
                                <i class="bi bi-clock-history"></i>
                            </button>

                            <!-- Payment Modal -->
                            <div class="modal fade" id="paymentModal${fine.id}" tabindex="-1" aria-hidden="true">
                                <div class="modal-dialog">
                                    <div class="modal-content">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/fines/${fine.id}/pay-partial">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <div class="modal-header">
                                                <h5 class="modal-title">Record Fine Payment</h5>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                            </div>
                                            <div class="modal-body text-start">
                                                <div class="mb-3">
                                                    <label class="form-label">Remaining Balance</label>
                                                    <input type="text" class="form-control" value="${fine.remainingAmount}" readonly disabled>
                                                </div>
                                                <div class="mb-3">
                                                    <label class="form-label" for="amount${fine.id}">Payment Amount</label>
                                                    <input type="number" step="0.01" class="form-control" id="amount${fine.id}" name="amount"
                                                           max="${fine.remainingAmount}" min="0.01" value="${fine.remainingAmount}" required>
                                                </div>
                                                <div class="mb-3">
                                                    <label class="form-label" for="paymentMethod${fine.id}">Payment Method</label>
                                                    <select class="form-select" id="paymentMethod${fine.id}" name="paymentMethod" required>
                                                        <option value="CASH">Cash</option>
                                                        <option value="GCASH">GCash</option>
                                                        <option value="MAYA">Maya</option>
                                                        <option value="BANK_TRANSFER">Bank Transfer</option>
                                                    </select>
                                                </div>
                                                <div class="mb-3">
                                                    <label class="form-label" for="receiptNumber${fine.id}">Receipt / Reference Number</label>
                                                    <input type="text" class="form-control" id="receiptNumber${fine.id}" name="receiptNumber" placeholder="OR-XXXXXX" required>
                                                </div>
                                                <div class="mb-3">
                                                    <label class="form-label" for="remarks${fine.id}">Remarks</label>
                                                    <textarea class="form-control" id="remarks${fine.id}" name="remarks" rows="2"></textarea>
                                                </div>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                <button type="submit" class="btn btn-brand">Post Payment</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>

                            <!-- History Modal -->
                            <div class="modal fade" id="historyModal${fine.id}" tabindex="-1" aria-hidden="true">
                                <div class="modal-dialog modal-lg">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h5 class="modal-title">Payment History - ${fine.issueRecord.book.title}</h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body text-start">
                                            <div class="mb-4">
                                                <h6>Fine Details</h6>
                                                <div class="row g-3">
                                                    <div class="col-sm-4">
                                                        <div class="small muted-text">Original Amount</div>
                                                        <div class="fw-bold">${fine.amount}</div>
                                                    </div>
                                                    <div class="col-sm-4">
                                                        <div class="small muted-text">Total Paid</div>
                                                        <div class="fw-bold text-success">${fine.paidAmount}</div>
                                                    </div>
                                                    <div class="col-sm-4">
                                                        <div class="small muted-text">Outstanding</div>
                                                        <div class="fw-bold text-danger">${fine.remainingAmount}</div>
                                                    </div>
                                                </div>
                                            </div>
                                            <h6>Transactions</h6>
                                            <div class="table-responsive">
                                                <table class="table table-sm align-middle">
                                                    <thead>
                                                        <tr>
                                                            <th>Date</th>
                                                            <th>Amount</th>
                                                            <th>Method</th>
                                                            <th>Receipt</th>
                                                            <th>Notes</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach items="${fine.payments}" var="payment">
                                                            <tr>
                                                                <td>${payment.paymentDate}</td>
                                                                <td class="fw-bold">${payment.amount}</td>
                                                                <td>${payment.paymentMethod}</td>
                                                                <td>${payment.receiptNumber}</td>
                                                                <td class="small">${payment.remarks}</td>
                                                            </tr>
                                                        </c:forEach>
                                                        <c:if test="${empty fine.payments}">
                                                            <tr>
                                                                <td colspan="5" class="text-center muted-text">No payments recorded yet.</td>
                                                            </tr>
                                                        </c:if>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty fines}">
                    <tr>
                        <td colspan="7" class="text-center muted-text">No fine records matched the current filters.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
