package server.database;

import shared.models.User;

import java.util.List;

public class UserRepository {
    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public void addUser(User user) {
        try {
            database.addUser(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add user", e);
        }
    }

    public User getUser(String username) {
        try {
            return database.getUser(username);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public List<User> getAllUsers() {
        try {
            return database.getAllUsers();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all users", e);
        }
    }

    public void deleteUser(String usernameToDelete) {
        try {
            database.deleteUser(usernameToDelete);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

}