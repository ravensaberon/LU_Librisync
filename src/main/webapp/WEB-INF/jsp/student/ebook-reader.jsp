<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>E-Book Reader</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="page-shell">
    <div class="app-nav">
        <div>
            <span class="tag-chip">Digital Library</span>
            <div class="brand-title mt-2">Read e-book</div>
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

    <section class="hero-card mb-4">
        <h1 class="fw-bold mb-2">${book.title}</h1>
        <p class="muted-text mb-0">ISBN: ${book.isbn} | Author: ${book.author.name} | Category: ${book.category.name}</p>
    </section>

    <section class="panel-card">
        <div class="section-title">Inline PDF reader</div>
        <iframe
                src="${pageContext.request.contextPath}/student/ebooks/${book.id}/content"
                title="E-book reader for ${book.title}"
                style="width: 100%; min-height: 78vh; border: 1px solid var(--line); border-radius: 18px; background: #fff;">
        </iframe>
    </section>
</div>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
