package com.example.karafusers.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads org.apache.karaf.jaas.cfg (.cfg / properties-like format).
 */
public final class JaasCfgLoader {

    private JaasCfgLoader() {}

    public static JaasCfg load(Path jaasCfgPath) throws IOException {
        if (jaasCfgPath == null) {
            throw new IllegalArgumentException("jaasCfgPath cannot be null");
        }
        if (!Files.exists(jaasCfgPath)) {
            throw new IllegalStateException("jaas.cfg not found: " + jaasCfgPath);
        }

        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(jaasCfgPath, StandardCharsets.UTF_8)) {
            p.load(r);
        }

        return new JaasCfg(p);
    }
}