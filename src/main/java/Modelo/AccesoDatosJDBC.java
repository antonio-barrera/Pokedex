/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author jeant
 */
public class AccesoDatosJDBC {
    private static String SQL_SELECT = "SELECT * FROM ";
    private static final String SQL_INSERT = "INSERT INTO favoritos(usuario, pokemon_id) VALUES(?, ?)";
    private static final String SQL_DELETE = "DELETE FROM favoritos WHERE pokemon_id = ?";
    
    public static ResultSet ejecutarConsulta(String tabla, String consulta){    
        String query = SQL_SELECT + tabla + consulta;
        Connection cn = null;
        ResultSet rs = null;
        Statement st = null;
        try {
            cn = ClsConexion.getConnection();
            st = cn.createStatement();
            rs = st.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        } 
        return rs;
    }
    
    public static void ejecutarInsertFavoritos(String usuario, int id) {
        Connection cn = null;
        PreparedStatement stmt = null;
        try {
            cn = ClsConexion.getConnection();
            stmt = cn.prepareStatement(SQL_INSERT);
            stmt.setString(1, usuario);
            stmt.setInt(2, id);
            stmt.execute();
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        } 
    }
    
    public static void ejecutarDeleteFavoritos(int id) {
        Connection cn = null;
        PreparedStatement stmt = null;
        try {
            cn = ClsConexion.getConnection();
            stmt = cn.prepareStatement(SQL_DELETE);
            stmt.setInt(1, id);
            stmt.execute();
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        } 
    }
}
