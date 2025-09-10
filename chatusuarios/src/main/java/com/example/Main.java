package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

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

public class Main {

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
                    System.out.println("El usuario ya existe.");
                    return false;
                }
            }
        } catch (IOException e) {
            // si no existe el archivo, se creará después
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_USUARIOS, true))) {
            String hash = hashPassword(contrasena);
            bw.write(usuario + ":" + hash);
            bw.newLine();
            System.out.println("Usuario registrado con éxito.");
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
        } catch (IOException e) {
            System.out.println("No hay usuarios registrados aún.");
        }
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

    public static void leerMensajes(String usuario) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_MENSAJES))) {
            String linea;
            System.out.println("📩 Mensajes de/para " + usuario + ":");
            while ((linea = br.readLine()) != null) {
                Mensaje m = Mensaje.fromString(linea);
                if (m.destinatario.equals(usuario) || m.remitente.equals(usuario)) {
                    System.out.println(m.remitente + " -> " + m.destinatario + ": " + m.texto);
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ No hay mensajes todavía.");
        }
    }

public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n=== Sistema de Mensajes Seguro ===");
            System.out.println("1. Registrar usuario");
            System.out.println("2. Iniciar sesión");
            System.out.println("3. Salir");
            System.out.print("Elige una opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1:
                    System.out.print("Nuevo usuario: ");
                    String nuevoUsuario = sc.nextLine();
                    System.out.print("Contraseña: ");
                    String nuevaContrasena = sc.nextLine();
                    registrarUsuario(nuevoUsuario, nuevaContrasena);
                    break;

                case 2:
                    System.out.print("Usuario: ");
                    String usuario = sc.nextLine();
                    System.out.print("Contraseña: ");
                    String contrasena = sc.nextLine();

                    if (autenticar(usuario, contrasena)) {
                        System.out.println("Sesión iniciada correctamente.");
                        menuUsuario(usuario, sc);
                    } else {
                        System.out.println("Usuario o contraseña incorrectos.");
                    }
                    break;

                case 3:
                    System.out.println("Cerrando el programa...");
                    break;
            }

        } while (opcion != 3);

        sc.close();
    }

    public static void menuUsuario(String usuario, Scanner sc) {
        int opcion;
        do {
            System.out.println("\n=== Menú de " + usuario + " ===");
            System.out.println("1. Enviar mensaje");
            System.out.println("2. Leer mis mensajes");
            System.out.println("3. Cerrar sesión");
            System.out.print("Elige una opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1:
                    System.out.print("Usuario destinatario: ");
                    String destinatario = sc.nextLine();
                    System.out.print("Escribe el mensaje: ");
                    String texto = sc.nextLine();
                    enviarMensaje(usuario, destinatario, texto);
                    System.out.println("Mensaje enviado.");
                    break;

                case 2:
                    leerMensajes(usuario);
                    break;

                case 3:
                    System.out.println("Sesión cerrada.");
                    break;
            }

        } while (opcion != 3);
    }
}
