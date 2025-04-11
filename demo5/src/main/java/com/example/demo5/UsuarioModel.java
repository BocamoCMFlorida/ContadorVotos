package com.example.demo5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UsuarioModel extends DBUtil {

    // Método para obtener los usuarios
    public ArrayList<Usuario> getUsuarios() {
        ArrayList<Usuario> resultado = new ArrayList<>();
        try {
            PreparedStatement ps = this.getConexion().prepareStatement("SELECT DNI, contraseña, nombre, edad, sexo, HaVotado FROM usuario");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String DNI = rs.getString("DNI");
                String contraseña = rs.getString("contraseña");
                String nombre = rs.getString("nombre");
                int edad = rs.getInt("edad");
                String sexo = rs.getString("sexo");
                boolean HaVotado = rs.getBoolean("HaVotado");
                boolean es_admin = rs.getBoolean("es_admin");

                Usuario u = new Usuario(DNI, contraseña, nombre, edad, sexo, HaVotado,es_admin);
                resultado.add(u);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    // Método para insertar un nuevo usuario
    public boolean insert(Usuario u) {
        boolean resultado = false;
        try {
            String sql = "INSERT INTO usuario (DNI, contraseña, nombre, edad, sexo, HaVotado,es_admin) VALUES (?, ?, ?, ?, ?, ?,?)";
            PreparedStatement ps = this.getConexion().prepareStatement(sql);
            ps.setString(1, u.getDNI());
            ps.setString(2, u.getContraseña());
            ps.setString(3, u.getNombre());
            ps.setInt(4, u.getEdad());
            ps.setString(5, u.getSexo());
            ps.setBoolean(6, u.isHaVotado());
            ps.setBoolean(7,u.isEs_admin());
            resultado = ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    // Método para actualizar el estado de votación de un usuario
    public int actualizarEstadoVotado(String dni) {
        int resultado = 0;
        try {
            String sql = "UPDATE usuario SET HaVotado = true WHERE DNI = ?";
            PreparedStatement ps = this.getConexion().prepareStatement(sql);
            ps.setString(1, dni);
            resultado = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    // Método para eliminar un usuario
    public int remove(Usuario u) {
        int resultado = 0;
        try {
            String sql = "DELETE FROM usuario WHERE DNI = ?";
            PreparedStatement ps = this.getConexion().prepareStatement(sql);
            ps.setString(1, u.getDNI());
            resultado = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    // Método para eliminar un usuario por DNI
    public int remove(String dni) {
        int resultado = 0;
        try {
            String sql = "DELETE FROM usuario WHERE DNI = ?";
            PreparedStatement ps = this.getConexion().prepareStatement(sql);
            ps.setString(1, dni);
            resultado = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
}
