# LU Librisync

LU Librisync is a `Spring Boot + JSP + MySQL` Library Management System starter for school or campus libraries. It is designed for a stack using:

- `JSP` for server-rendered UI
- `Spring Boot` for MVC, Security, and data access
- `MySQL Workbench` for database management and local schema setup

## Implemented Foundation

This repository now includes a working project foundation for:

- Login using Spring Security
- Student self-registration with generated student ID
- Admin dashboard with circulation counts and simple analytics
- Category and author management
- Book management
- Book issue and return flow
- Student search by student ID
- Student dashboard, profile update, and borrowing history
- Advanced catalog search by title or barcode keyword, category, author, availability, and ISBN
- Fine calculation for overdue records
- Digital-ready book fields such as barcode, QR issue code, and e-book path

## Current Scope

Implemented now:

- Core admin and student web flow
- Database-first JPA mapping based on the existing schema
- Legacy-friendly login that still accepts the plain-text demo passwords from `database/sample-data.sql`

Planned next:

- Password recovery flow
- Reservation queue system
- Email reminders before due date
- QR code image generation and scanning
- File upload for e-books
- PDF reader / digital library screen
- Deeper analytics charts

## Project Structure

- `src/main/java/com/lulibrisync/config`
  Security and authentication setup
- `src/main/java/com/lulibrisync/controller`
  MVC controllers for auth, admin, student, books, and circulation
- `src/main/java/com/lulibrisync/model`
  JPA entities and enums
- `src/main/java/com/lulibrisync/repository`
  Spring Data repositories
- `src/main/java/com/lulibrisync/service`
  Business logic such as registration, search, issue/return, and fines
- `src/main/resources/application.properties`
  Database and JSP configuration
- `src/main/resources/static/css/app.css`
  Shared UI styling
- `src/main/webapp/WEB-INF/jsp`
  JSP views
- `database/schema.sql`
  MySQL schema
- `database/sample-data.sql`
  Demo data

## Database Setup In MySQL Workbench

1. Open MySQL Workbench and connect to your local MySQL server.
2. Run `database/schema.sql`.
3. Run `database/sample-data.sql`.
4. Confirm that the database name is `lu_librisync`.

## Local Requirements

- Java 17
- Maven 3.9+
- MySQL 8

## Local Run

Recommended:

1. Run the local startup script:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-lu-librisync.ps1
```

2. Enter your MySQL password when prompted.

3. Open:

```text
http://localhost:8080
```

Manual alternative:

1. Set your database environment variables in PowerShell:

```powershell
$env:LU_LIBRISYNC_DB_URL="jdbc:mysql://127.0.0.1:3306/lu_librisync?useSSL=false&serverTimezone=Asia/Manila&allowPublicKeyRetrieval=true"
$env:LU_LIBRISYNC_DB_USERNAME="root"
$env:LU_LIBRISYNC_DB_PASSWORD="your_mysql_password"
```

2. Start the app:

```powershell
.\tools\apache-maven-3.9.14\bin\mvn.cmd -Dmaven.repo.local=.\.m2\repository spring-boot:run
```

3. Open:

```text
http://localhost:8080
```

## Demo Accounts

- Admin: `admin@lulibrisync.edu` / `Admin1234`
- Student: `maria.santos@student.edu` / `Student1234`
- Student: `john.cruz@student.edu` / `Student1234`

## Notes

- New registrations are stored using BCrypt-ready password hashing.
- Existing sample users can still log in because the app accepts legacy plain-text seed passwords for migration compatibility.
- A local Maven runtime is included under `tools/apache-maven-3.9.14` for this workspace setup.
