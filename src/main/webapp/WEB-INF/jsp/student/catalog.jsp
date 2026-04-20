<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Library Catalog</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
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
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/profile">Profile</a>
            <a class="nav-pill" href="${pageContext.request.contextPath}/student/history">Borrowing history</a>
            <form method="post" action="${pageContext.request.contextPath}/logout">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button class="nav-pill warm border-0" type="submit">Logout</button>
            </form>
        </div>
    </div>

    <section class="panel-card mb-4">
        <form method="get" action="${pageContext.request.contextPath}/student/catalog" class="row g-3">
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
            <div class="col-12">
                <button class="btn btn-brand" type="submit">Search catalog</button>
            </div>
        </form>
    </section>

    <section class="catalog-grid">
        <c:forEach items="${books}" var="book">
            <article class="catalog-card">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <h5 class="mb-0">${book.title}</h5>
                    <c:choose>
                        <c:when test="${book.available}">
                            <span class="tag-chip">Available</span>
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
                <p class="mb-3"><strong>Digital:</strong> <c:choose><c:when test="${book.digital}">Yes</c:when><c:otherwise>No</c:otherwise></c:choose></p>
                <p class="muted-text mb-0">${book.description}</p>
            </article>
        </c:forEach>
    </section>

    <c:if test="${empty books}">
        <section class="panel-card mt-4">
            <p class="mb-0 muted-text">No books matched your current filters.</p>
        </section>
    </c:if>
</div>
</body>
</html>
