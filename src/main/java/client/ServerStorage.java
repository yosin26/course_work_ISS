package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ServerStorage {
    private static final String CONFIG_FILE = "client/servers.json";
    private static final Gson gson = new Gson();

    static {
        ensureConfigExists();
    }

    public static Map<Integer, ServerInfo> loadServers() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Type type = new TypeToken<Map<Integer, ServerInfo>>() {}.getType();
            Map<Integer, ServerInfo> map = gson.fromJson(reader, type);
            return map != null ? map : new HashMap<>();
        } catch (IOException e) {
            System.err.println("Failed to read config: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void saveServers(Map<Integer, ServerInfo> servers) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(servers, writer);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    private static void ensureConfigExists() {
        File file = new File(CONFIG_FILE);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("{}");
            } catch (IOException e) {
                System.err.println("Failed to create config file: " + e.getMessage());
            }
        }
    }
}
