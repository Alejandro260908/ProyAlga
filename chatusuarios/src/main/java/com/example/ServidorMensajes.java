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

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            out.println("Bienvenido al servidor de mensajería");
            String linea;

            while ((linea = in.readLine()) != null) {
                String[] partes = linea.split(";", 4);

                switch (partes[0]) {
                    case "REGISTRAR":
                        if (ChatUsuarios.registrarUsuario(partes[1], partes[2])) {
                            out.println("Usuario registrado");
                        } else {
                            out.println("Usuario ya existe");
                        }
                        break;

                    case "LOGIN":
                        if (ChatUsuarios.autenticar(partes[1], partes[2])) {
                            out.println("Login exitoso");
                        } else {
                            out.println("Usuario o contraseña incorrectos");
                        }
                        break;

                    case "ENVIAR":
                        ChatUsuarios.enviarMensaje(partes[1], partes[2], partes[3]);
                        out.println("Mensaje enviado");
                        break;

                    
                }
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado");
        }
    }
}