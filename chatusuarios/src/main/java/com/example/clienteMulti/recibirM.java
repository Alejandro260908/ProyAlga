package com.example.clienteMulti;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class recibirM implements Runnable {
    private Socket socket;
    final DataInputStream entrada;
    
    public recibirM(Socket socket) throws IOException {
        this.socket = socket;
        entrada = new DataInputStream(socket.getInputStream());
    }
    
    @Override
    public void run() {
        String mensaje;
        while (true) {
            try {
                mensaje = entrada.readUTF();
                System.out.println(mensaje);
            } catch (SocketException e){
                if(e.getMessage().contains("Connection reset") || 
                   e.getMessage().contains("Software caused connection abort") ||
                   e.getMessage().contains("Broken pipe") ||
                   e.getMessage().contains("Connection closed")){
                    System.err.println("\n CONEXIÓN PERDIDA CON EL SERVIDOR");
                    System.err.println("No se pueden recibir más mensajes");
                    System.err.println("Se perdió la conexión de red║");
                    System.err.println("La aplicación se cerrará");
                } else {
                    System.err.println("\nERROR DE RED");
                    System.err.println(String.format("%-42s", e.getMessage()));
                    System.err.println("La aplicación se cerrará");
                }
                cerrarRecursos();
                System.exit(0);
                break;
            } catch (EOFException e) {
                System.err.println("\nEL SERVIDOR HA CERRADO LA CONEXIÓN");
                System.err.println("No se pueden recibir más mensajes");
                System.err.println("La aplicación se cerrará");
                cerrarRecursos();
                System.exit(0);
                break;
            } catch (IOException e) {
                System.err.println("\nERROR DE COMUNICACIÓN");
                System.err.println("No se pudo recibir el mensaje");
                System.err.println("La aplicación se cerrará");
                cerrarRecursos();
                System.exit(0);
                break;
            }
        }
    }
    
    private void cerrarRecursos() {
        try {
            if (entrada != null) {
                entrada.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignorar
        }
    }
    
}
