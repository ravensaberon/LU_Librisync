package com.lulibrisync.service;

import com.lulibrisync.model.Author;
import com.lulibrisync.model.Book;
import com.lulibrisync.model.Category;
import com.lulibrisync.model.IssueStatus;
import com.lulibrisync.repository.AuthorRepository;
import com.lulibrisync.repository.BookRepository;
import com.lulibrisync.repository.CategoryRepository;
import com.lulibrisync.repository.IssueRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final IssueRecordRepository issueRecordRepository;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository,
                       AuthorRepository authorRepository,
                       IssueRecordRepository issueRecordRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.issueRecordRepository = issueRecordRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAllByOrderByTitleAsc();
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailableQuantityGreaterThanOrderByTitleAsc(0);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAllByOrderByNameAsc();
    }

    public List<Book> searchBooks(String keyword,
                                  Long categoryId,
                                  Long authorId,
                                  String isbn,
                                  boolean availableOnly) {
        return bookRepository.searchBooks(blankToNull(keyword), categoryId, authorId, blankToNull(isbn), availableOnly);
    }

    public Book getBookById(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book is required.");
        }
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
    }

    /**
     * Looks up a book by its registered barcode first, then falls back to ISBN.
     * Returns the matching book or empty if no exact match is found.
     */
    public java.util.Optional<Book> findBookByCode(String code) {
        if (code == null || code.isBlank()) {
            return java.util.Optional.empty();
        }
        String normalized = code.trim();
        var byBarcode = bookRepository.findByBarcodeIgnoreCase(normalized);
        if (byBarcode.isPresent()) {
            return byBarcode;
        }
        return bookRepository.findByIsbnIgnoreCase(normalized);
    }

    public Category getCategoryById(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category is required.");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
    }

    public Author getAuthorById(Long authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("Author is required.");
        }
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found."));
    }

    @Transactional
    public Book createBook(String title,
                           String isbn,
                           String barcode,
                           Long categoryId,
                           Long authorId,
                           Integer publicationYear,
                           Integer quantity,
                           String shelfLocation,
                           String description,
                           String coverImage,
                           boolean digital,
                           String ebookPath) {
        Book book = new Book();
        applyBookDetails(book, title, isbn, barcode, categoryId, authorId, publicationYear, quantity, shelfLocation, description, coverImage, digital, ebookPath, true);
        book.setAvailableQuantity(book.getQuantity());
        return bookRepository.save(book);
    }

    @Transactional
    public Category createCategory(String name, String description) {
        Category category = new Category();
        applyCategoryDetails(category, name, description, true);
        return categoryRepository.save(category);
    }

    @Transactional
    public Author createAuthor(String name, String bio) {
        Author author = new Author();
        applyAuthorDetails(author, name, bio, true);
        return authorRepository.save(author);
    }

    @Transactional
    public Book updateBook(Long bookId,
                           String title,
                           String isbn,
                           String barcode,
                           Long categoryId,
                           Long authorId,
                           Integer publicationYear,
                           Integer quantity,
                           String shelfLocation,
                           String description,
                           String coverImage,
                           boolean digital,
                           String ebookPath) {
        Book book = getBookById(bookId);
        applyBookDetails(book, title, isbn, barcode, categoryId, authorId, publicationYear, quantity, shelfLocation, description, coverImage, digital, ebookPath, false);
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long bookId) {
        Book book = getBookById(bookId);
        long activeIssues = issueRecordRepository.countByBook_IdAndStatusIn(bookId, List.of(IssueStatus.ISSUED, IssueStatus.OVERDUE));
        if (activeIssues > 0) {
            throw new IllegalArgumentException("Return or resolve all active issues before deleting this book.");
        }
        bookRepository.delete(book);
    }

    @Transactional
    public Book updateStoredEbookPath(Long bookId, String ebookPath, boolean digital) {
        Book book = getBookById(bookId);
        book.setEbookPath(blankToNull(ebookPath));
        if (digital || book.getEbookPath() != null) {
            book.setDigital(true);
        }
        return bookRepository.save(book);
    }

    @Transactional
    public Category updateCategory(Long categoryId, String name, String description) {
        Category category = getCategoryById(categoryId);
        applyCategoryDetails(category, name, description, false);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        if (bookRepository.countByCategory_Id(categoryId) > 0) {
            throw new IllegalArgumentException("Reassign books before deleting this category.");
        }
        categoryRepository.delete(getCategoryById(categoryId));
    }

    @Transactional
    public Author updateAuthor(Long authorId, String name, String bio) {
        Author author = getAuthorById(authorId);
        applyAuthorDetails(author, name, bio, false);
        return authorRepository.save(author);
    }

    @Transactional
    public void deleteAuthor(Long authorId) {
        if (bookRepository.countByAuthor_Id(authorId) > 0) {
            throw new IllegalArgumentException("Reassign books before deleting this author.");
        }
        authorRepository.delete(getAuthorById(authorId));
    }

    private void applyBookDetails(Book book,
                                  String title,
                                  String isbn,
                                  String barcode,
                                  Long categoryId,
                                  Long authorId,
                                  Integer publicationYear,
                                  Integer quantity,
                                  String shelfLocation,
                                  String description,
                                  String coverImage,
                                  boolean digital,
                                  String ebookPath,
                                  boolean creating) {
        String normalizedTitle = required(title, "Book title is required.");
        String normalizedIsbn = required(isbn, "ISBN is required.");
        String normalizedBarcode = blankToNull(barcode);

        if (creating) {
            if (bookRepository.existsByIsbnIgnoreCase(normalizedIsbn)) {
                throw new IllegalArgumentException("ISBN already exists.");
            }
            if (normalizedBarcode != null && bookRepository.existsByBarcodeIgnoreCase(normalizedBarcode)) {
                throw new IllegalArgumentException("Barcode already exists.");
            }
        } else {
            if (bookRepository.existsByIsbnIgnoreCaseAndIdNot(normalizedIsbn, book.getId())) {
                throw new IllegalArgumentException("ISBN already exists.");
            }
            if (normalizedBarcode != null && bookRepository.existsByBarcodeIgnoreCaseAndIdNot(normalizedBarcode, book.getId())) {
                throw new IllegalArgumentException("Barcode already exists.");
            }
        }

        int normalizedQuantity = quantity == null || quantity < 1 ? 1 : quantity;
        int currentQuantity = book.getQuantity() == null || book.getQuantity() < 1 ? 1 : book.getQuantity();
        int currentAvailable = book.getAvailableQuantity() == null ? currentQuantity : book.getAvailableQuantity();
        int borrowedCount = Math.max(0, currentQuantity - currentAvailable);
        if (!creating && normalizedQuantity < borrowedCount) {
            throw new IllegalArgumentException("Quantity cannot be less than the number of borrowed copies.");
        }

        book.setTitle(normalizedTitle);
        book.setIsbn(normalizedIsbn);
        book.setBarcode(normalizedBarcode);
        book.setPublicationYear(publicationYear);
        book.setQuantity(normalizedQuantity);
        book.setAvailableQuantity(creating ? normalizedQuantity : Math.max(0, normalizedQuantity - borrowedCount));
        book.setShelfLocation(blankToNull(shelfLocation));
        book.setDescription(blankToNull(description));
        book.setCoverImage(blankToNull(coverImage));
        book.setDigital(digital || blankToNull(ebookPath) != null);
        book.setEbookPath(blankToNull(ebookPath));

        book.setCategory(categoryId == null ? null : getCategoryById(categoryId));
        book.setAuthor(authorId == null ? null : getAuthorById(authorId));
    }

    private void applyCategoryDetails(Category category,
                                      String name,
                                      String description,
                                      boolean creating) {
        String normalizedName = required(name, "Category name is required.");
        if (creating) {
            if (categoryRepository.findByNameIgnoreCase(normalizedName).isPresent()) {
                throw new IllegalArgumentException("Category already exists.");
            }
        } else if (categoryRepository.findByNameIgnoreCaseAndIdNot(normalizedName, category.getId()).isPresent()) {
            throw new IllegalArgumentException("Category already exists.");
        }

        category.setName(normalizedName);
        category.setDescription(blankToNull(description));
    }

    private void applyAuthorDetails(Author author,
                                    String name,
                                    String bio,
                                    boolean creating) {
        String normalizedName = required(name, "Author name is required.");
        if (creating) {
            if (authorRepository.findByNameIgnoreCase(normalizedName).isPresent()) {
                throw new IllegalArgumentException("Author already exists.");
            }
        } else if (authorRepository.findByNameIgnoreCaseAndIdNot(normalizedName, author.getId()).isPresent()) {
            throw new IllegalArgumentException("Author already exists.");
        }

        author.setName(normalizedName);
        author.setBio(blankToNull(bio));
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
