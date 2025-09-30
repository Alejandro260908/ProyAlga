package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteMensajes {
    public static void main(String[] args) {
        String host = "localhost";
        int puerto = 8080;

        try (Socket socket = new Socket(host, puerto);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println(in.readLine());

            String comando;
            while (true) {
                System.out.print("Comando: ");
                comando = sc.nextLine();
                out.println(comando);

                String respuesta = in.readLine();
                System.out.println("Servidor: " + respuesta.toUpperCase());

                if (comando.startsWith("SALIR")) {
                    break;
                }else if (comando.startsWith("DESCARGAR;")) {
                        out.println(comando);
                        StringBuilder contenidoArchivo = new StringBuilder();

                        while ((respuesta = in.readLine()) != null && !respuesta.equals("EOF")) {
                            contenidoArchivo.append(respuesta).append("\n");
                        }

                        // Guardar en carpeta local del cliente
                        String[] partes = comando.split(";");
                        String usuarioOrigen = partes[1];
                        String nombreArchivo = partes[2];

                        File carpetaDestino = new File("archivos_locales/" + usuarioOrigen);
                        if (!carpetaDestino.exists()) carpetaDestino.mkdirs();

                        File archivoDestino = new File(carpetaDestino, nombreArchivo);

                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoDestino))) {
                            bw.write(contenidoArchivo.toString());
                        }

                        System.out.println("✅ Archivo guardado en: " + archivoDestino.getAbsolutePath());
                    }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
