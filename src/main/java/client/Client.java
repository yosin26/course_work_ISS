package client;

public class Client {
    public static void main(String[] args) {
        System.out.println("=== Client Mode ===");

        while (true) {
            ServerInfo server = ServerSelector.selectServer();
            if (server == null) {
                System.out.println("Exiting client.");
                break;
            }

            ClientHandler handler = new ClientHandler(server.getIp(), server.getPort());
            handler.start();
        }
    }
}
