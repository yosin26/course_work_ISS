package client;

import java.util.Map;
import java.util.Scanner;

public class ServerSelector {
    private static final Scanner scanner = new Scanner(System.in);

    public static ServerInfo selectServer() {
        while (true) {
            Map<Integer, ServerInfo> servers = ServerStorage.loadServers();
            printMenu(servers);

            System.out.print("Select server ID, 'new' to add (':q' to quit): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase(":q") || input.equalsIgnoreCase("exit")) {
                return null;
            }

            if (input.equalsIgnoreCase("new")) {
                addNewServer(servers);
                continue;
            }

            try {
                int id = Integer.parseInt(input);
                ServerInfo server = servers.get(id);
                if (server != null) {
                    return server;
                }
                System.out.println("Invalid ID.\n");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number or 'new'.\n");
            }
        }
    }

    private static void printMenu(Map<Integer, ServerInfo> servers) {
        System.out.println("\n--- Available Servers ---");
        servers.forEach((id, server) ->
                System.out.printf("%d: %s:%d%n", id, server.getIp(), server.getPort())
        );
        if (servers.isEmpty()) {
            System.out.println("No servers found.");
        }
        System.out.println("--------------------------");
    }

    private static void addNewServer(Map<Integer, ServerInfo> servers) {
        try {
            System.out.print("Enter IP address: ");
            String ip = scanner.nextLine().trim();

            System.out.print("Enter port: ");
            int port = Integer.parseInt(scanner.nextLine().trim());

            int newId = servers.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
            servers.put(newId, new ServerInfo(ip, port));
            ServerStorage.saveServers(servers);

            System.out.println("Server added successfully.\n");
        } catch (NumberFormatException e) {
            System.out.println("Invalid port format.\n");
        } catch (Exception e) {
            System.out.println("Error adding server: " + e.getMessage());
        }
    }
}
