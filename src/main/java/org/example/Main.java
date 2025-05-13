package org.example;

import client.Client;
import server.Server;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nSelect mode:");
            System.out.println("1) Server");
            System.out.println("2) Client");
            System.out.println("3) Exit");
            System.out.print("Choose an option: ");

            if (!scanner.hasNextLine()) {
                System.out.println("\nNo input. Exiting.");
                break;
            }

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    Server.main(new String[]{});
                    break;
                case "2":
                    Client.main(new String[]{});
                    break;
                case "3":
                case "exit":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid input. Please choose 1, 2, or 3.");
            }
        }
    }
}
