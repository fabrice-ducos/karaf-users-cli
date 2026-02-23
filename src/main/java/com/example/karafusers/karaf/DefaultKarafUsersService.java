package com.example.karafusers.karaf;

//import com.example.karafusers.cli.ExitCodes;
import com.example.karafusers.cli.RootCommand;
import com.example.karafusers.config.PathResolver;
import com.example.karafusers.io.FileSecurity;
import com.example.karafusers.io.SecureFileUpdate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class DefaultKarafUsersService implements KarafUsersService {

    private final RootCommand root;
    private final Path usersFile;
    private final Path jaasCfg;

    public DefaultKarafUsersService(RootCommand root) {
        this.root = root;
        this.usersFile = PathResolver.resolveUsersFile(root.getUsersFile());
        this.jaasCfg = PathResolver.resolveJaasCfg(root.getJaasCfg());

        PathResolver.assertExists(usersFile, "users.properties");
        PathResolver.assertExists(jaasCfg, "org.apache.karaf.jaas.cfg");

        enforceSecurity();
    }

    private void enforceSecurity() {
        try {
            FileSecurity.assertSecureUsersFile(usersFile);
        } catch (IOException e) {
            throw new RuntimeException(
                    "I/O error while validating users.properties security: " + e.getMessage(), e);
        }
    }

    private void secureBackupIfNeeded() {
        if (!root.isBackupEnabled()) {
            return;
        }

        try {
            Path backup = SecureFileUpdate.createSecureBackup(usersFile);
            if (root.isVerbose()) {
                System.out.println("Backup created: " + backup);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create secure backup: " + e.getMessage(), e);
        }
    }

    @Override
    public void addUser(String username,
                        String rawPassword,
                        Set<String> roles,
                        Set<String> groups) {

        secureBackupIfNeeded();

        // TODO:
        // 1. Load jaas.cfg and build password encoder
        // 2. Encrypt rawPassword
        // 3. Use PropertiesBackingEngine to add user
        // 4. Atomic write via SecureFileUpdate

        throw new UnsupportedOperationException("User creation not implemented yet.");
    }

    @Override
    public void deleteUser(String username) {
        secureBackupIfNeeded();
        throw new UnsupportedOperationException("User deletion not implemented yet.");
    }

    @Override
    public void listUsers(boolean resolveGroups) {
        throw new UnsupportedOperationException("User listing not implemented yet.");
    }

    @Override
    public void editUser(String username,
                         Set<String> addRoles,
                         Set<String> removeRoles,
                         Set<String> addGroups,
                         Set<String> removeGroups,
                         String newPassword) {

        secureBackupIfNeeded();
        throw new UnsupportedOperationException("User edit not implemented yet.");
    }
}