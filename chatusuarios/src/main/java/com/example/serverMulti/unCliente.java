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
        String mensaje;
        try {

            salida.writeUTF("¡Bienvenido! : " + usuario);
            salida.writeUTF("Puedes mandar hasta 3 mensajes como invitado pero puedes iniciar sesion para enviar mas mensajes");
            salida.writeUTF("Introduce cualquiera de los siguientes comandos:");
            salida.writeUTF("- Escribe un mensaje para enviarlo a todos");
            salida.writeUTF("- Escribe '@NombreUsuario <mensaje>' si se envia un mensaje privado");
            salida.writeUTF("- Escribe '/u' para ver usuarios conectados");
            salida.writeUTF("- Escribe '/r' para ver el ranking de jugadores");
            salida.writeUTF("- Escribe '/vs <j1> <j2>' para ver estadisticas entre dos jugadores");
            salida.writeUTF("- Escribe '/re <usuario> <contrasena>' para registrarte");
            salida.writeUTF("- Escribe '/login <usuario> <contrasena>' para iniciar sesion");
            salida.writeUTF("- Escribe '/logout' para cerrar sesion");
            salida.writeUTF("- Escribe '/ec' para eliminar tu cuenta permanentemente");
            salida.writeUTF("- Escribe '/ban <NombreUsuario' para bloquear a un usuario");
            salida.writeUTF("- Escribe '/unban <NombreUsuario>' para desbloquear a un usuario");
            salida.writeUTF("- Escribe '/banned' para ver tu lista de usuarios bloqueados");
            salida.writeUTF("- Escribe '/cg <NombreGrupo>' para crear un grupo");
            salida.writeUTF("- Escribe '/ug <NombreGrupo>' para unirse a un grupo");
            salida.writeUTF("- Escribe '/eg <NombreGrupo>' para eliminar un grupo");
            salida.writeUTF("- Escribe '/g' para ver todos los grupos");
            salida.writeUTF("- Escribe '/j <NombreUsuario>' para invitar a jugar al gato");
            salida.writeUTF("- Escribe '/aceptar' para aceptar una invitacion");
            salida.writeUTF("- Escribe '/rechazar' para rechazar una invitacion");
            salida.writeUTF("- Durante el juego escribe el comando: '/mover <fila> <columna>' (ejemplo: /mover 0 1)");

        } catch (IOException e) {
            System.out.println("Error al enviar mensaje de bienvenida a " + usuario);
        }
    }
}
