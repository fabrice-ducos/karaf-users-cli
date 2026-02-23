package com.example.karafusers.io;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardCopyOption.*;

public final class SecureFileUpdate {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private SecureFileUpdate() {}

    public static Path createSecureBackup(Path usersFile) throws IOException {
        String suffix = ".bak-" + LocalDateTime.now().format(TS);
        Path backup = usersFile.resolveSibling(usersFile.getFileName() + suffix);

        // Create backup (copy) then enforce perms
        Files.copy(usersFile, backup, COPY_ATTRIBUTES);
        FileSecurity.enforce0600(backup);
        return backup;
    }

    /**
     * Writes updated content to a secure temp file, then atomically replaces usersFile.
     * You can adapt this to let your PropertiesBackingEngine write into the temp file.
     */
    public static void atomicReplace(Path usersFile, Path tmpFile) throws IOException {
        // Ensure tmp is secure before move (and after, just in case)
        FileSecurity.enforce0600(tmpFile);

        try {
            Files.move(tmpFile, usersFile, ATOMIC_MOVE, REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            // Fall back to non-atomic replace, but still safe permissions-wise
            Files.move(tmpFile, usersFile, REPLACE_EXISTING);
        }

        FileSecurity.enforce0600(usersFile);
    }

    /** Creates a secure temp file in the same directory (important for atomic move on most FS). */
    public static Path createSecureTempFileNear(Path usersFile) throws IOException {
        Path dir = usersFile.getParent();
        if (dir == null) dir = Path.of(".");
        return Files.createTempFile(dir, usersFile.getFileName().toString(), ".tmp", FileSecurity.attr0600());
    }
}
