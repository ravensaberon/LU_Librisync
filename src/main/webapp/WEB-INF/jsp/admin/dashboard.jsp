<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Admin Console</span>
            <div class="brand-title mt-2">LU Librisync Dashboard</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill active" href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/books">Books</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/issues">Issue / Return</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/reservations">Reservations</a>
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

    <section class="hero-card mb-4">
        <div class="hero-card-grid">
            <div>
                <span class="tag-chip">Library Health</span>
                <h1 class="fw-bold mt-3 mb-2">Library overview</h1>
                <p class="muted-text mb-0">Monitor circulation, student demand, and account risk from one clearer dashboard.</p>
            </div>
            <div class="hero-side-note">
                <div class="hero-side-title">Needs attention</div>
                <strong class="hero-side-value">${overdueCount + blockedBorrowerCount}</strong>
                <span class="hero-side-caption">${overdueCount} overdue case(s) and ${blockedBorrowerCount} blocked borrower(s) currently need follow-up.</span>
            </div>
        </div>
    </section>

    <section class="dashboard-kpi-grid mb-4">
        <article class="panel-card dashboard-kpi-card dashboard-kpi-card-feature">
            <span class="dashboard-kpi-label">Active circulation</span>
            <strong class="dashboard-kpi-value">${issuedCount}</strong>
            <p class="dashboard-kpi-copy">Books currently issued to borrowers.</p>
        </article>
        <article class="panel-card dashboard-kpi-card">
            <span class="dashboard-kpi-label">Overdue cases</span>
            <strong class="dashboard-kpi-value">${overdueCount}</strong>
            <p class="dashboard-kpi-copy">${overdueRate}% of active desk records are overdue.</p>
        </article>
        <article class="panel-card dashboard-kpi-card">
            <span class="dashboard-kpi-label">Pending reservations</span>
            <strong class="dashboard-kpi-value">${pendingReservationCount}</strong>
            <p class="dashboard-kpi-copy">${readyReservationCount} reservation(s) already ready for claim.</p>
        </article>
        <article class="panel-card dashboard-kpi-card">
            <span class="dashboard-kpi-label">Unpaid balance</span>
            <strong class="dashboard-kpi-value">${outstandingFineTotal}</strong>
            <p class="dashboard-kpi-copy">${outstandingFineCount} open fine record(s) still unpaid.</p>
        </article>
    </section>

    <section class="panel-card mb-4">
        <div class="dashboard-summary-grid">
            <div class="dashboard-summary-item">
                <span class="dashboard-summary-label">Catalog</span>
                <strong class="dashboard-summary-value">${bookCount}</strong>
                <span class="dashboard-summary-copy">${availableCount} available for issue</span>
            </div>
            <div class="dashboard-summary-item">
                <span class="dashboard-summary-label">Borrowers</span>
                <strong class="dashboard-summary-value">${studentCount}</strong>
                <span class="dashboard-summary-copy">${blockedBorrowerCount} blocked account(s)</span>
            </div>
            <div class="dashboard-summary-item">
                <span class="dashboard-summary-label">Reservations</span>
                <strong class="dashboard-summary-value">${pendingReservationCount + readyReservationCount}</strong>
                <span class="dashboard-summary-copy">${readyReservationCount} ready for claim</span>
            </div>
            <div class="dashboard-summary-item">
                <span class="dashboard-summary-label">Risk level</span>
                <strong class="dashboard-summary-value">${overdueRate}%</strong>
                <span class="dashboard-summary-copy">Overdue rate across active circulation</span>
            </div>
        </div>
    </section>

    <section class="panel-card mb-4">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
            <div>
                <div class="section-title mb-2">Admin operations center</div>
                <p class="helper-copy">Jump straight into the main staff workspaces without digging through long descriptions.</p>
            </div>
        </div>
        <div class="dashboard-action-grid">
            <a class="dashboard-action-card" href="${pageContext.request.contextPath}/admin/books">
                <span class="dashboard-action-icon"><i class="bi bi-journals"></i></span>
                <div class="dashboard-action-copy">
                    <h3>Catalog and inventory</h3>
                    <p>Books, ISBN, copies, and shelf records.</p>
                </div>
                <span class="dashboard-action-meta">${bookCount} books</span>
            </a>
            <a class="dashboard-action-card" href="${pageContext.request.contextPath}/admin/issues">
                <span class="dashboard-action-icon"><i class="bi bi-arrow-left-right"></i></span>
                <div class="dashboard-action-copy">
                    <h3>Circulation desk</h3>
                    <p>Issue, return, and due date handling.</p>
                </div>
                <span class="dashboard-action-meta">${issuedCount} active</span>
            </a>
            <a class="dashboard-action-card" href="${pageContext.request.contextPath}/admin/students">
                <span class="dashboard-action-icon"><i class="bi bi-people"></i></span>
                <div class="dashboard-action-copy">
                    <h3>Borrower accounts</h3>
                    <p>Student records, status, and password resets.</p>
                </div>
                <span class="dashboard-action-meta">${studentCount} students</span>
            </a>
            <a class="dashboard-action-card" href="${pageContext.request.contextPath}/admin/reservations">
                <span class="dashboard-action-icon"><i class="bi bi-bookmark-check"></i></span>
                <div class="dashboard-action-copy">
                    <h3>Reservation queue</h3>
                    <p>Pending holds and release-ready pickups.</p>
                </div>
                <span class="dashboard-action-meta">${pendingReservationCount} pending</span>
            </a>
            <a class="dashboard-action-card" href="${pageContext.request.contextPath}/admin/fines">
                <span class="dashboard-action-icon"><i class="bi bi-receipt"></i></span>
                <div class="dashboard-action-copy">
                    <h3>Fine ledger</h3>
                    <p>Payments, waivers, and open balances.</p>
                </div>
                <span class="dashboard-action-meta">${outstandingFineTotal} unpaid</span>
            </a>
            <a class="dashboard-action-card" href="${pageContext.request.contextPath}/admin/reports">
                <span class="dashboard-action-icon"><i class="bi bi-bar-chart"></i></span>
                <div class="dashboard-action-copy">
                    <h3>Reports and exports</h3>
                    <p>Analytics, summaries, and CSV output.</p>
                </div>
                <span class="dashboard-action-meta">Open reports</span>
            </a>
            <a class="dashboard-action-card" href="${pageContext.request.contextPath}/admin/references">
                <span class="dashboard-action-icon"><i class="bi bi-tags"></i></span>
                <div class="dashboard-action-copy">
                    <h3>Categories and authors</h3>
                    <p>Reference data for cleaner catalog search.</p>
                </div>
                <span class="dashboard-action-meta">Maintain data</span>
            </a>
            <a class="dashboard-action-card" href="${pageContext.request.contextPath}/admin/profile">
                <span class="dashboard-action-icon"><i class="bi bi-shield-lock"></i></span>
                <div class="dashboard-action-copy">
                    <h3>Admin security</h3>
                    <p>Profile settings and password protection.</p>
                </div>
                <span class="dashboard-action-meta">Account settings</span>
            </a>
        </div>
    </section>

    <section class="panel-grid mb-4">
        <div class="panel-card chart-card">
            <div class="chart-header">
                <div class="chart-copy">
                    <div class="section-title mb-2">7-day circulation graph</div>
                    <p>Compare daily book issues and completed returns from the past week for a clearer view of library circulation.</p>
                </div>
                <div class="chart-legend">
                    <span class="legend-pill"><span class="legend-dot issued"></span>Issued</span>
                    <span class="legend-pill"><span class="legend-dot returned"></span>Returned</span>
                </div>
            </div>
            <div class="chart-layout">
                <div class="chart-canvas-shell">
                    <canvas id="circulationChart" aria-label="Weekly circulation chart"></canvas>
                </div>
                <div class="chart-summary-grid">
                    <div class="chart-summary-card">
                        <span class="chart-summary-label">Issued this cycle</span>
                        <strong class="chart-summary-value">${issuedCount}</strong>
                        <span class="chart-summary-note">Books currently out in circulation.</span>
                    </div>
                    <div class="chart-summary-card">
                        <span class="chart-summary-label">Overdue cases</span>
                        <strong class="chart-summary-value">${overdueCount}</strong>
                        <span class="chart-summary-note">Items already beyond their due date.</span>
                    </div>
                    <div class="chart-summary-card">
                        <span class="chart-summary-label">Available books</span>
                        <strong class="chart-summary-value">${availableCount}</strong>
                        <span class="chart-summary-note">Titles ready for new issue transactions.</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="panel-card">
            <div class="section-title">Recent borrowing activity</div>
            <div class="table-responsive">
                <table class="table align-middle">
                    <thead>
                    <tr>
                        <th>Book</th>
                        <th>Student</th>
                        <th>Due date</th>
                        <th>Status</th>
                        <th>Fine</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${recentIssues}" var="issue">
                        <tr>
                            <td>${issue.book.title}</td>
                            <td>${issue.student.user.name}</td>
                            <td>${issue.dueDate}</td>
                            <td><span class="tag-chip">${issue.status}</span></td>
                            <td>${issue.fineAmount}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty recentIssues}">
                        <tr>
                            <td colspan="5" class="text-center muted-text">No issue records yet.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="panel-card">
            <div class="section-title">Most borrowed books</div>
            <ul class="list-clean">
                <c:forEach items="${mostBorrowedBooks}" var="item">
                    <li class="d-flex justify-content-between align-items-center">
                        <span>${item.title}</span>
                        <span class="tag-chip warn">${item.borrowCount} borrow(s)</span>
                    </li>
                </c:forEach>
                <c:if test="${empty mostBorrowedBooks}">
                    <li class="muted-text">Analytics will appear after circulation data grows.</li>
                </c:if>
            </ul>

            <hr class="my-4">

            <div class="section-title">Admin focus areas</div>
            <div class="support-list">
                <div class="support-item">
                    <strong>Daily circulation monitoring</strong>
                    <span>Use the graph and recent activity table to spot borrowing spikes, slow returns, and overdue trends quickly.</span>
                </div>
                <div class="support-item">
                    <strong>Collection maintenance</strong>
                    <span>Keep books, categories, and authors updated so students can browse a cleaner and more accurate catalog.</span>
                </div>
                <div class="support-item">
                    <strong>Student account follow-up</strong>
                    <span>Open student details to review active loans, fines, and borrowing history before handling concerns at the desk.</span>
                </div>
            </div>
        </div>

        <div class="panel-card">
            <div class="section-title">Recent outstanding fines</div>
            <div class="table-responsive">
                <table class="table align-middle">
                    <thead>
                    <tr>
                        <th>Student</th>
                        <th>Book</th>
                        <th>Amount</th>
                        <th>Recorded</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${recentOutstandingFines}" var="fine">
                        <tr>
                            <td>${fine.student.studentId} - ${fine.student.user.name}</td>
                            <td>${fine.issueRecord.book.title}</td>
                            <td>${fine.amount}</td>
                            <td>${fine.calculatedAt}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty recentOutstandingFines}">
                        <tr>
                            <td colspan="4" class="text-center muted-text">No outstanding fines are recorded right now.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="panel-card">
            <div class="section-title">Recent audit trail</div>
            <div class="audit-timeline">
                <c:forEach items="${recentAuditLogs}" var="log">
                    <div class="audit-item">
                        <div class="audit-item-badge"><i class="bi bi-shield-check"></i></div>
                        <div>
                            <div class="audit-item-heading">${log.summary}</div>
                            <div class="audit-item-meta">${log.action} | ${empty log.actorName ? 'System' : log.actorName} | ${log.createdAt}</div>
                            <c:if test="${not empty log.details}">
                                <div class="audit-item-copy">${log.details}</div>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
                <c:if test="${empty recentAuditLogs}">
                    <div class="muted-text">Audit trail data will appear after admin and system actions are recorded.</div>
                </c:if>
            </div>
        </div>
    </section>
</div>
<script>
    (function () {
        var chartCanvas = document.getElementById("circulationChart");
        if (!chartCanvas || typeof Chart === "undefined") {
            return;
        }

        var labels = [
            <c:forEach items="${weeklyChart}" var="point" varStatus="status">
                "${point.label}"<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];

        var issuedData = [
            <c:forEach items="${weeklyChart}" var="point" varStatus="status">
                ${point.issuedCount}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];

        var returnedData = [
            <c:forEach items="${weeklyChart}" var="point" varStatus="status">
                ${point.returnedCount}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];

        new Chart(chartCanvas, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [
                    {
                        type: "bar",
                        label: "Issued",
                        data: issuedData,
                        backgroundColor: "rgba(15, 127, 52, 0.88)",
                        borderRadius: 12,
                        borderSkipped: false,
                        maxBarThickness: 34
                    },
                    {
                        type: "bar",
                        label: "Returned",
                        data: returnedData,
                        backgroundColor: "rgba(145, 213, 166, 0.96)",
                        borderRadius: 12,
                        borderSkipped: false,
                        maxBarThickness: 34
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: "index",
                    intersect: false
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        backgroundColor: "rgba(23, 42, 28, 0.96)",
                        padding: 12,
                        titleFont: {
                            family: "Manrope"
                        },
                        bodyFont: {
                            family: "Manrope"
                        }
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            color: "#5d7065",
                            font: {
                                family: "Manrope",
                                weight: "700"
                            }
                        }
                    },
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0,
                            color: "#6d7a70",
                            font: {
                                family: "Manrope"
                            }
                        },
                        grid: {
                            color: "rgba(15, 127, 52, 0.10)"
                        },
                        border: {
                            display: false
                        }
                    }
                }
            }
        });
    })();
</script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
