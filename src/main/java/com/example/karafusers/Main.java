package com.example.karafusers;

import com.example.karafusers.cli.ExitCodes;
import com.example.karafusers.cli.RootCommand;
import picocli.CommandLine;

import java.io.PrintWriter;
//import java.util.concurrent.Callable;

public final class Main {

    private Main() {}

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RootCommand())
                .setExecutionExceptionHandler((ex, cmd, parseResult) -> handleException(ex, cmd))
                .setParameterExceptionHandler((ex, args0) -> handleParameterException(ex, args0))
                .execute(args);

        System.exit(exitCode);
    }

    private static int handleException(Exception ex, CommandLine cmd) {
        boolean debug = false; // FIXME: should be set by RootCommand
        PrintWriter err = cmd.getErr();

        // Keep messages clean and consistent: one-line error + optional hint.
        if (ex instanceof SecurityException) {
            err.println("ERROR: " + ex.getMessage());
            return ExitCodes.SECURITY_ERROR;
        }

        if (ex instanceof IllegalArgumentException) {
            err.println("ERROR: " + ex.getMessage());
            return ExitCodes.USAGE;
        }

        // Picocli wraps some errors; keep generic output for unexpected failures.
        err.println("ERROR: " + safeMessage(ex));
        //if (cmd.isDebug()) {
        if (debug) {
            ex.printStackTrace(err);
        } else {
            err.println("Hint: re-run with --debug for stacktrace.");
        }
        return ExitCodes.SOFTWARE;
    }

    private static int handleParameterException(CommandLine.ParameterException ex, String[] args) {
        CommandLine cmd = ex.getCommandLine();
        PrintWriter err = cmd.getErr();

        err.println("ERROR: " + ex.getMessage());
        err.println();
        err.println(cmd.getCommandSpec().usageMessage().descriptionHeading());
        cmd.usage(err);

        return ExitCodes.USAGE;
    }

    private static String safeMessage(Throwable t) {
        String msg = t.getMessage();
        if (msg == null || msg.isBlank()) {
            return t.getClass().getSimpleName();
        }
        return msg;
    }
}
