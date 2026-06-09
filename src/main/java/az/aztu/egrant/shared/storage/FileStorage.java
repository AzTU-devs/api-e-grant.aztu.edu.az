package az.aztu.egrant.shared.storage;

import java.io.InputStream;
import java.util.Optional;

/**
 * Pluggable file storage. Avatars and similar binaries are kept here and referenced by a
 * key (stored e.g. in {@code users.avatar_url}) — never as DB blobs.
 */
public interface FileStorage {

    /**
     * Stores the given content and returns the storage key/reference.
     *
     * @param keyPrefix  logical folder/prefix (e.g. {@code "avatars"})
     * @param filename   original filename (used to derive an extension)
     * @param contentType MIME type
     * @param content    the bytes to store
     * @return the storage key (to persist and later resolve)
     */
    String store(String keyPrefix, String filename, String contentType, byte[] content);

    /** Resolves a previously stored object. */
    Optional<StoredFile> load(String key);

    /** Removes a stored object if present. */
    void delete(String key);

    /** A stored object's content stream plus metadata. */
    record StoredFile(String key, String contentType, long size, InputStream content) {
    }
}
