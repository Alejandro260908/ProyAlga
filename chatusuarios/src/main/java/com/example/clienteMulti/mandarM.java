package com.example.clienteMulti;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

public class mandarM implements Runnable {
    private Socket socket;
    final DataOutputStream salida;
    final BufferedReader teclado;
    
    public mandarM(Socket socket) throws IOException {
        this.socket = socket;
        salida = new DataOutputStream(socket.getOutputStream());
        teclado = new BufferedReader(new InputStreamReader(System.in));
    }
    
    @Override
    public void run() {
        String mensaje;
        while (true) {
            try {
                mensaje = teclado.readLine();
                if(mensaje == null || mensaje.equals("/salir")){
                    System.out.println("Desconectándose del servidor...");
                    cerrarRecursos();
                    System.exit(0);
                    break;
                }
                salida.writeUTF(mensaje);
            } catch (SocketException e){
                if(e.getMessage().contains("Connection reset") || 
                   e.getMessage().contains("Software caused connection abort") ||
                   e.getMessage().contains("Broken pipe") ||
                   e.getMessage().contains("Connection closed")){
                    System.err.println("\n CONEXIÓN PERDIDA CON EL SERVIDOR");
                    System.err.println("No se puede enviar el mensaje");
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
            } catch (IOException e) {
                System.err.println("\nERROR DE COMUNICACIÓN");
                System.err.println("No se pudo enviar el mensaje");
                System.err.println("La aplicación se cerrará");
                cerrarRecursos();
                System.exit(0);
                break;
            }
        }
    }
    
    private void cerrarRecursos() {
        try {
            if (salida != null) {
                salida.close();
            }
            if (teclado != null) {
                teclado.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignorar errores al cerrar recursos
        }
    }
}