package com.lulibrisync.controller;

import com.lulibrisync.service.BookService;
import com.lulibrisync.service.AuditLogService;
import com.lulibrisync.service.DigitalLibraryService;
import com.lulibrisync.service.ReservationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class BookController {

    private final BookService bookService;
    private final ReservationService reservationService;
    private final DigitalLibraryService digitalLibraryService;
    private final AuditLogService auditLogService;

    public BookController(BookService bookService,
                          ReservationService reservationService,
                          DigitalLibraryService digitalLibraryService,
                          AuditLogService auditLogService) {
        this.bookService = bookService;
        this.reservationService = reservationService;
        this.digitalLibraryService = digitalLibraryService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/admin/books")
    public String adminBooks(@RequestParam(required = false) Long editId, Model model) {
        populateBookManagementModel(model);
        var books = bookService.getAllBooks();
        model.addAttribute("bookCount", books.size());
        model.addAttribute("digitalBookCount", books.stream().filter(book -> book.isDigital()).count());
        model.addAttribute("availableBookCount", books.stream().mapToInt(book -> book.getAvailableQuantity() == null ? 0 : book.getAvailableQuantity()).sum());
        model.addAttribute("totalCopyCount", books.stream().mapToInt(book -> book.getQuantity() == null ? 0 : book.getQuantity()).sum());
        if (editId != null) {
            model.addAttribute("editBook", bookService.getBookById(editId));
        }
        return "books/manage";
    }

    @PostMapping("/admin/books")
    public String createBook(@RequestParam String title,
                             @RequestParam String isbn,
                             @RequestParam(required = false) String barcode,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) Long authorId,
                             @RequestParam(required = false) Integer publicationYear,
                             @RequestParam(required = false) Integer quantity,
                             @RequestParam(required = false) String shelfLocation,
                             @RequestParam(required = false) String description,
                             @RequestParam(defaultValue = "false") boolean digital,
                             @RequestParam(required = false) String ebookPath,
                             @RequestParam(required = false) MultipartFile ebookFile,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        String storedEbookPath = null;
        boolean hasManualEbookPath = StringUtils.hasText(ebookPath);
        try {
            storedEbookPath = digitalLibraryService.storeEbookFile(title, ebookFile);
            var book = bookService.createBook(title, isbn, barcode, categoryId, authorId, publicationYear, quantity, shelfLocation, description, digital || storedEbookPath != null || hasManualEbookPath, storedEbookPath != null ? storedEbookPath : ebookPath);
            auditLogService.log(
                    authentication.getName(),
                    "BOOK_CREATED",
                    "BOOK",
                    book.getId().toString(),
                    "Book created",
                    "Title: " + book.getTitle() + " | ISBN: " + book.getIsbn()
            );
            redirectAttributes.addFlashAttribute("success", "Book added to library successfully.");
        } catch (IllegalArgumentException exception) {
            digitalLibraryService.deleteManagedEbook(storedEbookPath);
            redirectAttributes.addFlashAttribute("openBookModal", true);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/admin/books/{bookId}/update")
    public String updateBook(@RequestParam String title,
                             @RequestParam String isbn,
                             @RequestParam(required = false) String barcode,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) Long authorId,
                             @RequestParam(required = false) Integer publicationYear,
                             @RequestParam(required = false) Integer quantity,
                             @RequestParam(required = false) String shelfLocation,
                             @RequestParam(required = false) String description,
                             @RequestParam(defaultValue = "false") boolean digital,
                             @RequestParam(required = false) String ebookPath,
                             @RequestParam(required = false) MultipartFile ebookFile,
                             @org.springframework.web.bind.annotation.PathVariable Long bookId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        String storedEbookPath = null;
        String previousManagedEbookPath = null;
        boolean hasManualEbookPath = StringUtils.hasText(ebookPath);
        try {
            var existingBook = bookService.getBookById(bookId);
            previousManagedEbookPath = existingBook.getEbookPath();
            storedEbookPath = digitalLibraryService.storeEbookFile(title, ebookFile);
            var updatedBook = bookService.updateBook(bookId, title, isbn, barcode, categoryId, authorId, publicationYear, quantity, shelfLocation, description, digital || storedEbookPath != null || hasManualEbookPath, storedEbookPath != null ? storedEbookPath : ebookPath);
            if (storedEbookPath != null
                    && digitalLibraryService.isManagedEbookPath(previousManagedEbookPath)
                    && (previousManagedEbookPath == null || !previousManagedEbookPath.equals(updatedBook.getEbookPath()))) {
                digitalLibraryService.deleteManagedEbook(previousManagedEbookPath);
            }
            reservationService.promoteReservationsForBook(updatedBook.getId());
            auditLogService.log(
                    authentication.getName(),
                    "BOOK_UPDATED",
                    "BOOK",
                    updatedBook.getId().toString(),
                    "Book updated",
                    "Title: " + updatedBook.getTitle() + " | Available: " + updatedBook.getAvailableQuantity() + "/" + updatedBook.getQuantity()
            );
            redirectAttributes.addFlashAttribute("success", "Book updated successfully.");
            return "redirect:/admin/books";
        } catch (IllegalArgumentException exception) {
            digitalLibraryService.deleteManagedEbook(storedEbookPath);
            redirectAttributes.addFlashAttribute("openBookModal", true);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/books?editId=" + bookId;
        }
    }

    @PostMapping("/admin/books/{bookId}/delete")
    public String deleteBook(@org.springframework.web.bind.annotation.PathVariable Long bookId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            var book = bookService.getBookById(bookId);
            bookService.deleteBook(bookId);
            digitalLibraryService.deleteManagedEbook(book.getEbookPath());
            auditLogService.log(
                    authentication.getName(),
                    "BOOK_DELETED",
                    "BOOK",
                    bookId.toString(),
                    "Book deleted",
                    "Title: " + book.getTitle() + " | ISBN: " + book.getIsbn()
            );
            redirectAttributes.addFlashAttribute("success", "Book deleted successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/books";
    }

    @GetMapping("/student/catalog")
    public String studentCatalog(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) Long authorId,
                                 @RequestParam(required = false) String isbn,
                                 @RequestParam(defaultValue = "false") boolean availableOnly,
                                 Authentication authentication,
                                 Model model) {
        model.addAttribute("books", bookService.searchBooks(keyword, categoryId, authorId, isbn, availableOnly));
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("authors", bookService.getAllAuthors());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedAuthorId", authorId);
        model.addAttribute("isbnValue", isbn);
        model.addAttribute("availableOnly", availableOnly);
        model.addAttribute("studentReservationStatusByBookId", reservationService.getReservationStatusesForStudentBooks(authentication.getName()));
        model.addAttribute("reservationQueueSizes", reservationService.getActiveQueueSizesByBook());
        return "student/catalog";
    }

    private void populateBookManagementModel(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("authors", bookService.getAllAuthors());
    }
}
