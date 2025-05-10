package server.commands;

import java.io.*;
import java.nio.file.*;
import java.util.Date;
import shared.utils.PathUtils;

public class GetCommand implements Command {
    @Override
    public String execute(ObjectInputStream ois, ObjectOutputStream oos) {
        try {
            // 1. Получаем имя файла
            String filename = PathUtils.sanitizePath((String) ois.readObject());
            Path filePath = Paths.get("server/files", filename).normalize();
            
            // 2. Проверка безопасности пути
            if (!filePath.startsWith(Paths.get("server/files").normalize())) {
                oos.writeObject("ERROR: Access denied");
                oos.flush();
                return "Access violation attempt: " + filename;
            }
            
            // 3. Проверка существования файла
            if (!Files.exists(filePath)) {
                oos.writeObject("ERROR: File not found");
                oos.flush();
                return "File not found: " + filename;
            }
            
            // 4. Отправка метаданных
            oos.writeObject("OK");
            oos.writeLong(Files.size(filePath));
            oos.writeObject(new Date(Files.getLastModifiedTime(filePath).toMillis()));
            oos.flush();
            
            // 5. Отправка содержимого файла
            try (InputStream fis = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    oos.write(buffer, 0, bytesRead);
                }
            }
            oos.flush();
            
            return "File sent: " + filename + " (" + Files.size(filePath) + " bytes)";
            
        } catch (Exception e) {
            try {
                oos.writeObject("ERROR: " + e.getMessage());
                oos.flush();
            } catch (IOException ex) {
                System.err.println("Failed to send error message to client");
            }
            return "Failed to send file: " + e.getMessage();
        }
    }
}