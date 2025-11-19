package com.example.serverMulti;

import java.sql.Connection;

public class bdd {
    private static String BDD_URL = "jdbc:postgresql://localhost:5432/chat_db";
    private static String BDD_USER = "postgres";
    private static String BDD_PASSWORD = "ElPsyCongroo123";
    private static Connection conexion = null;

    public static void inicializarConexion() {
        try {
            cargarConfig();

            Class.forName("org.postgresql.Driver");

            crearTU();
            crearTR();

            System.out.println("Conexión a la base de datos establecida.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            conexion = null;
        }
    }
}
