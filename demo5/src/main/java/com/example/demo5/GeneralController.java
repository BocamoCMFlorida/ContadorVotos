package com.example.demo5;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GeneralController {

    @FXML
    private TableView<Partido> tablaPartidos;  // Tabla para mostrar los partidos

    @FXML
    private TableColumn<Partido, String> colNombre;
    @FXML
    private TableColumn<Partido, String> colSiglas;
    @FXML
    private TableColumn<Partido, Integer> colVotos;

    @FXML
    private Button btnVotar;  // Botón para votar
    @FXML
    private Button cerrarSesionBtn;  // Botón para cerrar sesión

    private ObservableList<Partido> partidosList = FXCollections.observableArrayList();

    private DBUtil dbUtil = new DBUtil();  // Instancia de DBUtil

    @FXML
    public void initialize() {
        // Cargar los datos de los partidos en la tabla
        cargarDatosPartidos();

        // Configurar las columnas de la tabla con PropertyValueFactory
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colSiglas.setCellValueFactory(new PropertyValueFactory<>("siglas"));
        colVotos.setCellValueFactory(new PropertyValueFactory<>("votos"));

        // Asignar los datos de los partidos a la tabla
        tablaPartidos.setItems(partidosList);
    }

    // Método para cargar partidos en la tabla desde la base de datos
    private void cargarDatosPartidos() {
        partidosList.clear();  // Limpiar la lista antes de cargar nuevos datos
        String query = "SELECT nombre, siglas, votos FROM partido"; // Ajusta la consulta a tu esquema

        try (Connection conn = dbUtil.getConexion();  // Usando la instancia de DBUtil
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String siglas = rs.getString("siglas");
                int votos = rs.getInt("votos");

                // Agregar los partidos a la lista
                Partido partido = new Partido(nombre, votos, siglas);
                partidosList.add(partido);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para manejar el evento de votar
    @FXML
    private void votarPartido(MouseEvent event) {
        // Verificar si el usuario ha votado
        String dni = getCurrentUserDNI();  // Suponiendo que tienes el DNI del usuario actual

        if (yaVoto(dni)) {
            mostrarAlerta("Error", "Ya has votado", Alert.AlertType.ERROR);
        } else {
            // Lógica para registrar el voto
            Partido partidoSeleccionado = tablaPartidos.getSelectionModel().getSelectedItem();
            if (partidoSeleccionado != null) {
                // Registrar el voto en la base de datos (incrementar los votos)
                String siglas = partidoSeleccionado.getSiglas();
                registrarVoto(siglas, dni);  // Registrar el voto del usuario
                actualizarTabla();  // Actualizar la tabla para reflejar el nuevo voto

                // Marcar al usuario como que ya votó
                marcarComoVotado(dni);
            } else {
                mostrarAlerta("Error", "Selecciona un partido para votar", Alert.AlertType.ERROR);
            }
        }
    }

    // Método para verificar si el usuario ya ha votado
    private boolean yaVoto(String dni) {
        String query = "SELECT HaVotado FROM usuario WHERE DNI = ?";
        try (Connection conn = dbUtil.getConexion();  // Usando la instancia de DBUtil
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean haVotado = rs.getBoolean("HaVotado");
                System.out.println("HaVotado: " + haVotado);  // Depuración
                return haVotado;  // Si ha votado, devolver true
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para registrar el voto
    private void registrarVoto(String siglas, String dni) {
        // Incrementar el voto del partido
        String updatePartido = "UPDATE partido SET votos = votos + 1 WHERE siglas = ?";
        try (Connection conn = dbUtil.getConexion();  // Usando la instancia de DBUtil
             PreparedStatement ps = conn.prepareStatement(updatePartido)) {
            ps.setString(1, siglas);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para marcar al usuario como que ya votó
    private void marcarComoVotado(String dni) {
        String updateUsuario = "UPDATE usuario SET HaVotado = true WHERE DNI = ?";
        try (Connection conn = dbUtil.getConexion();  // Usando la instancia de DBUtil
             PreparedStatement ps = conn.prepareStatement(updateUsuario)) {
            ps.setString(1, dni);
            ps.executeUpdate();
            System.out.println("Usuario con DNI " + dni + " marcado como que ya votó.");  // Depuración
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para actualizar la tabla con los nuevos datos
    private void actualizarTabla() {
        // Llamar a cargarDatosPartidos() para recargar la tabla con los votos actualizados
        cargarDatosPartidos();
    }

    // Método para mostrar alertas
    private void mostrarAlerta(String title, String message, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Método para obtener el DNI del usuario actual (esto depende de tu implementación de autenticación)
    private String getCurrentUserDNI() {
        // Implementa aquí la lógica para obtener el DNI del usuario actual
        // Ejemplo: obtener el DNI de una variable de sesión o de una clase de usuario
        return "12345678";  // Esto es un ejemplo; debes implementarlo adecuadamente
    }

    // Método para manejar el evento de cerrar sesión
    @FXML
    private void cerrarSesion(MouseEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesión");
        alert.setHeaderText("¿Estás seguro de que deseas cerrar sesión?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Redirigir al login (puedes hacerlo usando el método de cambio de pantalla que ya tienes en el LoginController)
                // Aquí el código para regresar al login
                // Por ejemplo:
                // cambiarPantallaLogin();
            }
        });
    }
}
