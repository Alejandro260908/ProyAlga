package com.example.serverMulti;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
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
            salida.writeUTF("- Escribe '/j <NombreUsuario>' para invitar a jugar al gato");
            salida.writeUTF("- Escribe '/aceptar' para aceptar una invitacion");
            salida.writeUTF("- Escribe '/rechazar' para rechazar una invitacion");
            salida.writeUTF("- Durante el juego escribe el comando: '/mover <fila> <columna>' (ejemplo: /mover 0 1)");

        } catch (IOException e) {
            System.out.println("Error al enviar mensaje de bienvenida a " + usuario);
        }

        while(true){
            try {
                mensaje = entrada.readUTF();

                // Comando de registro
                if(mensaje.startsWith("/registro")) {
                    String[] partes = mensaje.split(" ");
                    if (partes.length == 3) {
                        String resultado = SisAutenticacion.procesarRegistro(usuario, partes[1], partes[2]);
                        salida.writeUTF(resultado);
                        if (resultado.startsWith("EXITO")) {
                            serverMulti.notificarTodos("*** " + usuario + " ahora es " + partes[1] + "  ***", this);
                        }
                    } else {
                        salida.writeUTF("El formato es /registro usuario contrase;a");
                    }
                    continue;
                }
                
                // Comando de login
                if(mensaje.startsWith("/login ")){
                    String[] parteslog = mensaje.split(" ");
                    if(parteslog.length == 3){
                        String resultado = SisAutenticacion.procesarLogin(usuario, parteslog[1], parteslog[2]);
                        salida.writeUTF(resultado);
                        if(resultado.startsWith("EXITO")){
                            serverMulti.notificarTodos("*** " + usuario + " ahora es " + parteslog[1] + "  ***",this);
                        }
                    }else{
                        salida.writeUTF("El formato es /login usuario contrase;a");
                    }
                    continue;
                }

                if(mensaje.equals("/logout")){
                    String nombreAnterior = SisAutenticacion.getNombreUsuarioReal(usuario);
                    String resultado = SisAutenticacion.procesarLogout(usuario);
                    salida.writeUTF(resultado);
                    if(resultado.startsWith("EXITO")){
                        serverMulti.notificarTodos("*** " + nombreAnterior + " se ha desconectado ***",this);
                        serverMulti.notificarTodos("*** " + usuario + " se ha conectado ***",this);
                    }
                    continue;
                }

                // Comando para ver usuarios conectados
                if(mensaje.equals("/usuarios")) {
                    StringBuilder usuarios = new StringBuilder("Usuarios conectados: ");
                    for (String user : serverMulti.clientes.keySet()) {
                        usuarios.append(SisAutenticacion.getNombreDisplay(user)).append(", ");
                    }
                    salida.writeUTF(usuarios.toString());
                    continue;
                }

                if(mensaje.equals("/ranking")){
                    List<String> ranking = bdd.obtenerRanking();
                    for(String linea : ranking){
                        salida.writeUTF(linea);
                    }
                    continue;
                }

                if(mensaje.startsWith("/vs ")){
                    String[] partes = mensaje.split(" ");
                    if(partes.length != 3){
                        salida.writeUTF("Uso: /vs jugador1 jugador2");
                        continue;
                    }
                    String estadisticas = bdd.obtenerRankingsVs(partes[1], partes[2]);
                    salida.writeUTF(estadisticas);
                    continue;
                }
                

                //comando para borrar usuarios
                if(mensaje.startsWith("/eliminarcuenta")){
                    String[] partes = mensaje.split(" ",2);

                    if(partes.length < 2 || !partes[1].equals("CONFIRMAR")){
                        salida.writeUTF("ADVERTENCIA: Esta accion eliminara tu cuenta permanentemente. \n" +
                                "si estas seguro, escribe: /eliminarcuenta CONFIRMAR");
                    }else{
                        String nombreAnterior = SisAutenticacion.getNombreUsuarioReal(usuario);
                        String resultado = SisAutenticacion.procesarEliminarCuenta(usuario);
                        salida.writeUTF(resultado);
                        if(resultado.startsWith("EXITO")){
                            salida.writeUTF("Ahora eres " + usuario + " con 3 mensajes gratuitos");
                            serverMulti.notificarTodos("*** " + nombreAnterior + " se ha desconectado ***",this);

                        }
                    }
                    continue;

                }


                // Comando para bloquear usuario
                if(mensaje.startsWith("/bloquear ")){
                    String[] partes = mensaje.split(" ", 2);
                    if(partes.length < 2){
                        salida.writeUTF("Uso: /bloquear NombreUsuario");
                        continue;
                    }

                    if(!SisAutenticacion.estaAutenticado(usuario)){
                        salida.writeUTF("ERROR: Debes iniciar sesion para bloquear usuarios");
                        continue;
                    }

                    String usuarioABloquear = partes[1].trim();
                    String miUsuario = SisAutenticacion.getNombreUsuarioReal(usuario);

                    if(usuarioABloquear.equals(miUsuario)){
                        salida.writeUTF("ERROR: No puedes bloquearte a ti mismo");
                        continue;
                    }

                    if(!bdd.existeUsuario(usuarioABloquear)){
                        salida.writeUTF("ERROR: El usuario " + usuarioABloquear + " no existe");
                        continue;
                    }

                    if(bdd.yoBloquee(miUsuario, usuarioABloquear)){
                        salida.writeUTF("Ya has bloqueado a " + usuarioABloquear);
                        continue;
                    }

                    if(bdd.bloquearUsuario(miUsuario, usuarioABloquear)){
                        salida.writeUTF("Has bloqueado a " + usuarioABloquear);
                    } else {
                        salida.writeUTF("ERROR: No se pudo bloquear al usuario");
                    }
                    continue;
                }

                // Comando para desbloquear usuario
                if(mensaje.startsWith("/desbloquear ")){
                    String[] partes = mensaje.split(" ", 2);
                    if(partes.length < 2){
                        salida.writeUTF("Uso: /desbloquear NombreUsuario");
                        continue;
                    }

                    if(!SisAutenticacion.estaAutenticado(usuario)){
                        salida.writeUTF("ERROR: Debes iniciar sesion para desbloquear usuarios");
                        continue;
                    }

                    String usuarioADesbloquear = partes[1].trim();
                    String miUsuario = SisAutenticacion.getNombreUsuarioReal(usuario);

                    if(!bdd.yoBloquee(miUsuario, usuarioADesbloquear)){
                        salida.writeUTF("No has bloqueado a " + usuarioADesbloquear);
                        continue;
                    }

                    if(bdd.desbloquearUsuario(miUsuario, usuarioADesbloquear)){
                        salida.writeUTF("Has desbloqueado a " + usuarioADesbloquear);
                    } else {
                        salida.writeUTF("ERROR: No se pudo desbloquear al usuario");
                    }
                    continue;
                }

                // Comando para ver usuarios bloqueados
                if(mensaje.equals("/bloqueados")){
                    if(!SisAutenticacion.estaAutenticado(usuario)){
                        salida.writeUTF("ERROR: Debes iniciar sesion para ver tu lista de bloqueados");
                        continue;
                    }

                    String miUsuario = SisAutenticacion.getNombreUsuarioReal(usuario);
                    List<String> bloqueados = bdd.obtenerBloqueados(miUsuario);

                    if(bloqueados.isEmpty()){
                        salida.writeUTF("No has bloqueado a ningun usuario");
                    } else {
                        salida.writeUTF("=== USUARIOS BLOQUEADOS ===");
                        for(String bloqueado : bloqueados){
                            salida.writeUTF("- " + bloqueado);
                        }
                    }
                    continue;
                }

                // Comando para invitar a jugar
                if(mensaje.startsWith("/jugar ")){
                    String[] partes = mensaje.split(" ", 2);
                    if(partes.length < 2){
                        salida.writeUTF("Uso: /jugar NombreUsuario");
                        continue;
                    }
                    
                    
                    String nombreDestino = partes[1].trim();
                    unCliente destinatario = buscarClientePorNombre(nombreDestino);

                    if(destinatario == null){
                        salida.writeUTF("Usuario no encontrado: " + nombreDestino);
                    } else if(destinatario.usuario.equals(usuario)){
                        salida.writeUTF("No puedes jugar contigo mismo");
                    } else {
                        // Verificar bloqueos
                        String miUsuario = SisAutenticacion.getNombreUsuarioReal(usuario);
                        String otroUsuario = SisAutenticacion.getNombreUsuarioReal(destinatario.usuario);

                        if(miUsuario != null && otroUsuario != null && bdd.bloqueado(miUsuario, otroUsuario)){
                            if(bdd.yoBloquee(miUsuario, otroUsuario)){
                                salida.writeUTF("No puedes enviar invitacion a " + nombreDestino + " porque lo has bloqueado");
                            } else {
                                salida.writeUTF("El usuario " + nombreDestino + " no esta disponible");
                            }
                        } else {
                            juegos.enviarInvitacion(usuario, destinatario.usuario, this, destinatario);
                        }
                    }
                    continue;
                }
                
                // Comando para aceptar invitación
                if(mensaje.equals("/aceptar")){
                    juegos.aceptarInvitacion(usuario, this);
                    continue;
                }
                
                // Comando para rechazar invitación
                if(mensaje.equals("/rechazar")){
                    juegos.rechazarInvitacion(usuario, this);
                    continue;
                }
                
                // Comando para hacer un movimiento en el juego
                if(mensaje.startsWith("/mover ")){
                    String[] partes = mensaje.split(" ");
                    if(partes.length != 3){
                        salida.writeUTF("Uso: /mover fila columna (ejemplo: /mover 0 1)");
                        continue;
                    }
                    
                    try{
                        int fila = Integer.parseInt(partes[1]);
                        int columna = Integer.parseInt(partes[2]);
                        juegos.procesarMovimiento(usuario, fila, columna, this);
                    } catch(NumberFormatException e){
                        salida.writeUTF("Fila y columna deben ser numeros del 0 al 2");
                    }
                    continue;
                }
                
                // Verificar límite de mensajes para invitados
                if(!SisAutenticacion.puedeEnviarMensajes(usuario)){
                    salida.writeUTF("Has alcanzado el limite de 3 mensajes gratuitos");
                    salida.writeUTF("Registrate (/registro) o inicia sesion (/login) para enviar mensajes ilimitados");
                    salida.writeUTF("Puedes seguir viendo los mensajes de los demas usuarios");
                    continue;
                }
                
                // Mensaje privado
                if(mensaje.startsWith("@")){
                    String[] partes = mensaje.split(" ",2);
                    if(partes.length<2){
                        salida.writeUTF("Formato incorrecto, usa @NombreUsuario mensaje para enviar mensaje privado");
                        continue;
                    }
                    String aQuien = partes[0].substring(1);
                    String mensajePrivado = partes[1];
                    unCliente clienteDestino = buscarClientePorNombre(aQuien);

                    if(clienteDestino!=null){
                        // Verificar bloqueos
                        String miUsuario = SisAutenticacion.getNombreUsuarioReal(usuario);
                        String otroUsuario = SisAutenticacion.getNombreUsuarioReal(clienteDestino.usuario);

                        if(miUsuario != null && otroUsuario != null && bdd.bloqueado(miUsuario, otroUsuario)){
                            if(bdd.yoBloquee(miUsuario, otroUsuario)){
                                salida.writeUTF("No puedes enviar mensajes a " + aQuien + " porque lo has bloqueado");
                            } else {
                                salida.writeUTF("El usuario " + aQuien + " no esta disponible");
                            }
                        } else {
                            String nombreRemitente = SisAutenticacion.getNombreDisplay(usuario);
                            String mensajeFormateado = "[PRIVADO] " + nombreRemitente + " te dice: " + mensajePrivado;
                            clienteDestino.salida.writeUTF(mensajeFormateado);

                            salida.writeUTF("Mensaje privado enviado a " + SisAutenticacion.getNombreDisplay(clienteDestino.usuario));
                            int restantes = SisAutenticacion.incrementarMensajes(usuario);
                            if(restantes>=0){
                                salida.writeUTF("Te quedan " + restantes + " mensajes gratuitos");
                            }
                        }
                    }else{
                        salida.writeUTF("No se pudo enviar un mensaje privado");
                    }
                    continue;
                }
                
                
                int restantes = SisAutenticacion.incrementarMensajes(usuario);
                if(restantes>=0){
                    salida.writeUTF("Te quedan " + restantes + " mensajes gratuitos");
                }

            } catch (IOException e) {
                String nombreDisplay = SisAutenticacion.getNombreDisplay(usuario);
                
                // Determinar tipo de error
                String tipoError = "desconexión inesperada";
                if(e instanceof EOFException){
                    tipoError = "cliente cerró la conexión";
                } else if(e instanceof SocketException){
                    if(e.getMessage().contains("Connection reset")){
                        tipoError = "conexión perdida (red interrumpida)";
                    } else if(e.getMessage().contains("Software caused connection abort")){
                        tipoError = "conexión abortada (problema de red)";
                    } else if(e.getMessage().contains("Broken pipe")){
                        tipoError = "conexión rota (cliente desconectado)";
                    }
                }
                
                System.out.println("Cliente " + nombreDisplay + " se desconectó (" + tipoError + ")");

                // Manejar desconexión en juegos activos
                juegos.manejarDesconexion(usuario);
                

                SisAutenticacion.limpiarCliente(usuario);
                serverMulti.notificarTodos("*** " + SisAutenticacion.getNombreDisplay(usuario) + " se ha desconectado ***",this);
                serverMulti.clientes.remove(usuario);
                break;
            }

        }
    }

    private unCliente buscarClientePorNombre(String nombre){
        unCliente cliente = serverMulti.clientes.get(nombre);
        if(cliente!=null){
            return cliente;
        }
        for(unCliente c : serverMulti.clientes.values()){
            if(SisAutenticacion.getNombreDisplay(c.usuario).equals(nombre)){
                return c;
            }
        }
        return null;
    }
    }

