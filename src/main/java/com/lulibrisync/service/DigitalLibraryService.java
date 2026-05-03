package com.lulibrisync.service;

import com.lulibrisync.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@SuppressWarnings("null")
public class DigitalLibraryService {

    private final Path storageRoot;
    private final Path ebooksRoot;
    private final Path bookCoversRoot;

    public DigitalLibraryService(@Value("${lulibrisync.storage.root:${user.dir}/storage}") String storageRootPath) {
        this.storageRoot = Path.of(storageRootPath).toAbsolutePath().normalize();
        this.ebooksRoot = this.storageRoot.resolve("ebooks").normalize();
        this.bookCoversRoot = this.storageRoot.resolve("book-covers").normalize();
        try {
            Files.createDirectories(this.ebooksRoot);
            Files.createDirectories(this.bookCoversRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize digital library storage.", exception);
        }
    }

    public String storeEbookFile(String bookTitle, MultipartFile ebookFile) {
        if (ebookFile == null || ebookFile.isEmpty()) {
            return null;
        }

        String originalFilename = ebookFile.getOriginalFilename();
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        if (fileExtension == null || !"pdf".equalsIgnoreCase(fileExtension)) {
            throw new IllegalArgumentException("Only PDF files are allowed for e-book upload.");
        }

        String safeFileName = slugify(bookTitle) + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".pdf";
        Path targetPath = ebooksRoot.resolve(safeFileName).normalize();
        if (!targetPath.startsWith(ebooksRoot)) {
            throw new IllegalArgumentException("Invalid e-book upload path.");
        }

        try {
            Files.copy(ebookFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to store the uploaded PDF file.");
        }

        return "ebooks/" + safeFileName;
    }

    public String storeBookCoverFile(String bookTitle, MultipartFile coverImageFile) {
        if (coverImageFile == null || coverImageFile.isEmpty()) {
            return null;
        }

        String originalFilename = coverImageFile.getOriginalFilename();
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        if (!isSupportedCoverExtension(fileExtension)) {
            throw new IllegalArgumentException("Only JPG, PNG, or WEBP files are allowed for book cover upload.");
        }

        String normalizedFileExtension = fileExtension.toLowerCase(Locale.ROOT);
        String safeFileName = slugify(bookTitle) + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "." + normalizedFileExtension;
        Path targetPath = bookCoversRoot.resolve(safeFileName).normalize();
        if (!targetPath.startsWith(bookCoversRoot)) {
            throw new IllegalArgumentException("Invalid book cover upload path.");
        }

        try {
            Files.copy(coverImageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to store the uploaded book cover image.");
        }

        return "book-covers/" + safeFileName;
    }

    public Resource getEbookResource(Book book) {
        if (book == null || !StringUtils.hasText(book.getEbookPath())) {
            throw new IllegalArgumentException("No e-book file is linked to this title.");
        }

        Path resolvedPath = resolveBookPath(book.getEbookPath());
        if (!Files.exists(resolvedPath) || !Files.isRegularFile(resolvedPath)) {
            throw new IllegalArgumentException("The linked e-book file could not be found.");
        }

        return new PathResource(resolvedPath);
    }

    public Resource getBookCoverResource(Book book) {
        if (book == null || !StringUtils.hasText(book.getCoverImage())) {
            throw new IllegalArgumentException("No book cover is linked to this title.");
        }

        Path resolvedPath = resolveBookPath(book.getCoverImage());
        if (!Files.exists(resolvedPath) || !Files.isRegularFile(resolvedPath)) {
            throw new IllegalArgumentException("The linked book cover image could not be found.");
        }

        return new PathResource(resolvedPath);
    }

    public boolean hasReadableEbook(Book book) {
        if (book == null || !StringUtils.hasText(book.getEbookPath())) {
            return false;
        }

        Path resolvedPath = resolveBookPath(book.getEbookPath());
        return Files.exists(resolvedPath) && Files.isRegularFile(resolvedPath);
    }

    public boolean hasReadableBookCover(Book book) {
        if (book == null || !StringUtils.hasText(book.getCoverImage())) {
            return false;
        }

        Path resolvedPath = resolveBookPath(book.getCoverImage());
        return Files.exists(resolvedPath) && Files.isRegularFile(resolvedPath);
    }

    public void deleteManagedEbook(String ebookPath) {
        if (!StringUtils.hasText(ebookPath) || !isManagedEbookPath(ebookPath)) {
            return;
        }

        Path resolvedPath = resolveManagedPath(ebookPath);
        try {
            Files.deleteIfExists(resolvedPath);
        } catch (IOException ignored) {
            // Keep delete cleanup best-effort so catalog operations do not fail.
        }
    }

    public boolean isManagedEbookPath(String ebookPath) {
        return StringUtils.hasText(ebookPath)
                && ebookPath.replace('\\', '/').startsWith("ebooks/");
    }

    public void deleteManagedBookCover(String coverImagePath) {
        if (!StringUtils.hasText(coverImagePath) || !isManagedBookCoverPath(coverImagePath)) {
            return;
        }

        Path resolvedPath = resolveManagedPath(coverImagePath);
        try {
            Files.deleteIfExists(resolvedPath);
        } catch (IOException ignored) {
            // Keep delete cleanup best-effort so catalog operations do not fail.
        }
    }

    public boolean isManagedBookCoverPath(String coverImagePath) {
        return StringUtils.hasText(coverImagePath)
                && coverImagePath.replace('\\', '/').startsWith("book-covers/");
    }

    private Path resolveManagedPath(String relativeStoragePath) {
        Path resolvedPath = storageRoot.resolve(relativeStoragePath).toAbsolutePath().normalize();
        if (!resolvedPath.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid managed storage path.");
        }
        return resolvedPath;
    }

    private Path resolveBookPath(String storedPath) {
        if (isManagedEbookPath(storedPath) || isManagedBookCoverPath(storedPath)) {
            return resolveManagedPath(storedPath);
        }

        Path directPath = Path.of(storedPath);
        if (!directPath.isAbsolute()) {
            directPath = Path.of(System.getProperty("user.dir")).resolve(directPath);
        }
        return directPath.toAbsolutePath().normalize();
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value == null ? "ebook" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "ebook" : normalized;
    }

    private boolean isSupportedCoverExtension(String extension) {
        if (extension == null) {
            return false;
        }
        return "jpg".equalsIgnoreCase(extension)
                || "jpeg".equalsIgnoreCase(extension)
                || "png".equalsIgnoreCase(extension)
                || "webp".equalsIgnoreCase(extension);
    }
}
