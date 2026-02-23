package com.example.karafusers.crypto;

import com.example.karafusers.config.JaasCfg;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

import java.util.Locale;
import java.util.Objects;

/**
 * Builds a password codec based on org.apache.karaf.jaas.cfg encryption settings.
 *
 * Currently supports:
 * - encryption.name = spring-security-crypto
 *   encryption.algorithm = bcrypt|pbkdf2|scrypt|argon2
 *
 * You can extend this later for "basic" and "jasypt" providers.
 */
public final class PasswordEncoderFactory {

    private PasswordEncoderFactory() {}

    public static KarafPasswordCodec from(JaasCfg cfg) {
        Objects.requireNonNull(cfg, "cfg");

        if (!cfg.encryptionEnabled()) {
            throw new IllegalStateException("Password encryption is disabled in jaas.cfg (encryption.enabled=false). Refusing to proceed.");
        }

        String provider = cfg.encryptionName()
                .orElseThrow(() -> new IllegalStateException("Missing 'encryption.name' in jaas.cfg."));

        String prefix = cfg.encryptionPrefix();
        String suffix = cfg.encryptionSuffix();

        switch (provider.trim().toLowerCase(Locale.ROOT)) {
            case "spring-security-crypto":
                return new KarafPasswordCodec(buildSpringCrypto(cfg), prefix, suffix);

            default:
                throw new IllegalStateException(
                        "Unsupported encryption provider '" + provider + "'. " +
                        "Supported: spring-security-crypto (for now)."
                );
        }
    }

    private static PasswordEncoder buildSpringCrypto(JaasCfg cfg) {
        String algo = cfg.encryptionAlgorithm()
                .orElseThrow(() -> new IllegalStateException(
                        "Missing 'encryption.algorithm' for spring-security-crypto in jaas.cfg. " +
                        "Expected one of: bcrypt, pbkdf2, scrypt, argon2."
                ))
                .trim()
                .toLowerCase(Locale.ROOT);

        switch (algo) {
            case "bcrypt":
                // Optional: encryption.bcrypt.strength (default 10)
                int strength = cfg.optInt("encryption.bcrypt.strength").orElse(10);
                return new BCryptPasswordEncoder(strength);

            case "pbkdf2":
                // Spring 6: Pbkdf2PasswordEncoder has a builder.
                // Optional keys (sane defaults if missing):
                // - encryption.pbkdf2.secret
                // - encryption.pbkdf2.iterations
                // - encryption.pbkdf2.hashWidth
                // - encryption.pbkdf2.saltLength
                // - encryption.pbkdf2.algorithm (e.g. PBKDF2WithHmacSHA256)
                String secret = cfg.opt("encryption.pbkdf2.secret").orElse("");
                int iterations = cfg.optInt("encryption.pbkdf2.iterations").orElse(310_000);
                //int hashWidth = cfg.optInt("encryption.pbkdf2.hashWidth").orElse(256);
                int saltLength = cfg.optInt("encryption.pbkdf2.saltLength").orElse(16);
                String pbkdf2Alg = cfg.opt("encryption.pbkdf2.algorithm").orElse("PBKDF2WithHmacSHA256");
                SecretKeyFactoryAlgorithm skfa = Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.valueOf(normalizePbkdf2Alg(pbkdf2Alg));

                // FIXME: Pbkdf2PasswordEncoder(CharSequence, int, int, int) is deprecated in favor of Pbkdf2PasswordEncoder(CharSequence, int, int, SecretKeyFactoryAlgorithm)
                //Pbkdf2PasswordEncoder pbkdf2 = new Pbkdf2PasswordEncoder(secret, saltLength, iterations, hashWidth);
                //pbkdf2.setAlgorithm(Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.valueOf(normalizePbkdf2Alg(pbkdf2Alg)));
                Pbkdf2PasswordEncoder pbkdf2 = new Pbkdf2PasswordEncoder(secret, saltLength, iterations, skfa);
                return pbkdf2;

            case "scrypt":
                // Optional keys:
                // - encryption.scrypt.cpuCost
                // - encryption.scrypt.memoryCost
                // - encryption.scrypt.parallelization
                // - encryption.scrypt.keyLength
                // - encryption.scrypt.saltLength
                int cpuCost = cfg.optInt("encryption.scrypt.cpuCost").orElse(1 << 14);
                int memoryCost = cfg.optInt("encryption.scrypt.memoryCost").orElse(8);
                int parallelization = cfg.optInt("encryption.scrypt.parallelization").orElse(1);
                int keyLength = cfg.optInt("encryption.scrypt.keyLength").orElse(32);
                int scryptSaltLength = cfg.optInt("encryption.scrypt.saltLength").orElse(16);

                return new SCryptPasswordEncoder(cpuCost, memoryCost, parallelization, keyLength, scryptSaltLength);

            case "argon2":
                // Optional keys:
                // - encryption.argon2.saltLength
                // - encryption.argon2.hashLength
                // - encryption.argon2.parallelism
                // - encryption.argon2.memory
                // - encryption.argon2.iterations
                int argonSaltLength = cfg.optInt("encryption.argon2.saltLength").orElse(16);
                int argonHashLength = cfg.optInt("encryption.argon2.hashLength").orElse(32);
                int argonParallelism = cfg.optInt("encryption.argon2.parallelism").orElse(1);
                int argonMemory = cfg.optInt("encryption.argon2.memory").orElse(1 << 16); // in KiB
                int argonIterations = cfg.optInt("encryption.argon2.iterations").orElse(3);

                return new Argon2PasswordEncoder(argonSaltLength, argonHashLength, argonParallelism, argonMemory, argonIterations);

            default:
                throw new IllegalStateException(
                        "Unsupported spring-security-crypto algorithm '" + algo + "'. " +
                        "Supported: bcrypt, pbkdf2, scrypt, argon2."
                );
        }
    }

    /**
     * Accept common names and map them to Spring's enum names where possible.
     * Spring's enum uses names like PBKDF2WithHmacSHA256, PBKDF2WithHmacSHA1, etc.
     */
    private static String normalizePbkdf2Alg(String v) {
        String s = v.trim();

        // If user already provided an exact enum name, keep it.
        if (s.startsWith("PBKDF2WithHmac")) {
            return s;
        }

        // Accept lower-case shorthand.
        String lc = s.toLowerCase(Locale.ROOT);
        switch (lc) {
            case "pbkdf2withhmacsha1":
            case "hmacsha1":
            case "sha1":
                return "PBKDF2WithHmacSHA1";
            case "pbkdf2withhmacsha256":
            case "hmacsha256":
            case "sha256":
                return "PBKDF2WithHmacSHA256";
            case "pbkdf2withhmacsha512":
            case "hmacsha512":
            case "sha512":
                return "PBKDF2WithHmacSHA512";
            default:
                // Best effort: pass through and let valueOf fail with a clear message.
                return s;
        }
    }
}