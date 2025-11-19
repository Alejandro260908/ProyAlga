package com.example.clienteMulti;

import java.io.IOException;
import java.net.Socket;
import java.net.ConnectException;

public class clienteMulti {

    public static void main(String[] args) {
        int maxI = 5;
        int numIntento = 0;
        Socket s = null;
        
        System.out.println("     CLIENTE CHAT        \n");
        
        while (numIntento < maxI && s == null) {
            try {
                System.out.println("Intentando conectar al servidor... (Intento " + (numIntento + 1) + "/" + maxI + ")");
                s = new Socket("localhost", 8080);
                System.out.println("¡Conectado exitosamente al servidor!");
            } catch (ConnectException e) {
                numIntento++;
                if (numIntento < maxI) {
                    System.out.println("No se pudo conectar. Reintentando en 2 segundos...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("\nERROR: NO SE PUDO CONECTAR AL SERVIDOR");
                    System.err.println("Después de " + maxI + " intentos");
                    System.err.println("");
                    System.err.println("Verifica que:");
                    System.err.println("1. ServidorMulti esté ejecutándose");
                    System.err.println("2. El puerto 8080 esté disponible");
                    System.err.println("3. Tu firewall permita la conexión");
                    System.exit(1);
                }
            } catch (IOException e) {
                System.err.println("\nERROR DE CONEXIÓN");
                System.err.println(String.format("%-42s", e.getMessage()));
                System.exit(1);
            }
        }
        
        try {
            final Socket socketFinal = s;
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if(socketFinal != null && !socketFinal.isClosed()){
                        socketFinal.close();
                    }
                } catch (IOException e) {
                }
            }));
            
            mandarM mandar = new mandarM(s);
            Thread hiloParaMandar = new Thread(mandar);
            hiloParaMandar.start();
            
            recibirM recibir = new recibirM(s);
            Thread hiloParaRecibir = new Thread(recibir);
            hiloParaRecibir.start();
            
        } catch (IOException e) {
            System.err.println("\n");
            System.err.println("ERROR AL INICIAR COMUNICACIÓN");
            System.err.println(String.format("%-42s", e.getMessage()));
            System.exit(1);
        }
    }
}
