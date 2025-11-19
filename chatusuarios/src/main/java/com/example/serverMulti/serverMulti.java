package com.example.serverMulti;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
        bdd.inicializarConexion();
        
        ServerSocket servidorSocket = new ServerSocket(8080);
        int contador = 0;
        if(bdd.estaConectado()){
            System.out.println("Base de datos conectada. Los usuarios se guardaran permanentemente");
        }else{
            System.out.println("Modo offline. Los usuarios se guardaran solo en memoria");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bdd.cerrarConexion();
        }));

        while (true){
            Socket s = servidorSocket.accept();
            String clienteUsuario = "Usuario" + contador;
            unCliente Cliente = new unCliente(clienteUsuario,s);
            Thread hilo = new Thread(Cliente);
            clientes.put(clienteUsuario, Cliente);
            
            
            hilo.start();
            System.out.println("Se conectó el "+clienteUsuario);
            contador++;
        }
    }  

}
