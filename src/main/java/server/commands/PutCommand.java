package server.commands;

import java.io.*;
import java.nio.file.*;

public class PutCommand implements Command {
    @Override
    public String execute(ObjectInputStream ois, ObjectOutputStream oos) {
        try {
            // 1. Получаем имя файла
            String filename = (String) ois.readObject();
            Path filePath = Paths.get("server/files/" + filename).normalize();

            // 2. Получаем размер файла
            long fileSize = ois.readLong();

            // 3. Создаем директории если нужно
            Files.createDirectories(filePath.getParent());

            // 4. Получаем содержимое файла
            try (OutputStream fileStream = Files.newOutputStream(filePath)) {
                byte[] buffer = new byte[4096];
                long remaining = fileSize;
                
                while (remaining > 0) {
                    int bytesToRead = (int) Math.min(buffer.length, remaining);
                    int bytesRead = ois.read(buffer, 0, bytesToRead);
                    if (bytesRead == -1) break;
                    
                    fileStream.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
            }

            // 5. Отправляем подтверждение
            oos.writeObject("File uploaded successfully: " + filename);
            return "File received: " + filename;

        } catch (Exception e) {
            try {
                oos.writeObject("Error: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return "Error receiving file: " + e.getMessage();
        }
    }
}