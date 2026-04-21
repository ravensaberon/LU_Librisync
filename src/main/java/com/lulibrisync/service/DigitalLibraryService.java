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
public class DigitalLibraryService {

    private final Path storageRoot;
    private final Path ebooksRoot;

    public DigitalLibraryService(@Value("${lulibrisync.storage.root:${user.dir}/storage}") String storageRootPath) {
        this.storageRoot = Path.of(storageRootPath).toAbsolutePath().normalize();
        this.ebooksRoot = this.storageRoot.resolve("ebooks").normalize();
        try {
            Files.createDirectories(this.ebooksRoot);
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

    public boolean hasReadableEbook(Book book) {
        if (book == null || !StringUtils.hasText(book.getEbookPath())) {
            return false;
        }

        Path resolvedPath = resolveBookPath(book.getEbookPath());
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

    private Path resolveManagedPath(String ebookPath) {
        Path resolvedPath = storageRoot.resolve(ebookPath).toAbsolutePath().normalize();
        if (!resolvedPath.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid e-book path.");
        }
        return resolvedPath;
    }

    private Path resolveBookPath(String ebookPath) {
        if (isManagedEbookPath(ebookPath)) {
            return resolveManagedPath(ebookPath);
        }

        Path directPath = Path.of(ebookPath);
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
}
