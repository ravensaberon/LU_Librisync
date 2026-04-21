<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Library Catalog</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Advanced Search</span>
            <div class="brand-title mt-2">Browse the library catalog</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/dashboard">Dashboard</a>
            <a class="nav-pill active" href="${pageContext.request.contextPath}/student/catalog">Catalog</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/reservations">Reservations</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/profile">Profile</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/history">Borrowing history</a>
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
        <form method="get" action="${pageContext.request.contextPath}/student/catalog" class="row g-3" id="catalogSearchForm">
            <div class="col-md-3">
                <label class="form-label" for="keyword">Title or barcode</label>
                <input class="form-control" id="keyword" name="keyword" value="${keyword}">
            </div>
            <div class="col-md-3">
                <label class="form-label" for="isbn">ISBN</label>
                <input class="form-control" id="isbn" name="isbn" value="${isbnValue}">
            </div>
            <div class="col-md-2">
                <label class="form-label" for="categoryId">Category</label>
                <select class="form-select" id="categoryId" name="categoryId">
                    <option value="">All</option>
                    <c:forEach items="${categories}" var="category">
                        <option value="${category.id}" <c:if test="${selectedCategoryId == category.id}">selected</c:if>>${category.name}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label" for="authorId">Author</label>
                <select class="form-select" id="authorId" name="authorId">
                    <option value="">All</option>
                    <c:forEach items="${authors}" var="author">
                        <option value="${author.id}" <c:if test="${selectedAuthorId == author.id}">selected</c:if>>${author.name}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2 d-flex align-items-end">
                <div class="form-check">
                    <input class="form-check-input" id="availableOnly" name="availableOnly" type="checkbox" value="true" <c:if test="${availableOnly}">checked</c:if>>
                    <label class="form-check-label" for="availableOnly">Available only</label>
                </div>
            </div>
            <div class="col-12 d-flex flex-wrap gap-2">
                <button class="btn btn-brand" type="submit">
                    <i class="bi bi-search me-2"></i>Search catalog
                </button>
                <button class="btn btn-warm scanner-trigger" type="button" data-bs-toggle="modal" data-bs-target="#catalogScannerModal">
                    <i class="bi bi-upc-scan"></i>Scan barcode or ISBN
                </button>
            </div>
        </form>
    </section>

    <section class="catalog-grid">
        <c:forEach items="${books}" var="book">
            <c:set var="reservationStatus" value="${studentReservationStatusByBookId[book.id]}"/>
            <c:set var="activeIssueStatus" value="${studentActiveIssueStatusByBookId[book.id]}"/>
            <c:set var="walkInBorrowableCopies" value="${walkInBorrowableCopyCountByBook[book.id]}"/>
            <article class="catalog-card">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <h5 class="mb-0">${book.title}</h5>
                    <c:choose>
                        <c:when test="${not empty activeIssueStatus}">
                            <span class="tag-chip">Borrowed by you</span>
                        </c:when>
                        <c:when test="${reservationStatus == 'READY'}">
                            <span class="tag-chip">Ready for claim</span>
                        </c:when>
                        <c:when test="${walkInBorrowableCopies > 0}">
                            <span class="tag-chip">Available</span>
                        </c:when>
                        <c:when test="${book.availableQuantity > 0}">
                            <span class="tag-chip warn">On hold</span>
                        </c:when>
                        <c:otherwise>
                            <span class="tag-chip warn">Checked out</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <p class="muted-text mb-2">ISBN: ${book.isbn}</p>
                <p class="mb-2"><strong>Author:</strong> ${book.author.name}</p>
                <p class="mb-2"><strong>Category:</strong> ${book.category.name}</p>
                <p class="mb-2"><strong>Stock:</strong> ${book.availableQuantity} / ${book.quantity}</p>
                <p class="mb-2"><strong>Reservation queue:</strong> ${empty reservationQueueSizes[book.id] ? 0 : reservationQueueSizes[book.id]}</p>
                <p class="mb-3"><strong>Digital:</strong> <c:choose><c:when test="${book.digital}">Yes</c:when><c:otherwise>No</c:otherwise></c:choose></p>
                <p class="muted-text mb-0">${book.description}</p>
                <div class="d-flex flex-wrap gap-2 mt-3">
                    <c:if test="${book.digital and not empty book.ebookPath}">
                        <a class="btn btn-warm" href="${pageContext.request.contextPath}/student/ebooks/${book.id}">Read e-book</a>
                    </c:if>
                    <c:choose>
                        <c:when test="${not empty activeIssueStatus}">
                            <span class="tag-chip">Active loan: ${activeIssueStatus}</span>
                        </c:when>
                        <c:when test="${reservationStatus == 'READY'}">
                            <form method="post" action="${pageContext.request.contextPath}/student/catalog/${book.id}/borrow">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button class="btn btn-brand" type="submit">Borrow reserved copy</button>
                            </form>
                            <span class="muted-text">Due date: ${defaultBorrowDueDate}</span>
                        </c:when>
                        <c:when test="${walkInBorrowableCopies > 0}">
                            <form method="post" action="${pageContext.request.contextPath}/student/catalog/${book.id}/borrow">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button class="btn btn-brand" type="submit">Borrow now</button>
                            </form>
                            <span class="muted-text">Loan period: ${maxLoanDays} days</span>
                        </c:when>
                        <c:when test="${not empty reservationStatus}">
                            <span class="tag-chip warn">Reservation: ${reservationStatus}</span>
                        </c:when>
                        <c:otherwise>
                            <form method="post" action="${pageContext.request.contextPath}/student/reservations">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <input type="hidden" name="bookId" value="${book.id}">
                                <button class="btn btn-brand" type="submit">Reserve this book</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>
            </article>
        </c:forEach>
    </section>

    <c:if test="${empty books}">
        <section class="panel-card mt-4">
            <p class="mb-0 muted-text">No books matched your current filters.</p>
        </section>
    </c:if>

    <div class="modal fade" id="catalogScannerModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content border-0 shadow-lg">
                <div class="modal-header modal-header-brand">
                    <div>
                        <span class="modal-kicker">Barcode Scan</span>
                        <h2 class="h4 mb-1 mt-2">Search the catalog with your camera</h2>
                        <p class="modal-subtitle mb-0">Point your device at a library barcode, ISBN label, or QR code and the search form will update automatically.</p>
                    </div>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="scanner-shell">
                        <video id="catalogScannerVideo" autoplay muted playsinline></video>
                        <div class="scanner-overlay"></div>
                        <div class="scanner-target"></div>
                    </div>
                    <div class="scanner-status" id="catalogScannerStatus">
                        Camera scanner is preparing. Hold the code steady inside the highlighted frame.
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        const modalElement = document.getElementById("catalogScannerModal");
        const videoElement = document.getElementById("catalogScannerVideo");
        const statusElement = document.getElementById("catalogScannerStatus");
        const keywordInput = document.getElementById("keyword");
        const isbnInput = document.getElementById("isbn");
        const searchForm = document.getElementById("catalogSearchForm");

        let detector = null;
        let activeStream = null;
        let animationFrameId = null;

        function setStatus(message, isWarning) {
            statusElement.textContent = message;
            statusElement.classList.toggle("warn", Boolean(isWarning));
        }

        function stopScanner() {
            if (animationFrameId) {
                cancelAnimationFrame(animationFrameId);
                animationFrameId = null;
            }
            if (activeStream) {
                activeStream.getTracks().forEach(function (track) {
                    track.stop();
                });
                activeStream = null;
            }
            videoElement.srcObject = null;
        }

        function applyDetectedCode(rawValue) {
            const detectedCode = (rawValue || "").trim();
            if (!detectedCode) {
                return;
            }

            keywordInput.value = detectedCode;
            if (/^\d{10}(\d{3})?$/.test(detectedCode)) {
                isbnInput.value = detectedCode;
            }

            setStatus("Code detected: " + detectedCode + ". Applying it to the search form now.", false);
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) {
                modal.hide();
            }
            window.setTimeout(function () {
                searchForm.requestSubmit();
            }, 220);
        }

        async function scanLoop() {
            if (!detector || !videoElement.srcObject || videoElement.readyState < 2) {
                animationFrameId = window.requestAnimationFrame(scanLoop);
                return;
            }

            try {
                const detectedCodes = await detector.detect(videoElement);
                if (detectedCodes.length > 0) {
                    applyDetectedCode(detectedCodes[0].rawValue);
                    return;
                }
            } catch (error) {
                setStatus("Camera scanning is available, but the browser could not decode the current frame yet.", true);
            }

            animationFrameId = window.requestAnimationFrame(scanLoop);
        }

        async function startScanner() {
            if (!("BarcodeDetector" in window)) {
                setStatus("This browser does not support live barcode scanning. You can still type the ISBN or barcode manually.", true);
                return;
            }

            try {
                detector = new BarcodeDetector({
                    formats: ["ean_13", "ean_8", "upc_a", "upc_e", "code_39", "code_128", "qr_code"]
                });
                activeStream = await navigator.mediaDevices.getUserMedia({
                    video: {
                        facingMode: { ideal: "environment" }
                    },
                    audio: false
                });
                videoElement.srcObject = activeStream;
                await videoElement.play();
                setStatus("Scanner is live. Align the code inside the frame and hold still for a moment.", false);
                animationFrameId = window.requestAnimationFrame(scanLoop);
            } catch (error) {
                setStatus("Camera access was blocked or is unavailable on this device. Please allow camera access, then try again.", true);
            }
        }

        modalElement.addEventListener("shown.bs.modal", startScanner);
        modalElement.addEventListener("hidden.bs.modal", function () {
            stopScanner();
            setStatus("Camera scanner is preparing. Hold the code steady inside the highlighted frame.", false);
        });
    });
</script>
</body>
</html>
