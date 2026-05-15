/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clientes;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9999;

    private DatagramSocket socket;

    private InetAddress serverAddress;

    private boolean connected = true;

    private String username;

    public UDPClient(Scanner scanner) throws Exception {

        socket = new DatagramSocket();

        serverAddress = InetAddress.getByName(SERVER_IP);

        while (true) {

            System.out.print("Ingrese usuario: ");

            username = scanner.nextLine().trim();

            send(username);

            String response = receive();

            if (response.equals("OK")) {

                System.out.println("Conectado al servidor UDP\n");

                break;

            } else {

                System.out.println("Usuario ya existente\n");
            }
        }
    }

    public void startReceiving() {

        Thread receiverThread = new Thread(() -> {

            while (connected) {

                try {

                    String msg = receive();

                    System.out.println("\n" + msg);

                    System.out.print("> ");

                } catch (Exception e) {

                    break;
                }
            }
        });

        receiverThread.start();
    }

    public void send(String msg) throws Exception {

        byte[] buffer = msg.getBytes();

        DatagramPacket packet = new DatagramPacket(
                buffer,
                buffer.length,
                serverAddress,
                SERVER_PORT
        );

        socket.send(packet);
    }

    public String receive() throws Exception {

        byte[] buffer = new byte[1024];

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        return new String(packet.getData(), 0, packet.getLength());
    }

    public void stop() {

        connected = false;

        socket.close();

        System.out.println("Cliente UDP desconectado");
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        UDPClient client = null;

        try {

            client = new UDPClient(scanner);

            client.startReceiving();

            System.out.println("============== CHAT UDP ==============");
            System.out.println("Mensaje global:");
            System.out.println("hola");
            System.out.println();
            System.out.println("Mensaje privado:");
            System.out.println("@usuario mensaje");
            System.out.println();
            System.out.println("Salir:");
            System.out.println("exit");
            System.out.println("======================================\n");

            while (true) {

                System.out.print("> ");

                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {

                    client.send("exit");

                    client.stop();

                    break;
                }

                if (!input.isEmpty()) {

                    client.send(input);
                }
            }

        } catch (Exception e) {

            System.out.println("Error en cliente UDP");

        } finally {

            scanner.close();
        }
    }
}