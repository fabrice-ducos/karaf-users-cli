package com.example.karafusers.crypto;

import java.util.Objects;

/**
 * Handles encoding and verification of stored passwords,
 * including optional prefix/suffix wrapping used by Karaf.
 */
public final class KarafPasswordCodec {

    private final org.springframework.security.crypto.password.PasswordEncoder encoder;
    private final String prefix;
    private final String suffix;

    public KarafPasswordCodec(org.springframework.security.crypto.password.PasswordEncoder encoder,
                              String prefix,
                              String suffix) {
        this.encoder = Objects.requireNonNull(encoder, "encoder");
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
    }

    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        String encoded = encoder.encode(rawPassword);
        return prefix + encoded + suffix;
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) return false;
        if (storedPassword == null || storedPassword.isEmpty()) return false;

        String inner = unwrap(storedPassword);
        return encoder.matches(rawPassword, inner);
    }

    public String unwrap(String storedPassword) {
        String s = storedPassword;
        if (!prefix.isEmpty() && s.startsWith(prefix)) {
            s = s.substring(prefix.length());
        }
        if (!suffix.isEmpty() && s.endsWith(suffix)) {
            s = s.substring(0, s.length() - suffix.length());
        }
        return s;
    }

    public String prefix() { return prefix; }
    public String suffix() { return suffix; }
}