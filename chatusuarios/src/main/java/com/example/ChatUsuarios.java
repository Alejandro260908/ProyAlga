package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Mensaje {
    String remitente;
    String destinatario;
    String texto;

    public Mensaje(String remitente, String destinatario, String texto) {
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.texto = texto;
    }

    @Override
    public String toString() {
        return remitente + "->" + destinatario + ":" + texto;
    }

    public static Mensaje fromString(String linea) {
        String[] partes = linea.split("->");
        String remitente = partes[0];
        String[] partes2 = partes[1].split(":", 2);
        String destinatario = partes2[0];
        String texto = partes2[1];
        return new Mensaje(remitente, destinatario, texto);
    }
}

public class ChatUsuarios {

    static final String ARCHIVO_MENSAJES = "mensajes.txt";
    static final String ARCHIVO_USUARIOS = "usuarios.txt";

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al aplicar hash", e);
        }
    }

    public static boolean registrarUsuario(String usuario, String contrasena) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":");
                if (partes[0].equals(usuario)) {
                    return false;
                }
            }
        } catch (IOException e) {}

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_USUARIOS, true))) {
            String hash = hashPassword(contrasena);
            bw.write(usuario + ":" + hash);
            bw.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean autenticar(String usuario, String contrasena) {
        String hashIngresado = hashPassword(contrasena);
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":");
                if (partes[0].equals(usuario) && partes[1].equals(hashIngresado)) {
                    return true;
                }
            }
        } catch (IOException e) {}
        return false;
    }

    public static void enviarMensaje(String remitente, String destinatario, String texto) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_MENSAJES, true))) {
            Mensaje m = new Mensaje(remitente, destinatario, texto);
            bw.write(m.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String leerMensajes(String usuario) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_MENSAJES))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                Mensaje m = Mensaje.fromString(linea);
                if (m.destinatario.equals(usuario) || m.remitente.equals(usuario)) {
                    sb.append(m.remitente).append(" -> ").append(m.destinatario)
                      .append(": ").append(m.texto).append("\n");
                }
            }
        } catch (IOException e) {
            sb.append("⚠️ No hay mensajes todavía.\n");
        }
        return sb.toString();
    }
}