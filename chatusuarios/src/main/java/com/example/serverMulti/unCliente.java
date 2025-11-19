package com.example.serverMulti;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
;

public class unCliente implements Runnable {
    final DataOutputStream salida;
    final DataInputStream entrada;
    final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
    private final String usuario;

    public unCliente(String usuario, Socket socket) throws IOException {
        salida = new DataOutputStream(socket.getOutputStream());
        entrada = new DataInputStream(socket.getInputStream());
        this.usuario = usuario;
    }

    public String getUsuario() {
        return usuario;
    }

    @Override
    public void run() {
        // Lógica del cliente
    }
}
