package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMensajes {
    public static void main(String[] args) {
        int puerto = 8080;

        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado en el puerto " + puerto);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket);
                new Thread(new ManejadorCliente(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ManejadorCliente implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    
}