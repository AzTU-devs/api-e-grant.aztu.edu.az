package az.aztu.egrant.shared.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Local-directory {@link FileStorage} for dev/single-node deployments. Swap for an
 * object-storage implementation in prod by providing another {@code FileStorage} bean.
 */
@Component
public class LocalFileStorage implements FileStorage {

    private final Path baseDir;

    public LocalFileStorage(StorageProperties properties) {
        this.baseDir = Path.of(properties.local().baseDir()).toAbsolutePath().normalize();
    }

    @Override
    public String store(String keyPrefix, String filename, String contentType, byte[] content) {
        try {
            String ext = extensionOf(filename);
            String key = keyPrefix + "/" + UUID.randomUUID() + ext;
            Path target = resolve(key);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return key;
        } catch (IOException ex) {
            throw new StorageException("Failed to store file", ex);
        }
    }

    @Override
    public Optional<StoredFile> load(String key) {
        Path path = resolve(key);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            String contentType = Optional.ofNullable(Files.probeContentType(path))
                    .orElse("application/octet-stream");
            long size = Files.size(path);
            InputStream in = Files.newInputStream(path);
            return Optional.of(new StoredFile(key, contentType, size, in));
        } catch (IOException ex) {
            throw new StorageException("Failed to load file: " + key, ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ex) {
            throw new StorageException("Failed to delete file: " + key, ex);
        }
    }

    private Path resolve(String key) {
        Path resolved = baseDir.resolve(key).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new StorageException("Path traversal blocked for key: " + key, null);
        }
        return resolved;
    }

    private static String extensionOf(String filename) {
        String ext = StringUtils.getFilenameExtension(filename);
        return ext == null ? "" : "." + ext;
    }

    /** Unchecked wrapper for storage I/O failures. */
    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
