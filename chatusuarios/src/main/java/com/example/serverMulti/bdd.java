package com.example.serverMulti;

import java.sql.*;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.*;

public class bdd {
    private static String BDD_URL = "jdbc:postgresql://localhost:5432/chat_db";
    private static String BDD_USER = "postgres";
    private static String BDD_PASSWORD = "ElPsyCongroo123";
    private static Connection conexion = null;

    public static void inicializarConexion() {
        try {
            cargarConfig();

            Class.forName("org.postgresql.Driver");

            conexion = DriverManager.getConnection(BDD_URL, BDD_USER, BDD_PASSWORD);

            crearTU();
            crearTR();

            System.out.println("Conexión a la base de datos establecida.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            conexion = null;
        }
    }

    private static void crearTU() throws SQLException{
        String sql = """
                CREATE TABLE IF NOT EXISTS usuarios(
                id SERIAL PRIMARY KEY,
                username varchar(50) UNIQUE NOT NULL,
                password varchar(255) NOT NULL
                )
                """;

        try(Statement stmt = conexion.createStatement()){
            stmt.execute(sql);
            System.out.println("Tabla usuarios creada");
        }
    }

    private static void crearTR() throws SQLException{
        String sql = """
                CREATE TABLE IF NOT EXISTS ranking(
                id SERIAL PRIMARY KEY,
                username varchar(50) UNIQUE NOT NULL,
                victorias INT DEFAULT 0,
                empates INT DEFAULT 0,
                derrotas INT DEFAULT 0,
                puntos INT DEFAULT 0,
                FOREIGN KEY (username) REFERENCES usuarios(username) ON DELETE CASCADE
                )
                """;

        try(Statement stmt = conexion.createStatement()){
            stmt.execute(sql);
            System.out.println("Tabla ranking creada");
        }
        
    }
}
