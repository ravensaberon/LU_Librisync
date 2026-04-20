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

    <c:set var="bookFormAction" value="${pageContext.request.contextPath}/admin/books"/>
    <c:if test="${not empty editBook}">
        <c:set var="bookFormAction" value="${pageContext.request.contextPath}/admin/books/${editBook.id}/update"/>
    </c:if>

    <section class="panel-card mb-4">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
            <div>
                <div class="section-title mb-2">
                    <c:choose>
                        <c:when test="${not empty editBook}">Edit book record</c:when>
                        <c:otherwise>Add book to the library</c:otherwise>
                    </c:choose>
                </div>
                <p class="helper-copy">
                    Create new catalog entries, update bibliographic details, adjust inventory totals, and maintain digital resource information.
                </p>
            </div>
            <c:if test="${not empty editBook}">
                <a class="action-link" href="${pageContext.request.contextPath}/admin/books">Cancel editing</a>
            </c:if>
        </div>

        <form method="post" action="${bookFormAction}" class="row g-3">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

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
            <div class="col-md-5">
                <label class="form-label" for="ebookPath">E-book path</label>
                <input class="form-control" id="ebookPath" name="ebookPath" value="${editBook.ebookPath}" placeholder="Optional PDF or file path">
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
            <div class="col-12 d-flex flex-wrap gap-2">
                <button class="btn btn-brand" type="submit">
                    <i class="bi bi-journal-plus me-2"></i>
                    <c:choose>
                        <c:when test="${not empty editBook}">Update book</c:when>
                        <c:otherwise>Save book</c:otherwise>
                    </c:choose>
                </button>
                <c:if test="${not empty editBook}">
                    <a class="btn btn-warm" href="${pageContext.request.contextPath}/admin/books">Back to add mode</a>
                </c:if>
            </div>
        </form>
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
                            <strong>${book.title}</strong>
                            <c:if test="${not empty book.publicationYear}">
                                <div class="muted-text">${book.publicationYear}</div>
                            </c:if>
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
                            <a class="icon-action" href="${pageContext.request.contextPath}/admin/books?editId=${book.id}" title="Edit book">
                                <i class="bi bi-pencil-square"></i>
                            </a>
                            <form method="post" action="${pageContext.request.contextPath}/admin/books/${book.id}/delete">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
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
    </section>
</div>
</body>
</html>
