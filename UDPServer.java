/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servidores;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UDPServer {

    // Puerto UDP
    private static final int PORT = 9999;

    // Maximo de clientes
    private static final int MAX_CLIENTS = 5;

    // Formato de hora
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    // Random para el juego de moneda
    private static final Random random = new Random();

    // Socket UDP del servidor
    private DatagramSocket socket;

    // Clase para guardar informacion del cliente
    private static class ClientInfo {

        // Direccion IP del cliente
        InetAddress address;

        // Puerto del cliente
        int port;

        // Constructor
        public ClientInfo(
                InetAddress address,
                int port
        ) {

            this.address = address;

            this.port = port;
        }
    }

    // Lista de clientes conectados
    private final Map<String, ClientInfo> clients =
            new HashMap<>();

    // Constructor
    public UDPServer() throws Exception {

        // Crear socket UDP
        socket = new DatagramSocket(PORT);

        System.out.println("Servidor UDP iniciado");
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

                // Buffer de datos
                byte[] buffer = new byte[1024];

                // Paquete UDP
                DatagramPacket packet =
                        new DatagramPacket(
                                buffer,
                                buffer.length
                        );

                // Esperar mensaje
                socket.receive(packet);

                // Obtener mensaje
                String msg = new String(
                        packet.getData(),
                        0,
                        packet.getLength()
                ).trim();

                // Obtener direccion IP
                InetAddress address =
                        packet.getAddress();

                // Obtener puerto
                int port = packet.getPort();

                // Procesar mensaje
                handleMessage(
                        msg,
                        address,
                        port
                );
            }

        } catch (Exception e) {

            stop();
        }
    }

    // Procesar mensajes recibidos
    private void handleMessage(
            String msg,
            InetAddress address,
            int port
    ) throws Exception {

        // Buscar usuario
        String user =
                getUsername(address, port);

        // Si no existe usuario
        if (user == null) {

            registerUser(
                    msg,
                    address,
                    port
            );

            return;
        }

        // Salir del chat
        if (msg.equalsIgnoreCase("exit")) {

            disconnectUser(user);

            return;
        }

        // Juego de moneda
        if (msg.equalsIgnoreCase("/moneda")) {

            coinFlip(user);

        // Mensaje privado
        } else if (msg.startsWith("@")) {

            privateMessage(user, msg);

        // Mensaje global
        } else {

            broadcast(
                    timestamp()
                            + " ["
                            + user
                            + "] : "
                            + msg
            );
        }
    }

    // Juego de moneda
    private void coinFlip(String user)
            throws Exception {

        String resultado;

        // Generar cara o cruz
        if (random.nextBoolean()) {

            resultado = "CARA";

        } else {

            resultado = "CRUZ";
        }

        // Enviar resultado a todos
        broadcast(
                timestamp()
                        + " [JUEGO] "
                        + user
                        + " lanzó una moneda: "
                        + resultado
        );
    }

    // Registrar usuario
    private void registerUser(
            String username,
            InetAddress address,
            int port
    ) throws Exception {

        // Validar regex
        if (!username.matches("^[a-zA-Z0-9]{1,10}$")) {

            send(
                    "ERROR",
                    address,
                    port
            );

            return;
        }

        // Verificar limite
        if (clients.size() >= MAX_CLIENTS) {

            send(
                    "Servidor lleno",
                    address,
                    port
            );

            return;
        }

        // Verificar usuario repetido
        if (clients.containsKey(username)) {

            send(
                    "ERROR",
                    address,
                    port
            );

            return;
        }

        // Agregar cliente
        clients.put(
                username,
                new ClientInfo(address, port)
        );

        // Confirmar conexion
        send(
                "OK",
                address,
                port
        );

        // Avisar conexion
        broadcast(
                timestamp()
                        + " [SERVIDOR] "
                        + username
                        + " se unio"
        );

        // Actualizar lista
        sendUserList();
    }

    // Mensaje privado
    private void privateMessage(
            String sender,
            String msg
    ) throws Exception {

        // Buscar espacio
        int firstSpace = msg.indexOf(" ");

        if (firstSpace == -1) {

            return;
        }

        // Obtener usuario destino
        String destino =
                msg.substring(1, firstSpace);

        // Obtener mensaje
        String mensaje =
                msg.substring(firstSpace + 1);

        // Buscar receptor
        ClientInfo receptor =
                clients.get(destino);

        // Buscar emisor
        ClientInfo emisor =
                clients.get(sender);

        // Verificar existencia
        if (receptor != null) {

            // Enviar al receptor
            send(
                    timestamp()
                            + " [PRIVADO] "
                            + sender
                            + " -> "
                            + mensaje,
                    receptor.address,
                    receptor.port
            );

            // Confirmar al emisor
            send(
                    timestamp()
                            + " [PRIVADO a "
                            + destino
                            + "] "
                            + mensaje,
                    emisor.address,
                    emisor.port
            );
        }
    }

    // Desconectar usuario
    private void disconnectUser(
            String username
    ) throws Exception {

        // Eliminar usuario
        clients.remove(username);

        // Avisar desconexion
        broadcast(
                timestamp()
                        + " [SERVIDOR] "
                        + username
                        + " se desconecto"
        );

        // Actualizar lista
        sendUserList();
    }

    // Enviar mensaje global
    private void broadcast(String msg)
            throws Exception {

        // Recorrer clientes
        for (ClientInfo client :
                clients.values()) {

            send(
                    msg,
                    client.address,
                    client.port
            );
        }
    }

    // Enviar lista de usuarios
    private void sendUserList()
            throws Exception {

        StringBuilder users =
                new StringBuilder();

        users.append("[USUARIOS] ");

        // Agregar usuarios
        for (String user :
                clients.keySet()) {

            users.append(user)
                    .append(" ");
        }

        // Enviar lista
        broadcast(users.toString());
    }

    // Enviar paquete UDP
    private void send(
            String msg,
            InetAddress address,
            int port
    ) throws Exception {

        // Convertir mensaje
        byte[] buffer = msg.getBytes();

        // Crear paquete
        DatagramPacket packet =
                new DatagramPacket(
                        buffer,
                        buffer.length,
                        address,
                        port
                );

        // Enviar paquete
        socket.send(packet);
    }

    // Buscar usuario por IP y puerto
    private String getUsername(
            InetAddress address,
            int port
    ) {

        // Recorrer clientes
        for (Map.Entry<String, ClientInfo> entry
                : clients.entrySet()) {

            ClientInfo info = entry.getValue();

            // Comparar datos
            if (info.address.equals(address)
                    && info.port == port) {

                return entry.getKey();
            }
        }

        return null;
    }

    // Detener servidor
    public void stop() {

        if (socket != null) {

            socket.close();
        }
    }

    // Metodo principal
    public static void main(String[] args) {

        try {

            // Crear servidor
            UDPServer server =
                    new UDPServer();

            // Iniciar servidor
            server.start();

        } catch (Exception e) {

            System.out.println("Error");
        }
    }
}