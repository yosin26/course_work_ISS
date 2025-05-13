package client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Date;
import java.util.Scanner;

import shared.utils.PathUtils;

public class ClientHandler {
    private final String host;
    private final int port;

    public ClientHandler(String host, int port) {
        this.host = host;
        this.port = port;
        new File("client/downloads").mkdirs();
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Scanner scanner = new Scanner(System.in)) {

            // Аутентификация
            authenticate(ois, oos, scanner);

            System.out.println("Connected to server. Type 'help' for command list.");

            // Основной цикл обработки команд
            handleCommandLoop(ois, oos, scanner);

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private void authenticate(ObjectInputStream ois, ObjectOutputStream oos, Scanner scanner)
            throws IOException, ClassNotFoundException {
        String authRequest = (String) ois.readObject();
        if ("AUTH_REQUEST".equals(authRequest)) {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            oos.writeObject(username);
            oos.writeObject(password);
            oos.flush();

            String authResponse = (String) ois.readObject();
            if (!"AUTH_SUCCESS".equals(authResponse)) {
                System.out.println("Authentication failed!");
                System.exit(1);
            }
        }
    }

    private void handleCommandLoop(ObjectInputStream ois, ObjectOutputStream oos, Scanner scanner) {
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty())
                continue;

            String[] parts = input.split(" ", 2);
            String command = parts[0];
            String argument = parts.length > 1 ? parts[1] : "";

            try {
                switch (command.toLowerCase()) {
                    case "exit":
                        oos.writeObject("exit");
                        oos.flush();
                        return;

                    case "help":
                        printHelp();
                        continue;

                    case "put":
                        handlePutCommand(argument, oos, ois);
                        continue;

                    case "get":
                        handleGetCommand(argument, oos, ois);
                        continue;

                    case "ls":
                        oos.writeObject("ls"); // отправляем команду
                        oos.writeObject(argument); // отправляем аргумент (может быть "", "-a", "-l", и т.д.)
                        oos.flush();

                        // читаем и печатаем ответ сервера
                        String lsResponse = (String) ois.readObject();
                        System.out.println(lsResponse);

                        continue;
                    case "cd":
                    case "file":
                    case "pwd":
                    case "clear":
                        oos.writeObject(command);
                        if (!argument.isEmpty()) {
                            oos.writeObject(argument);
                        }
                        oos.flush();

                        String response = (String) ois.readObject();
                        if ("__CLEAR__".equals(response)) {
                            clearTerminal();
                        } else {
                            System.out.println(response);
                        }
                        break;

                    default:
                        System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (Exception e) {
                System.out.println("Error executing command: " + e.getMessage());
            }
        }
    }

    private void clearTerminal() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("Unable to clear terminal.");
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("ls [options]    - List directory contents (-l for details, -a for hidden files)");
        System.out.println("pwd             - Print working directory");
        System.out.println("cd <directory>  - Change directory");
        System.out.println("file <name>     - Show file information");
        System.out.println("put <filename>  - Upload file to server");
        System.out.println("get <filename>  - Download file from server");
        System.out.println("help            - Show this help");
        System.out.println("exit            - Disconnect from server");
    }

    private void handleGetCommand(String filename, ObjectOutputStream oos, ObjectInputStream ois) {
        try {
            // 1. Отправляем команду и имя файла
            oos.writeObject("get");
            oos.writeObject(Paths.get(filename).getFileName().toString());
            oos.flush();

            // 2. Получаем ответ сервера
            String response = (String) ois.readObject();
            if (!"OK".equals(response)) {
                System.out.println(response);
                return;
            }

            // 3. Получаем метаданные
            long fileSize = ois.readLong();
            Date lastModified = (Date) ois.readObject();

            // 4. Подготовка к сохранению
            Path downloadPath = Paths.get("client/downloads",
                    Paths.get(filename).getFileName().toString());
            Files.createDirectories(downloadPath.getParent());

            // 5. Прогресс-бар
            System.out.printf("Downloading %s (%.2f MB)%n",
                    downloadPath.getFileName(), fileSize / (1024.0 * 1024.0));

            // 6. Получение файла
            try (OutputStream fos = Files.newOutputStream(downloadPath)) {
                byte[] buffer = new byte[8192];
                long totalRead = 0;
                long lastUpdate = System.currentTimeMillis();

                while (totalRead < fileSize) {
                    int bytesToRead = (int) Math.min(buffer.length, fileSize - totalRead);
                    int bytesRead = ois.read(buffer, 0, bytesToRead);
                    if (bytesRead == -1)
                        break;

                    fos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;

                    // Обновление прогресса каждые 500мс
                    if (System.currentTimeMillis() - lastUpdate > 500) {
                        System.out.printf("\rProgress: %.1f%%", (totalRead * 100.0 / fileSize));
                        lastUpdate = System.currentTimeMillis();
                    }
                }
                System.out.println("\rDownload complete! 100%");
            }

            // 7. Установка даты модификации
            Files.setLastModifiedTime(downloadPath,
                    FileTime.fromMillis(lastModified.getTime()));

            System.out.println("File saved to: " + downloadPath.toAbsolutePath());

        } catch (SocketTimeoutException e) {
            System.err.println("Connection timeout");
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handlePutCommand(String filename, ObjectOutputStream oos, ObjectInputStream ois) {
        try {
            Path sourcePath = Paths.get(filename);
            if (!Files.exists(sourcePath)) {
                System.out.println("File not found: " + filename);
                return;
            }

            // 1. Отправляем команду put
            oos.writeObject("put");
            oos.flush();

            // 2. Отправляем безопасное имя файла
            String safeFilename = PathUtils.sanitizePath(filename);
            oos.writeObject(safeFilename);
            oos.flush();

            // 3. Отправляем размер файла
            long fileSize = Files.size(sourcePath);
            oos.writeLong(fileSize);
            oos.flush();

            // 4. Отправляем содержимое файла
            try (InputStream fis = Files.newInputStream(sourcePath)) {
                byte[] buffer = new byte[8192];
                long totalSent = 0;
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    oos.write(buffer, 0, bytesRead);
                    totalSent += bytesRead;

                    // Обновляем прогресс каждые 5%
                    if (fileSize > 0 && totalSent % (fileSize / 20) == 0) {
                        System.out.printf("\rUploading: %d%%", (totalSent * 100 / fileSize));
                    }
                }
                System.out.println("\rUpload complete! 100%");
                oos.flush();
            }

            // 5. Получаем ответ сервера
            String response = (String) ois.readObject();
            System.out.println(response);

        } catch (Exception e) {
            System.err.println("Error uploading file: " + e.getMessage());
        }
    }
}
