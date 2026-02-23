package com.example.karafusers.cli.user;

import java.io.Console;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import picocli.CommandLine;

import com.example.karafusers.cli.ExitCodes;
import com.example.karafusers.karaf.DefaultKarafUsersService;

@CommandLine.Command(
        name = "add",
        description = "Create a user (with optional roles and groups). Prompts interactively if values are missing."
)
public class UserAddCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private UserCommand parent;

    @CommandLine.Option(names = {"-u", "--username"}, description = "Username to create (otherwise prompted).")
    private String username;

    @CommandLine.Option(
            names = {"-p", "--password"},
            description = "User password (NOT recommended: visible via process list / shell history). If omitted, prompted securely."
    )
    private String password;

    @CommandLine.Option(names = {"--roles"}, description = "Comma-separated roles to assign to the user.")
    private String rolesCsv;

    @CommandLine.Option(names = {"--groups"}, description = "Comma-separated groups to assign to the user.")
    private String groupsCsv;

    @Override
    public Integer call() throws Exception {
        String u = (username == null || username.isBlank()) ? promptLine("Username: ") : username.trim();

        String pwd;
        if (password != null) {
            pwd = password;
        } else {
            pwd = promptPasswordWithConfirmation("Password: ", "Confirm password: ");
        }

        Set<String> roles = parseCsv(rolesCsv);
        Set<String> groups = parseCsv(groupsCsv);

        var service = new DefaultKarafUsersService(parent.root());
        service.addUser(u, pwd, roles, groups);
        return ExitCodes.OK;
    }

    private static Set<String> parseCsv(String csv) {
        Set<String> out = new LinkedHashSet<>();
        if (csv == null || csv.isBlank()) return out;
        for (String part : csv.split(",")) {
            String s = part.trim();
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    private static String promptLine(String prompt) {
        Console c = System.console();
        if (c == null) {
            throw new IllegalStateException("No console available for interactive input. Provide required options instead.");
        }
        String v = c.readLine(prompt);
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Value cannot be empty.");
        }
        return v.trim();
    }

    private static String promptPasswordWithConfirmation(String prompt, String confirmPrompt) {
        Console c = System.console();
        if (c == null) {
            throw new IllegalStateException("No console available for password prompt. Use --password (not recommended) or run in a real terminal.");
        }

        char[] p1 = c.readPassword(prompt);
        if (p1 == null || p1.length == 0) throw new IllegalArgumentException("Password cannot be empty.");

        char[] p2 = c.readPassword(confirmPrompt);
        if (p2 == null) throw new IllegalArgumentException("Password confirmation is required.");

        String s1 = new String(p1);
        String s2 = new String(p2);

        if (!s1.equals(s2)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        return s1;
    }
}