/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Clientes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.Scanner;

public class TCPClient {

    // IP del servidor
    private static final String SERVER_IP = "127.0.0.1";

    // Puerto del servidor
    private static final int SERVER_PORT = 8888;

    // Socket del cliente
    private Socket socket;

    // Entrada de mensajes
    private BufferedReader in;

    // Salida de mensajes
    private PrintWriter out;

    // Hilo receptor
    private Thread receiverThread;

    // Estado de conexion
    private boolean connected = false;

    // Nombre de usuario
    private String username;

    // Constructor
    public TCPClient(Scanner scanner)
            throws IOException {

        // Conexion con el servidor
        socket = new Socket(
                SERVER_IP,
                SERVER_PORT
        );

        connected = true;

        // Flujo de entrada
        in = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()
                )
        );

        // Flujo de salida
        out = new PrintWriter(
                socket.getOutputStream(),
                true
        );

        // Solicitar usuario
        while (true) {

            System.out.print("Usuario: ");

            username = scanner.nextLine().trim();

            // Enviar usuario
            out.println(username);

            // Leer respuesta
            String response = in.readLine();

            // Usuario valido
            if ("OK".equals(response)) {

                System.out.println("Conectado\n");

                break;

            } else {

                System.out.println(
                        "Nombre invalido o en uso\n"
                );
            }
        }
    }

    // Iniciar recepcion de mensajes
    public void startReceiving() {

        receiverThread = new Thread(() -> {

            try {

                String msg;

                while (
                        connected &&
                        (msg = in.readLine()) != null
                ) {

                    System.out.println(
                            "\n" + msg
                    );

                    System.out.print("> ");
                }

            } catch (IOException e) {
            }
        });

        receiverThread.start();
    }

    // Enviar mensaje al servidor
    public void sendMessage(String message) {

        if (out != null && connected) {

            out.println(message);
        }
    }

    // Desconectar cliente
    public void stop() {

        connected = false;

        try {

            if (socket != null) {
                socket.close();
            }

            if (receiverThread != null) {
                receiverThread.join(500);
            }

        } catch (Exception e) {
        }

        System.out.println("Desconectado");
    }

    // Metodo principal
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        TCPClient client = null;

        try {

            // Crear cliente
            client = new TCPClient(scanner);

            // Iniciar recepcion
            client.startReceiving();

            // Mostrar comandos
            System.out.println("================================");
            System.out.println("           CHAT TCP");
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

                    client.sendMessage("exit");

                    client.stop();

                    break;
                }

                // Enviar mensaje
                if (!input.isEmpty()) {

                    client.sendMessage(input);
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Error de conexion. Es posible que el servidor este lleno."
            );
        }
    }
}
