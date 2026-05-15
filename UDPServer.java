/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servidores;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {

    private static final int PORT = 9999;

    private static final int MAX_CLIENTS = 5;

    private DatagramSocket socket;

    private static class ClientInfo {

        InetAddress address;

        int port;

        public ClientInfo(InetAddress address, int port) {

            this.address = address;

            this.port = port;
        }
    }

    private final Map<String, ClientInfo> clients = new HashMap<>();

    public UDPServer() throws Exception {

        socket = new DatagramSocket(PORT);

        System.out.println("Servidor UDP iniciado en puerto " + PORT);
    }

    public void start() {

        try {

            while (true) {

                byte[] buffer = new byte[1024];

                DatagramPacket packet = new DatagramPacket(
                        buffer,
                        buffer.length
                );

                socket.receive(packet);

                String msg = new String(
                        packet.getData(),
                        0,
                        packet.getLength()
                ).trim();

                InetAddress clientAddress = packet.getAddress();

                int clientPort = packet.getPort();

                handleMessage(msg, clientAddress, clientPort);
            }

        } catch (Exception e) {

            stop();
        }
    }

    private void handleMessage(
            String msg,
            InetAddress address,
            int port
    ) throws Exception {

        String user = getUsername(address, port);

        if (user == null) {

            registerUser(msg, address, port);

            return;
        }

        if (msg.equalsIgnoreCase("exit")) {

            disconnectUser(user);

            return;
        }

        if (msg.startsWith("@")) {

            privateMessage(user, msg);

        } else {

            broadcast("[" + user + "] : " + msg);
        }
    }

    private void registerUser(
            String username,
            InetAddress address,
            int port
    ) throws Exception {

        if (clients.size() >= MAX_CLIENTS) {

            send("Servidor lleno", address, port);

            return;
        }

        if (clients.containsKey(username)) {

            send("ERROR", address, port);

            return;
        }

        clients.put(username, new ClientInfo(address, port));

        send("OK", address, port);

        broadcast("[SERVIDOR] " + username + " se unio");

        sendUserList();
    }

    private void privateMessage(
            String sender,
            String msg
    ) throws Exception {

        int firstSpace = msg.indexOf(" ");

        if (firstSpace == -1) {

            ClientInfo senderInfo = clients.get(sender);

            send("Formato invalido",
                    senderInfo.address,
                    senderInfo.port);

            return;
        }

        String destino = msg.substring(1, firstSpace);

        String mensaje = msg.substring(firstSpace + 1);

        ClientInfo receptor = clients.get(destino);

        ClientInfo emisor = clients.get(sender);

        if (receptor != null) {

            send("[PRIVADO] "
                            + sender
                            + " -> "
                            + mensaje,
                    receptor.address,
                    receptor.port
            );

            send("[PRIVADO a "
                            + destino
                            + "] "
                            + mensaje,
                    emisor.address,
                    emisor.port
            );

        } else {

            send("Usuario no encontrado",
                    emisor.address,
                    emisor.port
            );
        }
    }

    private void disconnectUser(String username) throws Exception {

        clients.remove(username);

        broadcast("[SERVIDOR] "
                + username
                + " se desconecto");

        sendUserList();
    }

    private void broadcast(String msg) throws Exception {

        for (ClientInfo client : clients.values()) {

            send(msg, client.address, client.port);
        }
    }

    private void sendUserList() throws Exception {

        StringBuilder users = new StringBuilder();

        users.append("[USUARIOS] ");

        for (String user : clients.keySet()) {

            users.append(user).append(" ");
        }

        broadcast(users.toString());
    }

    private void send(
            String msg,
            InetAddress address,
            int port
    ) throws Exception {

        byte[] buffer = msg.getBytes();

        DatagramPacket packet = new DatagramPacket(
                buffer,
                buffer.length,
                address,
                port
        );

        socket.send(packet);
    }

    private String getUsername(
            InetAddress address,
            int port
    ) {

        for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {

            ClientInfo info = entry.getValue();

            if (info.address.equals(address)
                    && info.port == port) {

                return entry.getKey();
            }
        }

        return null;
    }

    public void stop() {

        if (socket != null && !socket.isClosed()) {

            socket.close();
        }
    }

    public static void main(String[] args) {

        try {

            UDPServer server = new UDPServer();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                server.stop();
            }));

            server.start();

        } catch (Exception e) {

            System.out.println("No se pudo iniciar servidor UDP");
        }
    }
}