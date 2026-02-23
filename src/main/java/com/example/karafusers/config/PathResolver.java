package com.example.karafusers.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathResolver {

    private static final String ENV_KARAF_ETC = "KARAF_ETC";
    private static final String ENV_KARAF_JAAS_CFG = "KARAF_JAAS_CFG";

    private PathResolver() {}

    public static Path resolveUsersFile(Path explicit) {
        if (explicit != null) {
            return explicit.toAbsolutePath().normalize();
        }

        String etc = System.getenv(ENV_KARAF_ETC);
        if (etc != null && !etc.isBlank()) {
            return Paths.get(etc, "users.properties").toAbsolutePath().normalize();
        }

        return Paths.get("etc", "users.properties").toAbsolutePath().normalize();
    }

    public static Path resolveJaasCfg(Path explicit) {
        if (explicit != null) {
            return explicit.toAbsolutePath().normalize();
        }

        String direct = System.getenv(ENV_KARAF_JAAS_CFG);
        if (direct != null && !direct.isBlank()) {
            return Paths.get(direct).toAbsolutePath().normalize();
        }

        String etc = System.getenv(ENV_KARAF_ETC);
        if (etc != null && !etc.isBlank()) {
            return Paths.get(etc, "org.apache.karaf.jaas.cfg")
                    .toAbsolutePath()
                    .normalize();
        }

        return Paths.get("etc", "org.apache.karaf.jaas.cfg")
                .toAbsolutePath()
                .normalize();
    }

    public static void assertExists(Path file, String logicalName) {
        if (!Files.exists(file)) {
            throw new IllegalStateException(
                    logicalName + " not found: " + file
            );
        }
    }
}