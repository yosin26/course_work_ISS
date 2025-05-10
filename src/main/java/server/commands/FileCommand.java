package server.commands;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileCommand implements Command {
    @Override
    public String execute(ObjectInputStream ois, ObjectOutputStream oos) {
        try {
            String filename = (String) ois.readObject();
            File file = new File(filename);

            if (!file.exists()) {
                return "File not found: " + filename;
            }

            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            return String.format(
                "File information:\n" +
                "Name: %s\n" +
                "Path: %s\n" +
                "Size: %d bytes\n" +
                "Type: %s\n" +
                "Permissions: %s%s%s\n" +
                "Last modified: %s\n" +
                "Created: %s",
                file.getName(),
                file.getAbsolutePath(),
                file.length(),
                file.isDirectory() ? "Directory" : "File",
                file.canRead() ? "r" : "-",
                file.canWrite() ? "w" : "-",
                file.canExecute() ? "x" : "-",
                dateFormat.format(new Date(file.lastModified())),
                dateFormat.format(new Date(attrs.creationTime().toMillis()))
            );
        } catch (Exception e) {
            return "Error getting file info: " + e.getMessage();
        }
    }
}