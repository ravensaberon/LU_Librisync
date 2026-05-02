<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Book Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Book Management</span>
            <div class="brand-title mt-2">Full catalog and inventory control</div>
        </div>
        <div class="nav-links">
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a class="nav-pill active" href="${pageContext.request.contextPath}/admin/books">Books</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/admin/issues">Issue / Return</a>
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

    <c:set var="bookFormAction" value="${pageContext.request.contextPath}/admin/books"/>
    <c:if test="${not empty editBook}">
        <c:set var="bookFormAction" value="${pageContext.request.contextPath}/admin/books/${editBook.id}/update"/>
    </c:if>

    <section class="hero-card mb-4">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
            <div>
                <span class="tag-chip">Catalog Workspace</span>
                <h1 class="fw-bold mt-3 mb-2">Manage your library collection</h1>
                <p class="muted-text mb-0">
                    Add new titles through a popup form, keep bibliographic details organized, and update inventory records without leaving the current page.
                </p>
            </div>
            <div class="d-flex flex-wrap gap-2">
                <button class="btn btn-light" type="button" data-bs-toggle="modal" data-bs-target="#bookFormModal">
                    <i class="bi bi-journal-plus me-2"></i>Add book to the library
                </button>
                <c:if test="${not empty editBook}">
                    <a class="action-link" href="${pageContext.request.contextPath}/admin/books?page=${booksPage.page}">Exit edit mode</a>
                </c:if>
            </div>
        </div>
        <c:if test="${not empty editBook}">
            <div class="mt-3">
                <span class="tag-chip">Editing now: ${editBook.title}</span>
            </div>
        </c:if>
    </section>

    <section class="stat-grid mb-4">
        <div class="metric-card">
            <div class="metric-value">${bookCount}</div>
            <div class="metric-label">Catalog titles</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${totalCopyCount}</div>
            <div class="metric-label">Total copies</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${availableBookCount}</div>
            <div class="metric-label">Available copies</div>
        </div>
        <div class="metric-card">
            <div class="metric-value">${digitalBookCount}</div>
            <div class="metric-label">Digital-ready titles</div>
        </div>
    </section>

    <section class="panel-card">
        <div class="section-title">Current inventory</div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Title</th>
                    <th>ISBN / Barcode</th>
                    <th>Category</th>
                    <th>Author</th>
                    <th>Inventory</th>
                    <th>Location</th>
                    <th>Digital</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${books}" var="book">
                    <tr>
                        <td>
                            <div class="book-title-cell">
                                <div class="book-title-cover">
                                    <c:choose>
                                        <c:when test="${readableBookCoverByBookId[book.id]}">
                                            <img src="${pageContext.request.contextPath}/books/${book.id}/cover" alt="${book.title} cover">
                                        </c:when>
                                        <c:otherwise>
                                            <i class="bi bi-book-half"></i>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div>
                                    <strong>${book.title}</strong>
                                    <c:if test="${not empty book.publicationYear}">
                                        <div class="muted-text">${book.publicationYear}</div>
                                    </c:if>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div>${book.isbn}</div>
                            <div class="muted-text">${book.barcode}</div>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty book.category}">${book.category.name}</c:when>
                                <c:otherwise><span class="muted-text">Unassigned</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty book.author}">${book.author.name}</c:when>
                                <c:otherwise><span class="muted-text">Unassigned</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>${book.availableQuantity} / ${book.quantity}</td>
                        <td>${book.shelfLocation}</td>
                        <td>
                            <c:choose>
                                <c:when test="${book.digital}">
                                    <span class="tag-chip">Yes</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="tag-chip warn">No</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="table-actions">
                            <button class="icon-action" type="button" title="View book QR code" data-bs-toggle="modal" data-bs-target="#bookQrModal" data-book-title="${book.title}" data-book-isbn="${book.isbn}" data-book-code="${book.scanCode}" data-book-code-label="${book.scanCodeLabel}">
                                <i class="bi bi-qr-code"></i>
                            </button>
                            <a class="icon-action" href="${pageContext.request.contextPath}/admin/books?editId=${book.id}&page=${booksPage.page}" title="Edit book">
                                <i class="bi bi-pencil-square"></i>
                            </a>
                            <form method="post" action="${pageContext.request.contextPath}/admin/books/${book.id}/delete">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <input type="hidden" name="page" value="${booksPage.page}">
                                <button class="icon-action danger" type="submit" title="Delete book">
                                    <i class="bi bi-trash3"></i>
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty books}">
                    <tr>
                        <td colspan="8" class="text-center muted-text">No books available yet.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
        <c:if test="${booksPage.totalPages > 1}">
            <nav class="mt-4" aria-label="Book inventory pages">
                <ul class="pagination justify-content-center mb-0">
                    <li class="page-item <c:if test='${!booksPage.hasPrevious}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/books?page=${booksPage.previousPage}<c:if test='${not empty editBook}'>&editId=${editBook.id}</c:if>">Previous</a>
                    </li>
                    <c:forEach begin="${booksPage.startPage}" end="${booksPage.endPage}" var="pageNumber">
                        <li class="page-item <c:if test='${pageNumber == booksPage.page}'>active</c:if>">
                            <a class="page-link" href="${pageContext.request.contextPath}/admin/books?page=${pageNumber}<c:if test='${not empty editBook}'>&editId=${editBook.id}</c:if>">${pageNumber}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item <c:if test='${!booksPage.hasNext}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/admin/books?page=${booksPage.nextPage}<c:if test='${not empty editBook}'>&editId=${editBook.id}</c:if>">Next</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </section>

    <div class="modal fade" id="bookQrModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow-lg">
                <div class="modal-header modal-header-brand">
                    <div>
                        <span class="modal-kicker">Book QR Label</span>
                        <h2 class="h4 mb-1 mt-2">Scan-ready catalog code</h2>
                        <p class="modal-subtitle mb-0">Each title now has a QR label that resolves to its saved barcode or ISBN for fast lookup and desk-side scanning.</p>
                    </div>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="qr-card">
                        <div class="qr-code-shell mb-3" id="bookQrCanvas"></div>
                        <div class="qr-code-meta">
                            <div>
                                <span class="info-tile-label">Encoded value</span>
                                <span class="qr-code-value" id="bookQrValue">No code selected yet.</span>
                            </div>
                            <div class="info-grid">
                                <div class="info-tile">
                                    <span class="info-tile-label">Title</span>
                                    <span class="info-tile-value" id="bookQrTitle">Not selected</span>
                                </div>
                                <div class="info-tile">
                                    <span class="info-tile-label">ISBN</span>
                                    <span class="info-tile-value" id="bookQrIsbn">Not selected</span>
                                </div>
                                <div class="info-tile">
                                    <span class="info-tile-label">Code type</span>
                                    <span class="info-tile-value" id="bookQrType">Not selected</span>
                                </div>
                            </div>
                        </div>
                        <div class="d-flex flex-wrap gap-2 mt-3">
                            <button class="btn btn-brand" id="downloadBookQrButton" type="button" disabled>Download PNG</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="bookFormModal" tabindex="-1" aria-labelledby="bookFormModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
            <div class="modal-header modal-header-brand">
                <div>
                    <div class="modal-kicker">Book Management</div>
                    <h2 class="modal-title h4 mb-1" id="bookFormModalLabel">
                        <c:choose>
                            <c:when test="${not empty editBook}">Edit book record</c:when>
                            <c:otherwise>Add book to the library</c:otherwise>
                        </c:choose>
                    </h2>
                    <p class="modal-subtitle mb-0">Create new catalog entries, update bibliographic details, adjust inventory totals, and maintain digital resource information.</p>
                </div>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="post" action="${bookFormAction}" class="row g-3" enctype="multipart/form-data">
                <div class="modal-body">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <input type="hidden" name="page" value="${booksPage.page}">

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label" for="title">Title</label>
                            <input class="form-control" id="title" name="title" value="${editBook.title}" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label" for="isbn">ISBN</label>
                            <input class="form-control" id="isbn" name="isbn" value="${editBook.isbn}" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label" for="barcode">Barcode</label>
                            <input class="form-control" id="barcode" name="barcode" value="${editBook.barcode}">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="categoryId">Category</label>
                            <select class="form-select" id="categoryId" name="categoryId">
                                <option value="">Select category</option>
                                <c:forEach items="${categories}" var="category">
                                    <option value="${category.id}" <c:if test="${not empty editBook and not empty editBook.category and editBook.category.id == category.id}">selected</c:if>>
                                        ${category.name}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="authorId">Author</label>
                            <select class="form-select" id="authorId" name="authorId">
                                <option value="">Select author</option>
                                <c:forEach items="${authors}" var="author">
                                    <option value="${author.id}" <c:if test="${not empty editBook and not empty editBook.author and editBook.author.id == author.id}">selected</c:if>>
                                        ${author.name}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label" for="publicationYear">Year</label>
                            <input class="form-control" id="publicationYear" name="publicationYear" type="number" value="${editBook.publicationYear}">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label" for="quantity">Quantity</label>
                            <input class="form-control" id="quantity" name="quantity" type="number" min="1" value="${not empty editBook ? editBook.quantity : 1}">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="shelfLocation">Shelf location</label>
                            <input class="form-control" id="shelfLocation" name="shelfLocation" value="${editBook.shelfLocation}">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="coverImageFile">Book cover</label>
                            <input class="form-control" id="coverImageFile" name="coverImageFile" type="file" accept="image/png,image/jpeg,image/webp,.png,.jpg,.jpeg,.webp">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="ebookPath">E-book path</label>
                            <input class="form-control" id="ebookPath" name="ebookPath" value="${editBook.ebookPath}" placeholder="Optional PDF or file path">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="ebookFile">Upload PDF</label>
                            <input class="form-control" id="ebookFile" name="ebookFile" type="file" accept="application/pdf,.pdf">
                        </div>
                        <div class="col-12">
                            <small class="muted-text">Digital PDF stays readable in the student catalog even when all physical copies are occupied. Cover upload is optional but recommended.</small>
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <div class="form-check">
                                <input class="form-check-input" id="digital" name="digital" type="checkbox" value="true" <c:if test="${not empty editBook and editBook.digital}">checked</c:if>>
                                <label class="form-check-label" for="digital">Digital copy available</label>
                            </div>
                        </div>
                        <div class="col-12">
                            <label class="form-label" for="description">Description</label>
                            <textarea class="form-control" id="description" name="description" rows="3">${editBook.description}</textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <c:if test="${not empty editBook}">
                        <a class="btn btn-warm me-auto" href="${pageContext.request.contextPath}/admin/books?page=${booksPage.page}">Back to add mode</a>
                    </c:if>
                    <button class="btn btn-warm" type="button" data-bs-dismiss="modal">Close</button>
                    <button class="btn btn-brand" type="submit">
                        <i class="bi bi-journal-plus me-2"></i>
                        <c:choose>
                            <c:when test="${not empty editBook}">Update book</c:when>
                            <c:otherwise>Save book</c:otherwise>
                        </c:choose>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/vendor/qrious.min.js"></script>
<script src="${pageContext.request.contextPath}/js/qr-tools.js"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
<script>
    (function () {
        var shouldOpenBookModal = ${not empty editBook or openBookModal ? 'true' : 'false'};
        var bookFormModal = document.getElementById("bookFormModal");
        var qrModalElement = document.getElementById("bookQrModal");
        var qrCanvasElement = document.getElementById("bookQrCanvas");
        var qrValueElement = document.getElementById("bookQrValue");
        var qrTitleElement = document.getElementById("bookQrTitle");
        var qrIsbnElement = document.getElementById("bookQrIsbn");
        var qrTypeElement = document.getElementById("bookQrType");
        var downloadButton = document.getElementById("downloadBookQrButton");
        var currentQrCanvas = null;

        if (shouldOpenBookModal && bookFormModal) {
            bootstrap.Modal.getOrCreateInstance(bookFormModal).show();
        }

        qrModalElement.addEventListener("show.bs.modal", function (event) {
            var trigger = event.relatedTarget;
            var bookCode = trigger.getAttribute("data-book-code") || "";
            var bookTitle = trigger.getAttribute("data-book-title") || "Not selected";
            var bookIsbn = trigger.getAttribute("data-book-isbn") || "Not selected";
            var bookCodeLabel = trigger.getAttribute("data-book-code-label") || "Book code";

            qrValueElement.textContent = bookCode;
            qrTitleElement.textContent = bookTitle;
            qrIsbnElement.textContent = bookIsbn;
            qrTypeElement.textContent = bookCodeLabel;
            currentQrCanvas = window.LuLibrisyncQr.renderQr(qrCanvasElement, bookCode, {
                size: 240,
                emptyText: "No QR code available for this book.",
                errorText: "Unable to render this QR code."
            });
            downloadButton.disabled = !currentQrCanvas;
            downloadButton.dataset.filename = window.LuLibrisyncQr.normalizeFilename(bookTitle, "book") + "-qr.png";
        });

        downloadButton.addEventListener("click", function () {
            if (!currentQrCanvas) {
                return;
            }

            window.LuLibrisyncQr.downloadCanvas(currentQrCanvas, downloadButton.dataset.filename);
        });
    })();
</script>
</body>
</html>


