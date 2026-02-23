package com.example.karafusers.karaf;

import java.util.Set;

public interface KarafUsersService {

    void addUser(String username,
                 String rawPassword,
                 Set<String> roles,
                 Set<String> groups);

    void deleteUser(String username);

    void listUsers(boolean resolveGroups);

    void editUser(String username,
                  Set<String> addRoles,
                  Set<String> removeRoles,
                  Set<String> addGroups,
                  Set<String> removeGroups,
                  String newPassword);
}