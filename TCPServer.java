/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Servidores;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {

    private static final int PORT = 8888;
    private static final int MAX_CLIENTS = 5;

    private ServerSocket serverSocket;

    private static int connectedClients = 0;

    private static final Map<String, PrintWriter> clients = new HashMap<>();

    public TCPServer() throws IOException {

        serverSocket = new ServerSocket(PORT);

        System.out.println("Servidor TCP iniciado en puerto " + PORT);
    }

    public void start() {

        try {

            while (true) {

                Socket socket = serverSocket.accept();

                synchronized (TCPServer.class) {

                    if (connectedClients >= MAX_CLIENTS) {

                        PrintWriter temp = new PrintWriter(socket.getOutputStream(), true);

                        temp.println("Servidor lleno");

                        socket.close();

                        continue;
                    }

                    connectedClients++;
                }

                new Thread(new ClientHandler(socket)).start();
            }

        } catch (IOException e) {

            stop();
        }
    }

    public void stop() {

        try {

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

        } catch (IOException e) {
        }
    }

    private static class ClientHandler implements Runnable {

        private Socket socket;

        private BufferedReader in;

        private PrintWriter out;

        private String username;

        public ClientHandler(Socket socket) {

            this.socket = socket;
        }

        @Override
        public void run() {

            try {

                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {

                    username = in.readLine();

                    if (username == null) {
                        return;
                    }

                    username = username.trim();

                    if (username.isEmpty()) {

                        out.println("ERROR");

                        continue;
                    }

                    synchronized (TCPServer.class) {

                        if (clients.containsKey(username)) {

                            out.println("ERROR");

                        } else {

                            clients.put(username, out);

                            out.println("OK");

                            break;
                        }
                    }
                }

                broadcast("[SERVIDOR] " + username + " se unio al chat");

                sendUserList();

                String msg;

                while ((msg = in.readLine()) != null) {

                    if (msg.equalsIgnoreCase("exit")) {
                        break;
                    }

                    if (msg.startsWith("@")) {

                        privateMessage(msg);

                    } else {

                        broadcast("[" + username + "] : " + msg);
                    }
                }

            } catch (IOException e) {

                System.out.println("Error con cliente");

            } finally {

                disconnectClient();
            }
        }

        private void privateMessage(String msg) {

            try {

                int firstSpace = msg.indexOf(" ");

                if (firstSpace == -1) {

                    out.println("Formato invalido");
                    return;
                }

                String destino = msg.substring(1, firstSpace);

                String mensaje = msg.substring(firstSpace + 1);

                synchronized (TCPServer.class) {

                    PrintWriter receptor = clients.get(destino);

                    if (receptor != null) {

                        receptor.println("[PRIVADO] "
                                + username
                                + " -> "
                                + mensaje);

                        out.println("[PRIVADO a "
                                + destino
                                + "] "
                                + mensaje);

                    } else {

                        out.println("Usuario no encontrado");
                    }
                }

            } catch (Exception e) {

                out.println("Error enviando privado");
            }
        }

        private void disconnectClient() {

            try {

                if (socket != null) {
                    socket.close();
                }

            } catch (IOException e) {
            }

            synchronized (TCPServer.class) {

                if (username != null && clients.containsKey(username)) {

                    clients.remove(username);

                    connectedClients--;

                    broadcast("[SERVIDOR] "
                            + username
                            + " se desconecto");

                    sendUserList();
                }
            }
        }
    }

    private static void broadcast(String msg) {

        synchronized (TCPServer.class) {

            for (PrintWriter out : clients.values()) {

                out.println(msg);
            }
        }
    }

    private static void sendUserList() {

        synchronized (TCPServer.class) {

            StringBuilder users = new StringBuilder();

            users.append("[USUARIOS] ");

            for (String user : clients.keySet()) {

                users.append(user).append(" ");
            }

            for (PrintWriter out : clients.values()) {

                out.println(users.toString());
            }
        }
    }

    public static void main(String[] args) {

        try {

            TCPServer server = new TCPServer();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                server.stop();
            }));

            server.start();

        } catch (IOException e) {

            System.out.println("No se pudo iniciar el servidor");
        }
    }
}
