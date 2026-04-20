USE lu_librisync;

INSERT INTO users (name, email, password, role, student_id, status)
VALUES
    ('LU Admin', 'admin@lulibrisync.edu', 'Admin1234', 'ADMIN', NULL, 'ACTIVE'),
    ('Maria Santos', 'maria.santos@student.edu', 'Student1234', 'STUDENT', '241-0001', 'ACTIVE'),
    ('John Cruz', 'john.cruz@student.edu', 'Student1234', 'STUDENT', '231-0002', 'ACTIVE');

INSERT INTO students (user_id, student_id, course, year_level, phone, address)
SELECT id, student_id, 'BS Information Technology', '3rd Year', '09171234567', 'La Union'
FROM users WHERE student_id = '241-0001';

INSERT INTO students (user_id, student_id, course, year_level, phone, address)
SELECT id, student_id, 'BS Education', '2nd Year', '09179876543', 'San Fernando'
FROM users WHERE student_id = '231-0002';

INSERT INTO categories (name, description) VALUES
    ('Computer Science', 'Programming, systems, and software engineering'),
    ('Education', 'Teaching methods and curriculum'),
    ('Literature', 'Novels, poetry, and literary studies');

INSERT INTO authors (name, bio) VALUES
    ('Robert C. Martin', 'Software craftsman and author'),
    ('Paulo Coelho', 'Brazilian novelist'),
    ('John Dewey', 'American philosopher and educator');

INSERT INTO books (title, isbn, barcode, category_id, author_id, publication_year, quantity, available_quantity, shelf_location, description, is_digital)
VALUES
    ('Clean Code', '9780132350884', 'BC-9780132350884', 1, 1, 2008, 5, 3, 'A1-04', 'Guide to writing maintainable software.', TRUE),
    ('The Alchemist', '9780062315007', 'BC-9780062315007', 3, 2, 1993, 4, 2, 'B2-11', 'Inspirational fiction title.', TRUE),
    ('Democracy and Education', '9780684836317', 'BC-9780684836317', 2, 3, 1916, 2, 1, 'C3-09', 'Classic book on educational philosophy.', FALSE);

INSERT INTO issue_records (book_id, student_id, issued_by, qr_issue_code, issue_date, due_date, return_date, status, fine_amount, remarks)
SELECT
    b.id,
    s.id,
    a.id,
    'QR-ISSUE-0001',
    NOW() - INTERVAL 7 DAY,
    NOW() + INTERVAL 7 DAY,
    NULL,
    'ISSUED',
    0.00,
    'For software engineering class'
FROM books b
JOIN students s ON s.student_id = '241-0001'
JOIN users a ON a.email = 'admin@lulibrisync.edu'
WHERE b.isbn = '9780132350884';

INSERT INTO issue_records (book_id, student_id, issued_by, qr_issue_code, issue_date, due_date, return_date, status, fine_amount, remarks)
SELECT
    b.id,
    s.id,
    a.id,
    'QR-ISSUE-0002',
    NOW() - INTERVAL 20 DAY,
    NOW() - INTERVAL 6 DAY,
    NULL,
    'OVERDUE',
    60.00,
    'Pending return'
FROM books b
JOIN students s ON s.student_id = '231-0002'
JOIN users a ON a.email = 'admin@lulibrisync.edu'
WHERE b.isbn = '9780684836317';

INSERT INTO reservations (book_id, student_id, queue_position, status, reserved_at, expires_at)
SELECT b.id, s.id, 1, 'PENDING', NOW(), NOW() + INTERVAL 2 DAY
FROM books b
JOIN students s ON s.student_id = '231-0002'
WHERE b.isbn = '9780132350884';

INSERT INTO fines (issue_record_id, student_id, amount, status, calculated_at)
SELECT i.id, i.student_id, i.fine_amount, 'UNPAID', NOW()
FROM issue_records i
WHERE i.status = 'OVERDUE';
