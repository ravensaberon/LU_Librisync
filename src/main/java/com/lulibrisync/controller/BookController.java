package com.lulibrisync.controller;

import com.lulibrisync.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/admin/books")
    public String adminBooks(@RequestParam(required = false) Long editId, Model model) {
        populateBookManagementModel(model);
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
                             RedirectAttributes redirectAttributes) {
        try {
            bookService.createBook(title, isbn, barcode, categoryId, authorId, publicationYear, quantity, shelfLocation, description, digital, ebookPath);
            redirectAttributes.addFlashAttribute("success", "Book added to library successfully.");
        } catch (IllegalArgumentException exception) {
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
                             @org.springframework.web.bind.annotation.PathVariable Long bookId,
                             RedirectAttributes redirectAttributes) {
        try {
            bookService.updateBook(bookId, title, isbn, barcode, categoryId, authorId, publicationYear, quantity, shelfLocation, description, digital, ebookPath);
            redirectAttributes.addFlashAttribute("success", "Book updated successfully.");
            return "redirect:/admin/books";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/books?editId=" + bookId;
        }
    }

    @PostMapping("/admin/books/{bookId}/delete")
    public String deleteBook(@org.springframework.web.bind.annotation.PathVariable Long bookId,
                             RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(bookId);
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
                                 Model model) {
        model.addAttribute("books", bookService.searchBooks(keyword, categoryId, authorId, isbn, availableOnly));
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("authors", bookService.getAllAuthors());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedAuthorId", authorId);
        model.addAttribute("isbnValue", isbn);
        model.addAttribute("availableOnly", availableOnly);
        return "student/catalog";
    }

    private void populateBookManagementModel(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("authors", bookService.getAllAuthors());
    }
}
