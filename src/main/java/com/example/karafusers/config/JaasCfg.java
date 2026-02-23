package com.example.karafusers.config;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Typed view over org.apache.karaf.jaas.cfg (OSGi .cfg format).
 *
 * This is intentionally minimal and focused on password encryption settings.
 */
public final class JaasCfg {

    private final Properties raw;

    public JaasCfg(Properties raw) {
        this.raw = Objects.requireNonNull(raw, "raw");
    }

    public Properties raw() {
        return raw;
    }

    public boolean encryptionEnabled() {
        return bool("encryption.enabled").orElse(true);
    }

    /**
     * Provider name: e.g. "basic", "jasypt", "spring-security-crypto".
     */
    public Optional<String> encryptionName() {
        return str("encryption.name").map(String::trim).filter(s -> !s.isEmpty());
    }

    /**
     * Algorithm (meaning depends on provider). For spring-security-crypto: "bcrypt", "pbkdf2", "scrypt", "argon2".
     */
    public Optional<String> encryptionAlgorithm() {
        return str("encryption.algorithm").map(String::trim).filter(s -> !s.isEmpty());
    }

    /**
     * Optional prefix/suffix used by Karaf to wrap the stored encoded password.
     * Example: prefix = "{CRYPT}" etc.
     */
    public String encryptionPrefix() {
        return str("encryption.prefix").orElse("");
    }

    public String encryptionSuffix() {
        return str("encryption.suffix").orElse("");
    }

    /**
     * Provider-specific option lookup, e.g. "encryption.bcrypt.strength".
     */
    public Optional<String> opt(String key) {
        return str(key);
    }

    public Optional<Integer> optInt(String key) {
        return str(key).map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt);
    }

    public Optional<Boolean> optBool(String key) {
        return bool(key);
    }

    private Optional<String> str(String key) {
        String v = raw.getProperty(key);
        if (v == null) return Optional.empty();
        return Optional.of(v);
    }

    private Optional<Boolean> bool(String key) {
        String v = raw.getProperty(key);
        if (v == null) return Optional.empty();
        v = v.trim().toLowerCase();
        if (v.equals("true") || v.equals("yes") || v.equals("on") || v.equals("1")) return Optional.of(true);
        if (v.equals("false") || v.equals("no") || v.equals("off") || v.equals("0")) return Optional.of(false);
        throw new IllegalArgumentException("Invalid boolean value for '" + key + "': " + raw.getProperty(key));
    }
}