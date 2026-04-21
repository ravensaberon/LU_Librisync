<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Student Directory</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Student Search</span>
            <div class="brand-title mt-2">Find students by ID</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/books">Books</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/issues">Issue / Return</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/reservations">Reservations</a>
            <a class="nav-pill active" href="${pageContext.request.contextPath}/admin/students">Students</a>
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

    <section class="panel-grid mb-4">
        <div class="panel-card">
            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                <div>
                    <div class="section-title mb-2">Student account controls</div>
                    <p class="helper-copy">Open student details in a popup window, update records faster, and keep the directory page visible while you work.</p>
                </div>
                <button class="btn btn-brand" type="button" data-bs-toggle="modal" data-bs-target="#createStudentModal">
                    <i class="bi bi-person-plus me-2"></i>Create student
                </button>
            </div>
            <div class="support-list">
                <div class="support-item">
                    <strong>Modal-based account review</strong>
                    <span>View borrower details, active loans, borrowing history, password reset, and account status in one popup instead of opening another page.</span>
                </div>
                <div class="support-item">
                    <strong>Quick admin actions</strong>
                    <span>Use icon buttons in the directory to open accounts faster and keep circulation work moving without losing context.</span>
                </div>
            </div>
        </div>

        <div class="panel-card">
            <div class="section-title">Search student directory</div>
            <form method="get" action="${pageContext.request.contextPath}/admin/students" class="row g-3 align-items-end">
                <div class="col-md-9">
                    <label class="form-label" for="studentId">Student ID</label>
                    <input class="form-control" id="studentId" name="studentId" value="${studentIdFilter}" placeholder="Example: 261-0003">
                </div>
                <div class="col-md-3 d-grid">
                    <button class="btn btn-brand" type="submit">
                        <i class="bi bi-search me-2"></i>Search
                    </button>
                </div>
            </form>
        </div>
    </section>

    <section class="panel-card">
        <div class="section-title">Student directory</div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Student ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Program</th>
                    <th>Year level</th>
                    <th>Standing</th>
                    <th>Outstanding</th>
                    <th>Phone</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${students}" var="student">
                    <c:set var="standing" value="${borrowerStandingByStudentId[student.studentId]}"/>
                    <tr>
                        <td>${student.studentId}</td>
                        <td>${student.user.name}</td>
                        <td>${student.user.email}</td>
                        <td>${student.course}</td>
                        <td>${student.yearLevel}</td>
                        <td>
                            <c:choose>
                                <c:when test="${standing.eligibleToBorrow}">
                                    <span class="tag-chip">Borrowing cleared</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="tag-chip warn">Blocked</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>${standing.outstandingFineAmount}</td>
                        <td>${student.phone}</td>
                        <td><span class="tag-chip">${student.user.status}</span></td>
                        <td class="table-actions">
                            <button class="icon-action"
                                    type="button"
                                    data-student-id="${student.studentId}"
                                    data-student-label="${student.user.name}"
                                    title="Open student details">
                                <i class="bi bi-eye"></i>
                            </button>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty students}">
                    <tr>
                        <td colspan="10" class="text-center muted-text">No student matched the entered ID.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>

<div class="modal fade" id="createStudentModal" tabindex="-1" aria-labelledby="createStudentModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-xl">
        <div class="modal-content">
            <div class="modal-header modal-header-brand">
                <div>
                    <div class="modal-kicker">Create Student</div>
                    <h2 class="modal-title h4 mb-1" id="createStudentModalLabel">Register a new borrower account</h2>
                    <p class="modal-subtitle mb-0">Student ID is generated automatically after successful account creation.</p>
                </div>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <form method="post" action="${pageContext.request.contextPath}/admin/students" class="row g-3">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                    <div class="col-md-6">
                        <label class="form-label" for="createName">Full name</label>
                        <input class="form-control" id="createName" name="name" placeholder="Example: Maria Santos" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="createEmail">Email address</label>
                        <input class="form-control" id="createEmail" name="email" type="email" placeholder="student@example.com" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createPassword">Temporary password</label>
                        <input class="form-control" id="createPassword" name="password" type="password" minlength="12" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createCourse">Program</label>
                        <input class="form-control" id="createCourse" name="course" placeholder="BSIT">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createYearLevel">Year level</label>
                        <input class="form-control" id="createYearLevel" name="yearLevel" placeholder="1st Year">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createPhone">Phone</label>
                        <input class="form-control" id="createPhone" name="phone" placeholder="+639171234567">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createDateOfBirth">Birth date</label>
                        <input class="form-control" id="createDateOfBirth" name="dateOfBirth" type="date">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createStatus">Account status</label>
                        <select class="form-select" id="createStatus" name="status">
                            <c:forEach items="${userStatuses}" var="status">
                                <option value="${status}">${status}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="createAddress">Address</label>
                        <textarea class="form-control" id="createAddress" name="address" rows="3" placeholder="Optional address or admin note"></textarea>
                    </div>
                    <div class="col-12 d-flex flex-wrap gap-2">
                        <button class="btn btn-brand" type="submit">
                            <i class="bi bi-check-circle me-2"></i>Create student
                        </button>
                        <button class="btn btn-warm" type="button" data-bs-dismiss="modal">Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="studentDetailModal" tabindex="-1" aria-labelledby="studentDetailModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-fullscreen-lg-down modal-xl">
        <div class="modal-content" id="studentDetailModalContent">
            <div class="modal-body modal-loading-state">
                <div class="text-center py-5">
                    <div class="spinner-border text-success mb-3" role="status" aria-hidden="true"></div>
                    <p class="mb-0 muted-text">Loading student details...</p>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function () {
        var modalElement = document.getElementById("studentDetailModal");
        var modalContent = document.getElementById("studentDetailModalContent");
        var studentButtons = document.querySelectorAll("[data-student-id]");
        var bootstrapModal = modalElement ? new bootstrap.Modal(modalElement) : null;
        var autoOpenStudentId = "${modalStudentId}";

        function showLoadingState() {
            modalContent.innerHTML = '' +
                '<div class="modal-body modal-loading-state">' +
                '    <div class="text-center py-5">' +
                '        <div class="spinner-border text-success mb-3" role="status" aria-hidden="true"></div>' +
                '        <p class="mb-0 muted-text">Loading student details...</p>' +
                '    </div>' +
                '</div>';
        }

        function showErrorState() {
            modalContent.innerHTML = '' +
                '<div class="modal-header modal-header-brand">' +
                '    <div>' +
                '        <div class="modal-kicker">Student Account</div>' +
                '        <h2 class="modal-title h4 mb-1">Unavailable</h2>' +
                '        <p class="modal-subtitle mb-0">The student detail popup could not be loaded.</p>' +
                '    </div>' +
                '    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>' +
                '</div>' +
                '<div class="modal-body modal-loading-state">' +
                '    <div class="text-center py-5">' +
                '        <i class="bi bi-exclamation-circle modal-empty-icon"></i>' +
                '        <p class="mb-0 muted-text">Unable to load the student record right now.</p>' +
                '    </div>' +
                '</div>';
        }

        function openStudentModal(studentId) {
            if (!studentId || !bootstrapModal) {
                return;
            }

            showLoadingState();
            bootstrapModal.show();

            fetch("${pageContext.request.contextPath}/admin/students/" + encodeURIComponent(studentId) + "/modal", {
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                }
            })
                .then(function (response) {
                    if (!response.ok) {
                        throw new Error("Request failed");
                    }
                    return response.text();
                })
                .then(function (html) {
                    modalContent.innerHTML = html;
                })
                .catch(function () {
                    showErrorState();
                });
        }

        studentButtons.forEach(function (button) {
            button.addEventListener("click", function () {
                openStudentModal(button.getAttribute("data-student-id"));
            });
        });

        if (autoOpenStudentId) {
            openStudentModal(autoOpenStudentId);
        }
    })();
</script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
