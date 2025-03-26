package com.example.demo5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
public class UsuarioModel extends DBUtil {

    public ArrayList<Usuario> getUsuario() {

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

                Usuario u = new Usuario(DNI, contraseña, nombre, edad, sexo, HaVotado);
                resultado.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;


    }
}

