/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package Clientes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread receiverThread;
    private boolean connected = false;

    private String username;

    public TCPClient(Scanner scanner) throws IOException {

        socket = new Socket(SERVER_IP, SERVER_PORT);
        connected = true;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {

            System.out.print("Ingrese usuario: ");
            username = scanner.nextLine().trim();

            out.println(username);

            String response = in.readLine();

            if ("OK".equals(response)) {
                System.out.println("Conectado al servidor\n");
                break;
            } else {
                System.out.println("Usuario ya existente\n");
            }
        }
    }

    public void startReceiving() {

        receiverThread = new Thread(() -> {

            try {

                String msg;

                while (connected && (msg = in.readLine()) != null) {

                    System.out.println("\n" + msg);
                    System.out.print("> ");
                }

            } catch (IOException e) {

                System.out.println("Conexion cerrada");
            }

        });

        receiverThread.start();
    }

    public void sendMessage(String message) {

        if (out != null && connected) {
            out.println(message);
        }
    }

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

        System.out.println("Cliente desconectado");
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        TCPClient client = null;

        try {

            client = new TCPClient(scanner);

            client.startReceiving();

            System.out.println("============== CHAT TCP ==============");
            System.out.println("Mensajes globales:");
            System.out.println("Hola a todos");
            System.out.println();
            System.out.println("Mensajes privados:");
            System.out.println("@usuario mensaje");
            System.out.println();
            System.out.println("Salir:");
            System.out.println("exit");
            System.out.println("======================================\n");

            while (true) {

                System.out.print("> ");

                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {

                    client.sendMessage("exit");

                    client.stop();

                    break;
                }

                if (!input.isEmpty()) {
                    client.sendMessage(input);
                }
            }

        } catch (IOException e) {

            System.out.println("No se pudo conectar al servidor");

        } finally {

            scanner.close();
        }
    }
}
