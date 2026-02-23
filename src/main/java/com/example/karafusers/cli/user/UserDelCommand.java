package com.example.karafusers.cli.user;

import java.io.Console;
import java.util.concurrent.Callable;

import picocli.CommandLine;

import com.example.karafusers.cli.ExitCodes;
import com.example.karafusers.karaf.DefaultKarafUsersService;

@CommandLine.Command(
        name = "del",
        description = "Delete a user. Prompts for confirmation unless --force is used."
)
public class UserDelCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private UserCommand parent;

    @CommandLine.Option(names = {"-u", "--username"}, description = "Username to delete (otherwise prompted).")
    private String username;

    @CommandLine.Option(names = {"-f", "--force"}, description = "Do not prompt for confirmation.")
    private boolean force;

    @Override
    public Integer call() throws Exception {
        String u = (username == null || username.isBlank()) ? promptLine("Username: ") : username.trim();

        if (!force) {
            boolean ok = confirm("Delete user '" + u + "'? [y/N]: ");
            if (!ok) {
                System.out.println("Aborted.");
                return ExitCodes.OK;
            }
        }

        var service = new DefaultKarafUsersService(parent.root());
        service.deleteUser(u);
        return ExitCodes.OK;
    }

    private static String promptLine(String prompt) {
        Console c = System.console();
        if (c == null) {
            throw new IllegalStateException("No console available for interactive input. Provide required options instead.");
        }
        String v = c.readLine(prompt);
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Value cannot be empty.");
        return v.trim();
    }

    private static boolean confirm(String prompt) {
        Console c = System.console();
        if (c == null) {
            throw new IllegalStateException("No console available for interactive confirmation. Use --force or run in a real terminal.");
        }
        String v = c.readLine(prompt);
        if (v == null) return false;
        v = v.trim().toLowerCase();
        return v.equals("y") || v.equals("yes");
    }
}