package server.handlers;

import server.database.UserRepository;
import server.security.PasswordHasher;
import shared.models.User;

public class AuthHandler {
    private final UserRepository userRepository;

    public AuthHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.getUser(username);
        if (user == null) {
            return false;
        }
        String hashedPassword = PasswordHasher.hashPassword(password);
        return user.getPasswordHash().equals(hashedPassword);
    }
}