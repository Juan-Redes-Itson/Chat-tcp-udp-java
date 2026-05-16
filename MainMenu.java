/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Clientes;

import java.util.Scanner;

public class MainMenu {

    public static void main(String[] args) {

        // Scanner para leer datos del teclado
        Scanner scanner = new Scanner(System.in);

        // Ciclo infinito del menu principal
        while (true) {

            // Mostrar menu
            System.out.println("================================");
            System.out.println("         CHAT CLIENTE");
            System.out.println("================================");
            System.out.println("1. Cliente TCP");
            System.out.println("2. Cliente UDP");
            System.out.println("3. Salir");
            System.out.print("Selecciona una opcion: ");

            // Leer opcion del usuario
            String opcion = scanner.nextLine();

            // Evaluar opcion seleccionada
            switch (opcion) {

                // Iniciar cliente TCP
                case "1":

                    try {

                        TCPClient.main(null);

                    } catch (Exception e) {

                        System.out.println("Error iniciando TCP");
                    }

                    break;

                // Iniciar cliente UDP
                case "2":

                    try {

                        UDPClient.main(null);

                    } catch (Exception e) {

                        System.out.println("Error iniciando UDP");
                    }

                    break;

                // Salir del programa
                case "3":

                    System.out.println("Saliendo...");

                    scanner.close();

                    System.exit(0);

                    break;

                // Opcion invalida
                default:

                    System.out.println("Opcion invalida");
            }
        }
    }
}
