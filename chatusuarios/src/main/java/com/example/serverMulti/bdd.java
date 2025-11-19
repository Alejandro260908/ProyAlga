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

    private static void crearTB() throws SQLException{
        String sql = """
                CREATE TABLE IF NOT EXISTS ban(
                id SERIAL PRIMARY KEY,
                bloqueador varchar(50) NOT NULL,
                baneado varchar(50) NOT NULL,
                fecha_ban TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                unique(bloqueador, baneado),
                FOREIGN KEY (bloqueador) REFERENCES usuarios(username) ON DELETE CASCADE,
                FOREIGN KEY (baneado) REFERENCES usuarios(username) ON DELETE CASCADE
                )
                """;
        try(Statement stmt = conexion.createStatement()){
            stmt.execute(sql);
            System.out.println("Tabla ban creada");
        }
    }

    public static boolean rU(String username, String password){
        if(conexion == null){
            return SisAutenticacion.rUOffline(username,password);
        }

        String sql = "INSERT INTO usuarios(username, password) VALUES(?,?)";

        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setString(1,username);
            pstmt.setString(2,password);

            int rowsAffected = pstmt.executeUpdate();
            
            if(rowsAffected > 0){
                inicializarRanking(username);
                return true;
            }
            return false;
        }catch(SQLException e){
            if(e.getSQLState().equals("23505")){
                return false;
            }
            System.err.println("Error al registrar el usuario: "+e.getMessage());
            return false;
        }
    }

    private static void inicializarRanking(String username){
        String sql = "INSERT INTO ranking(username, victorias, empates, derrotas, puntos) VALUES(?,0,0,0,0)";
        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setString(1,username);
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.err.println("Error al inicializar ranking: "+e.getMessage());
        }
    }

    public static boolean validarLogin(String username, String password){
        if(conexion == null){
            return SisAutenticacion.vLOffline(username,password);
        }

        String sql = "SELECT password FROM usuarios WHERE username = ?";

        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setString(1,username);

            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    String contraGuardada = rs.getString("password");
                    return password.equals(contraGuardada);
                }
                return false;
            }
        }catch(SQLException e){
            System.err.println("Error al validar el login: "+e.getMessage());
            return false;
        }
    }

    public static boolean existeUsuario(String username){
        if(conexion == null){
            return SisAutenticacion.eUOffline(username);
        }

        String sql = "SELECT 1 FROM usuarios WHERE username = ?";

        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setString(1,username);

            try(ResultSet rt = pstmt.executeQuery()){
                return rt.next();
            }
        }catch(SQLException e ){
            System.err.println("Error al obtener el usuario: "+e.getMessage());
            return false;
        }
    }

    public static boolean eliminarUsuario(String username){
        if(conexion == null) {
            System.err.println("No hay conexion a la base de datos, no se puede eliminar el usuario: " + username);
            return false;
        }

        try{
            conexion.setAutoCommit(false);
            String sqlRanking = "DELETE FROM ranking WHERE username = ?";
            try(PreparedStatement ps = conexion.prepareStatement(sqlRanking)){
                ps.setString(1,username);
                ps.executeUpdate();
            }

            String sqlMensajes = "DELETE FROM mensajes_grupo WHERE username = ?";
            try(PreparedStatement ps = conexion.prepareStatement(sqlMensajes)) {
                ps.setString(1,username);
                ps.executeUpdate();
            }

            String sqlBloqueos = "DELETE FROM bloqueos WHERE bloqueador = ? OR baneado = ?";
            try(PreparedStatement ps = conexion.prepareStatement(sqlBloqueos)) {
                ps.setString(1,username);
                ps.setString(2,username);
                ps.executeUpdate();
            }

            String sqlUsuario = "DELETE FROM usuarios WHERE username = ?";
            try(PreparedStatement ps = conexion.prepareStatement(sqlUsuario)) {
                ps.setString(1,username);
                int filasAfectadas = ps.executeUpdate();

                if(filasAfectadas > 0){
                    conexion.commit();
                    System.out.println("Usuario " + username + " eliminado correctamente");
                    return true;
                }else{
                    conexion.rollback();
                    return false;
            }

            }

        }catch(SQLException e){
            System.err.println("Error al eliminar el usuario: " + e.getMessage());
            try{
                conexion.rollback();
            }catch(SQLException ex){
                System.err.println("Error en el rollback: " + ex.getMessage());
            }
            return false;
        }finally{
            try{
                conexion.setAutoCommit(true);
            }catch(SQLException ex){
                System.err.println("Error al establecer autocommit = true: " + ex.getMessage());
            }
        }
    }

    public static boolean bloquearUsuario(String bloqueador, String bloqueado){
        if(conexion == null) {
            System.err.println("No hay conexion a la base de datos, no se puede bloquear el usuario: " + bloqueado);
            return false;
        }

        if(bloqueador.equals(bloqueado)){
            return false;
        }

        String sql = "INSERT INTO ban(bloqueador, baneado) VALUES(?,?) ON CONFLICT DO NOTHING";
        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setString(1,bloqueador);
            pstmt.setString(2,bloqueado);
            return pstmt.executeUpdate() > 0;
        }catch(SQLException e){
            System.err.println("Error al bloquear el usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean desbloquearUsuario(String bloqueador, String baneado){
        if(conexion == null) {
            System.err.println("No hay conexion a la base de datos, no se puede desbloquear el usuario: " + bloqueado);
            return false;
        }

        String sql = "DELETE FROM ban WHERE bloqueador = ? AND baneado = ?";
        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setString(1,bloqueador);
            pstmt.setString(2,baneado);
            return pstmt.executeUpdate() > 0;
        }catch(SQLException e){
            System.err.println("Error al desbloquear el usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean bloqueado(String usuario1, String usuario2){
        if(conexion == null) {
            return false;
        }

        String sql = "SELECT 1 FROM ban WHERE (bloqueador = ? AND baneado = ?) OR (bloqueador = ? AND baneado = ?)";
        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setString(1,usuario1);
            pstmt.setString(2,usuario2);
            pstmt.setString(3,usuario2);
            pstmt.setString(4,usuario1);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }catch (SQLException e){
            System.err.println("Error al verificar bloqueo");
            return false;
        }
    }
}
