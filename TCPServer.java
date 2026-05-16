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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TCPServer {

    // Puerto del servidor TCP
    private static final int PORT = 8888;

    // Maximo de clientes permitidos
    private static final int MAX_CLIENTS = 5;

    // Formato de hora
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    // Objeto Random para el juego de moneda
    private static final Random random = new Random();

    // Socket del servidor
    private ServerSocket serverSocket;

    // Contador de clientes conectados
    private static int connectedClients = 0;

    // Mapa de usuarios conectados
    // Guarda:
    // Usuario -> PrintWriter
    private static final Map<String, PrintWriter> clients =
            new HashMap<>();

    // Constructor
    public TCPServer() throws IOException {

        // Crear servidor TCP
        serverSocket = new ServerSocket(PORT);

        System.out.println("Servidor TCP iniciado");
    }

    // Generar timestamp
    private static String timestamp() {

        return "[" + LocalTime.now().format(formatter) + "]";
    }

    // Iniciar servidor
    public void start() {

        try {

            // Ciclo infinito
            while (true) {

                // Esperar cliente
                Socket socket = serverSocket.accept();

                synchronized (TCPServer.class) {

                    // Verificar limite de clientes
                    if (connectedClients >= MAX_CLIENTS) {

                        // Crear flujo temporal
                        PrintWriter temp =
                                new PrintWriter(
                                        socket.getOutputStream(),
                                        true
                                );

                        // Avisar que el servidor esta lleno
                        temp.println("Servidor lleno");

                        // Cerrar conexion
                        socket.close();

                        continue;
                    }

                    // Incrementar contador
                    connectedClients++;
                }

                // Crear hilo para el cliente
                new Thread(new ClientHandler(socket)).start();
            }

        } catch (IOException e) {

            stop();
        }
    }

    // Detener servidor
    public void stop() {

        try {

            if (serverSocket != null) {

                serverSocket.close();
            }

        } catch (IOException e) {
        }
    }

    // Clase interna para manejar clientes
    private static class ClientHandler implements Runnable {

        // Socket del cliente
        private Socket socket;

        // Flujo de entrada
        private BufferedReader in;

        // Flujo de salida
        private PrintWriter out;

        // Nombre de usuario
        private String username;

        // Constructor
        public ClientHandler(Socket socket) {

            this.socket = socket;
        }

        // Metodo principal del hilo
        @Override
        public void run() {

            try {

                // Crear flujo de entrada
                in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()
                        )
                );

                // Crear flujo de salida
                out = new PrintWriter(
                        socket.getOutputStream(),
                        true
                );

                // Solicitar nombre de usuario
                while (true) {

                    username = in.readLine();

                    // Si el cliente se desconecta
                    if (username == null) {
                        return;
                    }

                    // Eliminar espacios
                    username = username.trim();

                    // Validar regex
                    if (!username.matches("^[a-zA-Z0-9]{1,10}$")) {

                        out.println("ERROR");

                        continue;
                    }

                    synchronized (TCPServer.class) {

                        // Verificar usuario repetido
                        if (clients.containsKey(username)) {

                            out.println("ERROR");

                        } else {

                            // Agregar usuario
                            clients.put(username, out);

                            // Confirmar conexion
                            out.println("OK");

                            break;
                        }
                    }
                }

                // Avisar conexion
                broadcast(timestamp()
                        + " [SERVIDOR] "
                        + username
                        + " se unio");

                // Enviar lista de usuarios
                sendUserList();

                String msg;

                // Leer mensajes
                while ((msg = in.readLine()) != null) {

                    // Salir del chat
                    if (msg.equalsIgnoreCase("exit")) {
                        break;
                    }

                    // Juego de moneda
                    if (msg.equalsIgnoreCase("/moneda")) {

                        coinFlip();

                    // Mensaje privado
                    } else if (msg.startsWith("@")) {

                        privateMessage(msg);

                    // Mensaje global
                    } else {

                        broadcast(timestamp()
                                + " ["
                                + username
                                + "] : "
                                + msg);
                    }
                }

            } catch (IOException e) {

                System.out.println("Cliente desconectado");

            } finally {

                // Desconectar cliente
                disconnectClient();
            }
        }

        // Juego de moneda
        private void coinFlip() {

            String resultado;

            // Generar resultado aleatorio
            if (random.nextBoolean()) {

                resultado = "CARA";

            } else {

                resultado = "CRUZ";
            }

            // Enviar resultado a todos
            broadcast(
                    timestamp()
                            + " [JUEGO] "
                            + username
                            + " lanzó una moneda: "
                            + resultado
            );
        }

        // Mensaje privado
        private void privateMessage(String msg) {

            try {

                // Buscar espacio
                int firstSpace = msg.indexOf(" ");

                // Validar formato
                if (firstSpace == -1) {

                    out.println("Formato invalido");

                    return;
                }

                // Obtener usuario destino
                String destino = msg.substring(1, firstSpace);

                // Obtener mensaje
                String mensaje = msg.substring(firstSpace + 1);

                synchronized (TCPServer.class) {

                    // Buscar receptor
                    PrintWriter receptor =
                            clients.get(destino);

                    // Verificar existencia
                    if (receptor != null) {

                        // Enviar mensaje al receptor
                        receptor.println(
                                timestamp()
                                        + " [PRIVADO] "
                                        + username
                                        + " -> "
                                        + mensaje
                        );

                        // Confirmar al emisor
                        out.println(
                                timestamp()
                                        + " [PRIVADO a "
                                        + destino
                                        + "] "
                                        + mensaje
                        );

                    } else {

                        out.println("Usuario no encontrado");
                    }
                }

            } catch (Exception e) {

                out.println("Error");
            }
        }

        // Desconectar cliente
        private void disconnectClient() {

            try {

                if (socket != null) {
                    socket.close();
                }

            } catch (IOException e) {
            }

            synchronized (TCPServer.class) {

                // Verificar usuario
                if (username != null &&
                        clients.containsKey(username)) {

                    // Eliminar usuario
                    clients.remove(username);

                    // Reducir contador
                    connectedClients--;

                    // Avisar desconexion
                    broadcast(timestamp()
                            + " [SERVIDOR] "
                            + username
                            + " se desconecto");

                    // Actualizar lista
                    sendUserList();
                }
            }
        }
    }

    // Enviar mensaje global
    private static void broadcast(String msg) {

        synchronized (TCPServer.class) {

            // Recorrer clientes
            for (PrintWriter out : clients.values()) {

                out.println(msg);
            }
        }
    }

    // Enviar lista de usuarios
    private static void sendUserList() {

        synchronized (TCPServer.class) {

            StringBuilder users =
                    new StringBuilder();

            users.append("[USUARIOS] ");

            // Agregar usuarios
            for (String user : clients.keySet()) {

                users.append(user).append(" ");
            }

            // Enviar lista a todos
            for (PrintWriter out : clients.values()) {

                out.println(users.toString());
            }
        }
    }

    // Metodo principal
    public static void main(String[] args) {

        try {

            // Crear servidor
            TCPServer server = new TCPServer();

            // Iniciar servidor
            server.start();

        } catch (IOException e) {

            System.out.println("No se pudo iniciar");
        }
    }
}