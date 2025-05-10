package shared.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {
    public static String sanitizePath(String path) {
        // Удаляем все небезопасные символы и оставляем только имя файла
        return Paths.get(path).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    public static String getSystemIndependentPath(String path) {
        return path.replace("\\", "/");
    }
}