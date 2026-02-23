package com.example.karafusers.cli.user;

import com.example.karafusers.cli.ExitCodes;
import com.example.karafusers.cli.RootCommand;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "user",
        description = "Manage users in Karaf users.properties",
        subcommands = {
                UserAddCommand.class,
                UserDelCommand.class,
                UserListCommand.class,
                UserEditCommand.class
        }
)
public class UserCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private RootCommand root;

    @Override
    public Integer call() {
        // If no subcommand is provided, show usage for "user".
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.forAnnotatedObject(this);
        CommandLine cmd = new CommandLine(spec);

        PrintWriter out = cmd.getOut();
        cmd.usage(out);

        return ExitCodes.USAGE;
    }

    public RootCommand root() {
        return root;
    }
}