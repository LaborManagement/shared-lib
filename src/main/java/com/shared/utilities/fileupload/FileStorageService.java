package com.shared.utilities.fileupload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.shared.utilities.logger.LoggerFactoryProvider;

@Component
public class FileStorageService {
    private static final Logger log = LoggerFactoryProvider.getLogger(FileStorageService.class);

    private final String baseUploadDir;

    public FileStorageService(String baseUploadDir) {
        this.baseUploadDir = baseUploadDir != null && !baseUploadDir.isBlank() ? baseUploadDir : "uploads";
    }

    public FileMetadata storeFile(MultipartFile file, String category, String fileName) throws IOException {
        String uploadDir = baseUploadDir + File.separator + category;
        Path destinationPath = Path.of(uploadDir, fileName).toAbsolutePath();
        Files.createDirectories(destinationPath.getParent());
        log.info("Storing file to: {}", destinationPath);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }

        try (InputStream inputStream = file.getInputStream();
                DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);
                OutputStream outputStream = new BufferedOutputStream(
                        Files.newOutputStream(destinationPath, StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING))) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = digestInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Files.deleteIfExists(destinationPath);
            throw e;
        }

        String fileHash = HexFormat.of().formatHex(digest.digest());
        log.debug("Calculated SHA-256 hash {} for uploaded file", fileHash);

        FileMetadata metadata = new FileMetadata();
        metadata.setFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : fileName);
        metadata.setStoredPath(destinationPath.toString());
        metadata.setFileHash(fileHash);
        metadata.setFileType(category);
        metadata.setUploadDate(java.time.LocalDateTime.now());
        return metadata;
    }

    public void deleteFileByPath(String path) throws IOException {
        if (path == null || path.isBlank())
            return;
        Files.deleteIfExists(Path.of(path));
    }

    String getBaseUploadDir() {
        return baseUploadDir;
    }
}
