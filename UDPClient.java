/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Clientes;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.Scanner;

public class UDPClient {

    // IP del servidor
    private static final String SERVER_IP =
            "127.0.0.1";

    // Puerto UDP
    private static final int SERVER_PORT =
            9999;

    // Socket UDP
    private DatagramSocket socket;

    // Direccion del servidor
    private InetAddress serverAddress;

    // Estado de conexion
    private boolean connected = true;

    // Nombre de usuario
    private String username;

    // Constructor
    public UDPClient(Scanner scanner)
            throws Exception {

        // Crear socket UDP
        socket = new DatagramSocket();

        // Obtener IP del servidor
        serverAddress =
                InetAddress.getByName(
                        SERVER_IP
                );

        // Solicitar usuario
        while (true) {

            System.out.print("Usuario: ");

            username =
                    scanner.nextLine().trim();

            // Enviar usuario
            send(username);

            // Leer respuesta
            String response = receive();

            // Usuario valido
            if (response.equals("OK")) {

                System.out.println(
                        "Conectado\n"
                );

                break;

            } else {

                System.out.println(
                        "Nombre invalido o en uso\n"
                );
            }
        }
    }

    // Iniciar recepcion
    public void startReceiving() {

        Thread receiverThread =
                new Thread(() -> {

            while (connected) {

                try {

                    String msg = receive();

                    System.out.println(
                            "\n" + msg
                    );

                    System.out.print("> ");

                } catch (Exception e) {

                    break;
                }
            }
        });

        receiverThread.start();
    }

    // Enviar mensaje
    public void send(String msg)
            throws Exception {

        byte[] buffer = msg.getBytes();

        DatagramPacket packet =
                new DatagramPacket(
                        buffer,
                        buffer.length,
                        serverAddress,
                        SERVER_PORT
                );

        socket.send(packet);
    }

    // Recibir mensaje
    public String receive()
            throws Exception {

        byte[] buffer = new byte[1024];

        DatagramPacket packet =
                new DatagramPacket(
                        buffer,
                        buffer.length
                );

        socket.receive(packet);

        return new String(
                packet.getData(),
                0,
                packet.getLength()
        );
    }

    // Desconectar cliente
    public void stop() {

        connected = false;

        socket.close();

        System.out.println(
                "Desconectado"
        );
    }

    // Metodo principal
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        UDPClient client = null;

        try {

            // Crear cliente
            client = new UDPClient(scanner);

            // Iniciar recepcion
            client.startReceiving();

            // Mostrar comandos
            System.out.println("================================");
            System.out.println("           CHAT UDP");
            System.out.println("================================");
            System.out.println("Mensaje global:");
            System.out.println("hola");
            System.out.println();
            System.out.println("Mensaje privado:");
            System.out.println("@usuario hola");
            System.out.println();
            System.out.println("Juego moneda:");
            System.out.println("/moneda");
            System.out.println();
            System.out.println("Salir:");
            System.out.println("exit");
            System.out.println("================================");

            // Ciclo principal
            while (true) {

                System.out.print("> ");

                String input =
                        scanner.nextLine().trim();

                // Salir
                if (
                        input.equalsIgnoreCase(
                                "exit"
                        )
                ) {

                    client.send("exit");

                    client.stop();

                    break;
                }

                // Enviar mensaje
                if (!input.isEmpty()) {

                    client.send(input);
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error de conexion. Es posible que el servidor este lleno."
            );
        }
    }
}
