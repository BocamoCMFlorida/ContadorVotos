package com.example.demo5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PartidoModel extends DBUtil {

    public ArrayList<Partido> getPartidos() {
        ArrayList<Partido> resultado = new ArrayList<>();
        try {

            PreparedStatement ps = this.getConexion().prepareStatement("SELECT nombre, votos, siglas FROM partido");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String nombre = rs.getString("nombre");
                int votos = rs.getInt("votos");
                String siglas = rs.getString("siglas");

                // Crear el objeto Partido con los datos recuperados
                Partido p = new Partido(nombre, votos, siglas);
                resultado.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
}
