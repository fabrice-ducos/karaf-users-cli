package com.example.karafusers.cli;

import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "karaf-users",
        mixinStandardHelpOptions = true,
        version = "karaf-users 1.0.0",
        description = "CLI tool for managing Apache Karaf users.properties",
        subcommands = {
                // Register subcommands here
                // UserCommand.class,
                // GroupCommand.class
        }
)
public class RootCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = {"--users-file"},
            description = "Path to users.properties file. " +
                          "Defaults to $KARAF_ETC/users.properties or ./etc/users.properties"
    )
    private Path usersFile;

    @CommandLine.Option(
            names = {"--jaas-cfg"},
            description = "Path to org.apache.karaf.jaas.cfg file. " +
                          "Defaults to $KARAF_JAAS_CFG or $KARAF_ETC/org.apache.karaf.jaas.cfg"
    )
    private Path jaasCfg;

    @CommandLine.Option(
            names = {"--backup"},
            description = "Create a timestamped backup before modifying users.properties"
    )
    private boolean backup;

    @CommandLine.Option(
            names = {"--dry-run"},
            description = "Perform validation and show intended changes without writing files"
    )
    private boolean dryRun;

    @CommandLine.Option(
            names = {"--verbose"},
            description = "Enable verbose output"
    )
    private boolean verbose;

    @CommandLine.Option(
            names = {"--debug"},
            description = "Enable debug mode"
    )
    private boolean debug;

    @Override
    public Integer call() {
        // If no subcommand is provided, show usage.
        CommandLine cmd = new CommandLine(this);
        cmd.usage(System.out);
        return ExitCodes.USAGE;
    }

    /* ------------------------------------------------------------------
       Accessors for subcommands
       ------------------------------------------------------------------ */

    public Path getUsersFile() {
        return usersFile;
    }

    public Path getJaasCfg() {
        return jaasCfg;
    }

    public boolean isBackupEnabled() {
        return backup;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isDebug() {
        return debug;
    }
}