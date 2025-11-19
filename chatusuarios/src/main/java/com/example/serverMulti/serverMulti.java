package com.example.serverMulti;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class serverMulti {
    
    static HashMap<String, unCliente> clientes = new HashMap <>();

    public static void notificarTodos(String mensaje, unCliente excluir){
        for(unCliente cliente : clientes.values()){
            if(cliente!=excluir){
                try{
                    cliente.salida.writeUTF(mensaje);
                }catch(IOException e){}
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket servidorSocket = new ServerSocket(8080);
        int c = 0;  

        System.out.println("     SERVIDOR CHAT MULTIUSUARIO     \n");
        try {
            java.net.ServerSocket servidor = new java.net.ServerSocket(puerto);
            System.out.println("Servidor iniciado en el puerto " + puerto);
            System.out.println("Esperando conexiones de clientes...\n");
            while (true) {
                java.net.Socket socketCliente = servidor.accept();
                unCliente nuevoCliente = new unCliente(socketCliente);
                Thread hiloCliente = new Thread(nuevoCliente);
                hiloCliente.start();
            }
        } catch (IOException e) {
            System.err.println("ERROR AL INICIAR EL SERVIDOR");
            System.err.println(String.format("%-42s", e.getMessage()));
            System.exit(1);
        }
    }    

}
