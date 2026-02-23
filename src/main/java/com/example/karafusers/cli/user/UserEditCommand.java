package com.example.karafusers.cli.user;

import java.io.Console;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import picocli.CommandLine;

import com.example.karafusers.cli.ExitCodes;
import com.example.karafusers.karaf.DefaultKarafUsersService;

@CommandLine.Command(
        name = "edit",
        description = "Edit a user: add/remove roles/groups, or change password."
)
public class UserEditCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private UserCommand parent;

    @CommandLine.Option(names = {"-u", "--username"}, description = "Username to edit (otherwise prompted).")
    private String username;

    @CommandLine.Option(names = {"--add-roles"}, description = "Comma-separated roles to add.")
    private String addRolesCsv;

    @CommandLine.Option(names = {"--remove-roles"}, description = "Comma-separated roles to remove.")
    private String removeRolesCsv;

    @CommandLine.Option(names = {"--add-groups"}, description = "Comma-separated groups to add.")
    private String addGroupsCsv;

    @CommandLine.Option(names = {"--remove-groups"}, description = "Comma-separated groups to remove.")
    private String removeGroupsCsv;

    @CommandLine.Option(
            names = {"--password"},
            description = "New password (NOT recommended: visible via process list / shell history)."
    )
    private String password;

    @CommandLine.Option(
            names = {"--set-password"},
            description = "Prompt for a new password (with confirmation)."
    )
    private boolean setPassword;

    @Override
    public Integer call() throws Exception {
        String u = (username == null || username.isBlank()) ? promptLine("Username: ") : username.trim();

        Set<String> addRoles = parseCsv(addRolesCsv);
        Set<String> removeRoles = parseCsv(removeRolesCsv);
        Set<String> addGroups = parseCsv(addGroupsCsv);
        Set<String> removeGroups = parseCsv(removeGroupsCsv);

        String newPassword = null;
        if (password != null && setPassword) {
            throw new IllegalArgumentException("Use either --password or --set-password, not both.");
        }
        if (password != null) {
            newPassword = password;
        } else if (setPassword) {
            newPassword = promptPasswordWithConfirmation("New password: ", "Confirm new password: ");
        }

        if (addRoles.isEmpty() && removeRoles.isEmpty() && addGroups.isEmpty() && removeGroups.isEmpty() && newPassword == null) {
            throw new IllegalArgumentException("No changes requested. Use --add-roles/--remove-roles/--add-groups/--remove-groups/--set-password.");
        }

        var service = new DefaultKarafUsersService(parent.root());
        service.editUser(u, addRoles, removeRoles, addGroups, removeGroups, newPassword);
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
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Value cannot be empty.");
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