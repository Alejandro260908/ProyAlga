package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteMensajes {
    public static void main(String[] args) {
        String host = "localhost";
        int puerto = 8080;

        try (Socket socket = new Socket(host, puerto);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println(in.readLine());

            String comando;
            while (true) {
                System.out.print("Comando: ");
                comando = sc.nextLine();
                out.println(comando);

                String respuesta = in.readLine();
                System.out.println("Servidor: " + respuesta.toUpperCase());

                if (comando.startsWith("SALIR")) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
