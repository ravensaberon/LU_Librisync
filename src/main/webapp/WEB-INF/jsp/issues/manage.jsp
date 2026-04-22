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

    <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <section class="stat-grid mb-4">
        <div class="metric-card">
            <div class="metric-value">${activeIssueCount}</div>
            <div class="metric-label">Active desk records</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${overdueIssueCount}</div>
            <div class="metric-label">Overdue active records</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${historyCount}</div>
            <div class="metric-label">Total circulation history</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${maxLoanDays}</div>
            <div class="metric-label">Max loan days</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${maxActiveLoans}</div>
            <div class="metric-label">Max active loans per borrower</div>
        </div>
    </section>

    <section class="panel-card mb-4">
        <div class="section-title">Issue a new book</div>
        <form method="post" action="${pageContext.request.contextPath}/admin/issues" class="row g-3">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

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
                            <button class="icon-action" type="button" title="View QR code" data-bs-toggle="modal" data-bs-target="#issueQrModal" data-issue-code="${issue.qrIssueCode}" data-book-title="${issue.book.title}" data-student-name="${issue.student.user.name}">
                                <i class="bi bi-qr-code"></i>
                            </button>
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
                            <div class="info-grid">
                                <div class="info-tile">
                                    <span class="info-tile-label">Book</span>
                                    <span class="info-tile-value" id="issueQrBookTitle">Not selected</span>
                                </div>
                                <div class="info-tile">
                                    <span class="info-tile-label">Borrower</span>
                                    <span class="info-tile-value" id="issueQrStudentName">Not selected</span>
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
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/vendor/qrious.min.js"></script>
<script src="${pageContext.request.contextPath}/vendor/jsQR.js"></script>
<script src="${pageContext.request.contextPath}/js/qr-tools.js"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", function () {
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
    });
</script>
</body>
</html>
