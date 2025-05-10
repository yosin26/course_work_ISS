package server.commands;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class LsCommand implements Command {
    @Override
    public String execute(ObjectInputStream ois, ObjectOutputStream oos) {
        try {
            String options = "";

            Object param = ois.readObject();
            if (param instanceof String) {
                options = (String) param;
            }

            boolean showAll = options.contains("a");
            boolean longFormat = options.contains("l");

            File currentDir = new File(System.getProperty("user.dir"));
            File[] files = currentDir.listFiles();

            if (files == null) {
                return "Directory is empty or cannot be accessed";
            }

            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });

            StringBuilder result = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm");

            for (File file : files) {
                if (!showAll && file.isHidden()) continue;

                if (longFormat) {
                    result.append(String.format("%s%s%s %10d %s %s%n",
                            file.canRead() ? "r" : "-",
                            file.canWrite() ? "w" : "-",
                            file.canExecute() ? "x" : "-",
                            file.length(),
                            dateFormat.format(new Date(file.lastModified())),
                            file.getName()));
                } else {
                    result.append(file.getName()).append("\n");
                }
            }

            return result.toString();
        } catch (Exception e) {
            return "Error listing files: " + e.getMessage();
        }
    }
}
