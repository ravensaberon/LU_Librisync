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
            <span class="tag-chip">Student Directory</span>
            <div class="brand-title mt-2">Manage borrower accounts</div>
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

    <section class="hero-card mb-4">
        <div class="hero-card-grid">
            <div>
                <span class="tag-chip">Borrower Accounts</span>
                <h1 class="fw-bold mt-3 mb-2">Student directory</h1>
                <p class="muted-text mb-0">Search, review, and manage student borrower records from one clean workspace.</p>
            </div>
            <div class="hero-side-note">
                <c:choose>
                    <c:when test="${not empty studentIdFilter}">
                        <div class="hero-side-title">Search matches</div>
                        <strong class="hero-side-value">${studentDirectoryFilteredCount}</strong>
                        <span class="hero-side-caption">Student ID filter: ${studentIdFilter}</span>
                    </c:when>
                    <c:otherwise>
                        <div class="hero-side-title">Registered students</div>
                        <strong class="hero-side-value">${studentDirectoryTotalCount}</strong>
                        <span class="hero-side-caption">${studentDirectoryBlockedCount} blocked borrowers need follow-up.</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <section class="stat-grid mb-4">
        <div class="metric-card">
            <div class="metric-value">${studentDirectoryTotalCount}</div>
            <div class="metric-label">Total student accounts</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${studentDirectoryFilteredCount}</div>
            <div class="metric-label">Shown in current view</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${studentDirectoryClearedCount}</div>
            <div class="metric-label">Borrowing cleared</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${studentDirectoryActiveCount}</div>
            <div class="metric-label">Active accounts</div>
        </div>
    </section>

    <section class="panel-card directory-workspace">
        <div class="directory-toolbar">
            <div class="directory-toolbar-copy">
                <div class="section-title mb-2">Student directory</div>
                <div class="directory-toolbar-meta">
                    <span class="directory-meta-pill">${studentDirectoryFilteredCount} results</span>
                    <c:if test="${not empty studentIdFilter}">
                        <span class="directory-meta-pill subtle">ID: ${studentIdFilter}</span>
                    </c:if>
                    <span class="directory-meta-text">${studentDirectoryBlockedCount} blocked borrower<c:if test="${studentDirectoryBlockedCount != 1}">s</c:if> in this view</span>
                </div>
            </div>
            <div class="directory-toolbar-actions">
                <form method="get" action="${pageContext.request.contextPath}/admin/students" class="directory-search-form">
                    <label class="visually-hidden" for="studentId">Search by student ID</label>
                    <div class="directory-search-input">
                        <i class="bi bi-search" aria-hidden="true"></i>
                        <input class="form-control"
                               id="studentId"
                               name="studentId"
                               value="${studentIdFilter}"
                               placeholder="Search by student ID">
                    </div>
                    <button class="btn btn-brand" type="submit">Search</button>
                    <c:if test="${not empty studentIdFilter}">
                        <a class="btn btn-warm" href="${pageContext.request.contextPath}/admin/students">Clear</a>
                    </c:if>
                </form>
                <button class="btn btn-brand" type="button" data-bs-toggle="modal" data-bs-target="#createStudentModal">
                    <i class="bi bi-person-plus me-2"></i>Create student
                </button>
            </div>
        </div>

        <div class="table-responsive directory-table-wrap">
            <table class="table align-middle directory-table">
                <thead>
                <tr>
                    <th>Student</th>
                    <th>Program</th>
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
                        <td>
                            <div class="directory-student-cell">
                                <strong>${student.user.name}</strong>
                                <span>${student.studentId}</span>
                                <span>${student.user.email}</span>
                            </div>
                        </td>
                        <td>
                            <div class="directory-student-cell">
                                <strong>${empty student.course ? 'Not set' : student.course}</strong>
                                <span>${empty student.yearLevel ? 'Not set' : student.yearLevel}</span>
                            </div>
                        </td>
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
                        <td class="directory-amount">${standing.outstandingFineAmount}</td>
                        <td>${empty student.phone ? 'Not provided' : student.phone}</td>
                        <td><span class="tag-chip subtle">${student.user.status}</span></td>
                        <td class="table-actions">
                            <button class="icon-action"
                                    type="button"
                                    data-student-id="${student.studentId}"
                                    data-student-label="${student.user.name}"
                                    title="Open student details"
                                    aria-label="Open student details for ${student.user.name}">
                                <i class="bi bi-eye"></i>
                            </button>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty students}">
                    <tr>
                        <td colspan="7" class="text-center muted-text">No student matched the current student ID search.</td>
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
                        <select class="form-select" id="createCourse" name="course">
                            <option value="">Select program</option>
                            <c:forEach items="${programOptionsByCollege}" var="collegeEntry">
                                <optgroup label="${collegeEntry.key}">
                                    <c:forEach items="${collegeEntry.value}" var="programOption">
                                        <option value="${programOption}">${programOption}</option>
                                    </c:forEach>
                                </optgroup>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createYearLevel">Year level</label>
                        <select class="form-select" id="createYearLevel" name="yearLevel" required>
                            <option value="">Select year level</option>
                            <c:forEach items="${yearLevelOptions}" var="yearLevelOption">
                                <option value="${yearLevelOption}">${yearLevelOption}</option>
                            </c:forEach>
                        </select>
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
                    <div class="col-md-4">
                        <label class="form-label" for="createProvince">Province</label>
                        <select class="form-select" id="createProvince" name="province">
                            <option value="Laguna" selected>Laguna</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createCityMunicipality">City / Municipality</label>
                        <select class="form-select" id="createCityMunicipality" name="cityMunicipality">
                            <option value="">Select city / municipality</option>
                            <c:forEach items="${registrationCityZipCodes}" var="entry">
                                <option value="${entry.key}">${entry.key}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createBarangay">Barangay</label>
                        <select class="form-select" id="createBarangay" name="barangay" data-selected-barangay="" disabled>
                            <option value="">Select city / municipality first</option>
                        </select>
                    </div>
                    <div class="col-md-8">
                        <label class="form-label" for="createStreet">Street / House No.</label>
                        <input class="form-control" id="createStreet" name="street" placeholder="Block, lot, street">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="createZipcode">Zip code</label>
                        <input class="form-control" id="createZipcode" name="zipcode" readonly>
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
        var cityZipCodes = {
            <c:forEach items="${registrationCityZipCodes}" var="entry" varStatus="status">
            "${entry.key}": "${entry.value}"<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };

        function initAddressForms(root) {
            if (!window.LuLibrisyncAddress) {
                return;
            }

            var scope = root || document;
            window.LuLibrisyncAddress.initForm({
                cityMunicipality: scope.querySelector("#createCityMunicipality"),
                barangay: scope.querySelector("#createBarangay"),
                zipcode: scope.querySelector("#createZipcode"),
                endpoint: "${pageContext.request.contextPath}/register/barangays",
                cityZipCodes: cityZipCodes
            });
            window.LuLibrisyncAddress.initForm({
                cityMunicipality: scope.querySelector("#modalCityMunicipality"),
                barangay: scope.querySelector("#modalBarangay"),
                zipcode: scope.querySelector("#modalZipcode"),
                endpoint: "${pageContext.request.contextPath}/register/barangays",
                cityZipCodes: cityZipCodes
            });
        }

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
                    initAddressForms(modalContent);
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

        initAddressForms(document);
    })();
</script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
