package server.commands;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CdCommand implements Command {
    @Override
    public String execute(ObjectInputStream ois, ObjectOutputStream oos) {
        try {
            String path = (String) ois.readObject();
            File newDir = new File(path);

            if (!newDir.exists()) {
                return "Directory does not exist: " + path;
            }

            if (!newDir.isDirectory()) {
                return "Not a directory: " + path;
            }

            System.setProperty("user.dir", newDir.getAbsolutePath());
            return "Changed directory to: " + newDir.getAbsolutePath();
        } catch (Exception e) {
            return "Error changing directory: " + e.getMessage();
        }
    }
}