package com.example.karafusers.cli;

/**
 * Centralized process exit codes for the CLI.
 *
 * Keep these stable for scripting/automation usage.
 */
public final class ExitCodes {

    private ExitCodes() {
        // Utility class
    }

    /**
     * Successful execution.
     */
    public static final int OK = 0;

    /**
     * Incorrect command usage (invalid arguments, missing required options, etc.).
     */
    public static final int USAGE = 2;

    /**
     * Security-related failure (insecure file permissions, ownership issues, etc.).
     */
    public static final int SECURITY_ERROR = 3;

    /**
     * Configuration error (invalid jaas.cfg, unsupported encryption type, etc.).
     */
    public static final int CONFIG_ERROR = 4;

    /**
     * Validation failure (user/group not found, already exists, inconsistent state, etc.).
     */
    public static final int VALIDATION_ERROR = 5;

    /**
     * I/O failure (read/write errors, atomic move failure, backup failure, etc.).
     */
    public static final int IO_ERROR = 6;

    /**
     * Unexpected internal error.
     */
    public static final int SOFTWARE = 10;
}