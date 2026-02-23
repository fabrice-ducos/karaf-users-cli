package com.example.karafusers.io;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Set;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public final class FileSecurity {

    private static final Set<PosixFilePermission> OWNER_RW =
            PosixFilePermissions.fromString("rw-------");

    private FileSecurity() {}

    /** Throws a SecurityException with a clear message if the file is not owner-read/write only (0600). */
    public static void assertSecureUsersFile(Path usersFile) throws IOException {
        // Existence and type checks
        if (!Files.exists(usersFile, NOFOLLOW_LINKS)) {
            throw new SecurityException("users.properties not found: " + usersFile);
        }
        if (!Files.isRegularFile(usersFile, NOFOLLOW_LINKS)) {
            throw new SecurityException("users.properties must be a regular file (not a directory/symlink): " + usersFile);
        }
        if (Files.isSymbolicLink(usersFile)) {
            throw new SecurityException("Refusing to use symlink for users.properties: " + usersFile);
        }

        // POSIX support check
        FileStore store = Files.getFileStore(usersFile);
        if (!store.supportsFileAttributeView(PosixFileAttributeView.class)) {
            throw new SecurityException(
                    "File system does not support POSIX permissions for: " + usersFile +
                    ". Refusing to proceed because secure permissions cannot be validated/enforced."
            );
        }

        // Permissions check
        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(usersFile, NOFOLLOW_LINKS);
        if (!perms.equals(OWNER_RW)) {
            throw new SecurityException(
                    "Insecure permissions on users.properties: " + usersFile +
                    ". Expected rw------- (0600), actual " + PosixFilePermissions.toString(perms) +
                    ". Fix with: chmod 600 " + usersFile
            );
        }

        // Optional: ownership check (recommended)
        UserPrincipal owner = Files.getOwner(usersFile, NOFOLLOW_LINKS);
        String currentUser = System.getProperty("user.name");
        if (owner != null && owner.getName() != null && !owner.getName().contains(currentUser)) {
            // owner.getName() can be "user" or "DOMAIN\\user" depending on system
            throw new SecurityException(
                    "users.properties is not owned by the current user. Owner=" + owner.getName() +
                    ", currentUser=" + currentUser + ". Refusing to proceed."
            );
        }
    }

    /** Enforces 0600 on a file (POSIX). */
    public static void enforce0600(Path file) throws IOException {
        FileStore store = Files.getFileStore(file);
        if (!store.supportsFileAttributeView(PosixFileAttributeView.class)) {
            throw new SecurityException("Cannot enforce permissions (POSIX not supported) for: " + file);
        }
        Files.setPosixFilePermissions(file, OWNER_RW);
    }

    /** Returns a FileAttribute that creates a file as 0600 on POSIX systems. */
    public static FileAttribute<Set<PosixFilePermission>> attr0600() {
        return PosixFilePermissions.asFileAttribute(OWNER_RW);
    }
}