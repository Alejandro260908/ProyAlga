package com.example.serverMulti;

import java.sql.*;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

public class bdd {
    private static String BDD_URL = "jdbc:postgresql://localhost:5432/chat_db";
    private static String BDD_USER = "postgres";
    private static String BDD_PASSWORD = "ElPsyCongroo123";
    private static Connection conexion;

    public static void inicializarConexion() throws SQLException {
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

    private static void cargarConfig(){
        try(FileInputStream fis = new FileInputStream("database.properties")){
            Properties prop = new Properties();
            prop.load(fis);
            BDD_URL = prop.getProperty("BDD_URL",BDD_URL);
            BDD_USER = prop.getProperty("BDD_USER",BDD_USER);
            BDD_PASSWORD = prop.getProperty("BDD_PASSWORD",BDD_PASSWORD);

            System.out.println("Configuracion cargada desde database.properties");
        }catch(IOException e){
            System.err.println("Usando la configuracion por defecto (database.properties no encontrado)");
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
            System.err.println("No hay conexion a la base de datos, no se puede desbloquear el usuario: " + baneado);
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

    public static List<String> obtenerBloqueados(String username){
        List<String> bloqueados = new ArrayList<>();
        if(conexion == null){
            return bloqueados;
        }

        String sql = "SELECT baneado FROM ban WHERE bloqueador = ? ORDER BY fecha_bloqueo DESC";
        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){
                bloqueados.add(rs.getString("baneado"));
            }
        }catch(SQLException e){
            System.err.println("Error al obtener usuarios bloqueados: " + e.getMessage());
        }

        return bloqueados;
    }

    public static void rVic(String ganador, String perdedor){
        if(conexion == null) {
            System.out.println("No se pueden registrar estadisticas en modo offline");
            return;
        }

        aEst(ganador, "victorias", 2);
        aEst(perdedor, "derrotas", 0);
    }

    public static void rEmp(String jugador1, String jugador2){
        if(conexion == null) {
            System.out.println("No se pueden registrar estadisticas en modo offline");
            return;
        }
        aEst(jugador1, "empates", 1);
        aEst(jugador2, "empates", 1);
    }

    private static void aEst(String username, String tipo, int puntos){
        String sql = "UPDATE ranking SET " + tipo + " = " + tipo + " + 1, puntos = puntos + ? WHERE username = ?";
        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setInt(1, puntos);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();

            if(rowsAffected == 0){
                System.err.println("No se encontraron estadisticas para el usuario: " + username);
            }
        }catch(SQLException e){
            System.err.println("Error actualizando rankings para " + username + ": " + e.getMessage());
        }
    }

    public static List<String> obtenerRanking(){
        List<String> ranking = new ArrayList<>();
        if(conexion == null){
            ranking.add("Ranking no disponible en modo offline");
            return ranking;
        }

        String sql = "SELECT username, victorias, empates, derrotas, puntos FROM ranking ORDER BY puntos DESC, victorias DESC LIMIT 10";

        try(Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            ranking.add("\n=== RANKING DE JUGADORES ===");
            ranking.add("Pos | Usuario      | V | E | D | Puntos");
            ranking.add("----+--------------+---+---+---+-------");

            int pos = 1;
            boolean hayJugadores = false;
            while(rs.next()){
                hayJugadores = true;
                String usuario = rs.getString("username");
                int victorias = rs.getInt("victorias");
                int empates = rs.getInt("empates");
                int derrotas = rs.getInt("derrotas");
                int puntos = rs.getInt("puntos");

                ranking.add(String.format("%-3d | %-12s | %d | %d | %d | %d",
                        pos++, usuario, victorias, empates, derrotas, puntos));
            }

            if(!hayJugadores){
                ranking.add("No hay jugadores en el ranking todavia");
            }

        }catch(SQLException e){
            System.err.println("Error obteniendo ranking: "+e.getMessage());
            ranking.clear();
            ranking.add("Error al obtener el ranking");
        }

        return ranking;
    }

    public static String obtenerRankingsVs(String jugador1, String jugador2){
        if(conexion == null){
            return "Ranking no disponible en modo offline";
        }

        String sql = "SELECT username, victorias, empates, derrotas, puntos FROM ranking WHERE username IN (?,?)";

        try(PreparedStatement pstmt = conexion.prepareStatement(sql)){
            pstmt.setString(1, jugador1);
            pstmt.setString(2, jugador2);

            ResultSet rs = pstmt.executeQuery();

            int v1 = 0, e1 = 0, d1 = 0, p1 = 0;
            int v2 = 0, e2 = 0, d2 = 0, p2 = 0;
            boolean encontrado1 = false, encontrado2 = false;

            while(rs.next()){
                String username = rs.getString("username");
                if(username.equals(jugador1)){
                    v1 = rs.getInt("victorias");
                    e1 = rs.getInt("empates");
                    d1 = rs.getInt("derrotas");
                    p1 = rs.getInt("puntos");
                    encontrado1 = true;
                } else if(username.equals(jugador2)){
                    v2 = rs.getInt("victorias");
                    e2 = rs.getInt("empates");
                    d2 = rs.getInt("derrotas");
                    p2 = rs.getInt("puntos");
                    encontrado2 = true;
                }
            }

            if(!encontrado1 && !encontrado2){
                return "Ninguno de los dos jugadores tiene estadisticas registradas";
            } else if(!encontrado1){
                return "El jugador " + jugador1 + " no tiene estadisticas registradas";
            } else if(!encontrado2){
                return "El jugador " + jugador2 + " no tiene estadisticas registradas";
            }

            int total1 = v1 + e1 + d1;
            int total2 = v2 + e2 + d2;

            double porcentajeV1 = total1 > 0 ? (v1 * 100.0 / total1) : 0;
            double porcentajeV2 = total2 > 0 ? (v2 * 100.0 / total2) : 0;

            StringBuilder sb = new StringBuilder();
            sb.append("\n=== RANKING: ").append(jugador1).append(" vs ").append(jugador2).append(" ===\n");
            sb.append(String.format("%-12s | V: %d | E: %d | D: %d | Puntos: %d | %% Victorias: %.1f%%\n",
                    jugador1, v1, e1, d1, p1, porcentajeV1));
            sb.append(String.format("%-12s | V: %d | E: %d | D: %d | Puntos: %d | %% Victorias: %.1f%%",
                    jugador2, v2, e2, d2, p2, porcentajeV2));

            return sb.toString();

        }catch(SQLException e){
            System.err.println("Error obteniendo ranking vs: "+e.getMessage());
            return "Error al obtener ranking";
        }
    }

    public static void cerrarConexion(){
        if(conexion != null){
            try{
                conexion.close();
                System.out.println("Conexion cerrada");
            }catch(SQLException e){
                System.err.println("Error al cerrar el conexion: "+e.getMessage());
            }
        }
    }

    public static boolean estaConectado(){
        return conexion != null;
    }

}
