package com.lulibrisync.controller;

import com.lulibrisync.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class ReferenceController {

    private final BookService bookService;

    public ReferenceController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/references")
    public String references(@RequestParam(required = false) Long editCategoryId,
                             @RequestParam(required = false) Long editAuthorId,
                             Model model) {
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("authors", bookService.getAllAuthors());
        if (editCategoryId != null) {
            model.addAttribute("editCategory", bookService.getCategoryById(editCategoryId));
        }
        if (editAuthorId != null) {
            model.addAttribute("editAuthor", bookService.getAuthorById(editAuthorId));
        }
        return "admin/references";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        try {
            bookService.createCategory(name, description);
            redirectAttributes.addFlashAttribute("success", "Category added successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/references";
    }

    @PostMapping("/categories/{categoryId}/update")
    public String updateCategory(@PathVariable Long categoryId,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        try {
            bookService.updateCategory(categoryId, name, description);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully.");
            return "redirect:/admin/references";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/references?editCategoryId=" + categoryId;
        }
    }

    @PostMapping("/categories/{categoryId}/delete")
    public String deleteCategory(@PathVariable Long categoryId,
                                 RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteCategory(categoryId);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/references";
    }

    @PostMapping("/authors")
    public String createAuthor(@RequestParam String name,
                               @RequestParam(required = false) String bio,
                               RedirectAttributes redirectAttributes) {
        try {
            bookService.createAuthor(name, bio);
            redirectAttributes.addFlashAttribute("success", "Author added successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/references";
    }

    @PostMapping("/authors/{authorId}/update")
    public String updateAuthor(@PathVariable Long authorId,
                               @RequestParam String name,
                               @RequestParam(required = false) String bio,
                               RedirectAttributes redirectAttributes) {
        try {
            bookService.updateAuthor(authorId, name, bio);
            redirectAttributes.addFlashAttribute("success", "Author updated successfully.");
            return "redirect:/admin/references";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/references?editAuthorId=" + authorId;
        }
    }

    @PostMapping("/authors/{authorId}/delete")
    public String deleteAuthor(@PathVariable Long authorId,
                               RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteAuthor(authorId);
            redirectAttributes.addFlashAttribute("success", "Author deleted successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/references";
    }
}
