package com.example.demo5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PartidoModel extends DBUtil {

    public ArrayList<Partido> getPartidos() {
        ArrayList<Partido> resultado = new ArrayList<>();
        try {
            // Aquí colocamos una consulta SQL adecuada para recuperar los partidos
            PreparedStatement ps = this.getConexion().prepareStatement("SELECT nombre, votos, siglas FROM partido");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Extraemos los datos del ResultSet
                String nombre = rs.getString("nombre");
                int votos = rs.getInt("votos");
                String siglas = rs.getString("siglas");

                // Creamos el objeto Partido con los datos recuperados
                Partido p = new Partido(nombre, votos, siglas);
                resultado.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
}
