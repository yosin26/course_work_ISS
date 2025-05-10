package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\nSelect mode:");
            System.out.println("1) Server");
            System.out.println("2) Client");
            System.out.println("3) Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Очистка буфера
            
            switch (choice) {
                case 1:
                    server.Server.main(args);
                    break;
                case 2:
                    client.Client.main(args);
                    break;
                case 3:
                    System.exit(0);
                default:
                    System.out.println("Invalid option");
            }
        }
    }
}