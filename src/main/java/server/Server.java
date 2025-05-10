package server;

import server.database.Database;
import server.database.UserRepository;
import server.handlers.AuthHandler;
import server.handlers.CommandHandler;
import server.security.PasswordHasher;
import shared.models.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Server {
    private final int port;
    private final UserRepository userRepository;
    private final AuthHandler authHandler;
    private final CommandHandler commandHandler;
    private boolean isRunning;

    public Server(int port) throws Exception {
        this.port = port;
        Database database = new Database();
        this.userRepository = new UserRepository(database);
        this.authHandler = new AuthHandler(userRepository);
        this.commandHandler = new CommandHandler();
        new File("server/files").mkdirs();
    }

    public void start() {
        isRunning = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

            // Аутентификация
            oos.writeObject("AUTH_REQUEST");
            String username = (String) ois.readObject();
            String password = (String) ois.readObject();

            if (authHandler.authenticate(username, password)) {
                oos.writeObject("AUTH_SUCCESS");

                // Обработка команд
                while (true) {
                    String command = (String) ois.readObject();
                    if ("exit".equalsIgnoreCase(command)) {
                        break;
                    }
                    String response = commandHandler.handleCommand(command, ois, oos);
                    oos.writeObject(response);
                    oos.flush();
                }
            } else {
                oos.writeObject("AUTH_FAILED");
            }
        } catch (Exception e) {
            System.err.println("Client handling error: " + e.getMessage());
        }
    }

    public void createUser(String username, String password) {
        String hashedPassword = PasswordHasher.hashPassword(password);
        userRepository.addUser(new User(username, hashedPassword));
        System.out.println("User created: " + username);
    }

    // Вывод всех пользователей
    public void listUsers() {
        List<User> users = userRepository.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("List of users:");

            for (int i = 0; i < users.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, users.get(i).getUsername());
            }
        }
    }

    // Удаление пользователя
    public void deleteUser() {
        // Сначала выводим список пользователей
        listUsers();
        if (userRepository.getAllUsers().isEmpty()) {
            System.out.println("No users to delete.");
            return;
        }

        // Запрашиваем имя пользователя для удаления
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the username of the user to delete: ");
        String usernameToDelete = scanner.nextLine();

        // Проверяем, существует ли пользователь с таким именем
        User user = userRepository.getUser(usernameToDelete);
        if (user != null) {
            userRepository.deleteUser(usernameToDelete);
            System.out.println("User deleted: " + usernameToDelete);
        } else {
            System.out.println("User not found: " + usernameToDelete);
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(8080);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\nServer Menu:");
                System.out.println("1) Start server");
                System.out.println("2) Create user");
                System.out.println("3) List users");
                System.out.println("4) Delete user");
                System.out.println("5) Exit");
                System.out.print("Choose an option: ");

                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        new Thread(server::start).start();
                        break;
                    case 2:
                        System.out.print("Username: ");
                        String username = scanner.nextLine();
                        System.out.print("Password: ");
                        String password = scanner.nextLine();
                        server.createUser(username, password);
                        break;
                    case 3:
                        server.listUsers();
                        break;
                    case 4:
                        server.deleteUser();
                        break;
                    case 5:
                        System.exit(0);
                    default:
                        System.out.println("Invalid option");
                }
            }
        } catch (Exception e) {
            System.err.println("Server initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
