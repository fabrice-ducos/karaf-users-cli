package com.example.karafusers.cli.user;

//import com.example.karafusers.cli.ExitCodes;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import com.example.karafusers.cli.ExitCodes;
import com.example.karafusers.karaf.DefaultKarafUsersService;

@CommandLine.Command(
        name = "list",
        description = "List users with their roles and groups."
)
public class UserListCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private UserCommand parent;

    @CommandLine.Option(names = {"--no-resolve-groups"}, description = "Do not resolve group roles; list only direct user entries.")
    private boolean noResolveGroups;

    @Override
    public Integer call() throws Exception {
        // FIXME: wire the real service and print results.
        // var users = service.listUsers(parent.root(), !noResolveGroups);
        // users.forEach(u -> System.out.println(...));
        var service = new DefaultKarafUsersService(parent.root());
        service.listUsers(!noResolveGroups);
        return ExitCodes.OK;

        // return ExitCodes.OK;
    }
}