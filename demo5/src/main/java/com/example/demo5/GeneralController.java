package com.example.demo5;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeneralController {

    @FXML
    private TableView<Partido> tablaPartidos;
    @FXML
    private TableColumn<Partido, String> colNombre;
    @FXML
    private TableColumn<Partido, String> colSiglas;
    @FXML
    private TableColumn<Partido, Integer> colVotos;
    @FXML
    private Button btnVotar;
    @FXML
    private Button cerrarSesionBtn;

    private ObservableList<Partido> partidosList = FXCollections.observableArrayList();
    private DBUtil dbUtil = new DBUtil();
    private UsuarioService usuarioService = new UsuarioService(); // Instancia de UsuarioService

    @FXML
    public void initialize() {
        cargarDatosPartidos();
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colSiglas.setCellValueFactory(new PropertyValueFactory<>("siglas"));
        colVotos.setCellValueFactory(new PropertyValueFactory<>("votos"));
        tablaPartidos.setItems(partidosList);
    }

    private void cargarDatosPartidos() {
        partidosList.clear();
        String query = "SELECT nombre, siglas, votos FROM partido";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                partidosList.add(new Partido(rs.getString("nombre"), rs.getInt("votos"), rs.getString("siglas")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void votarPartido(MouseEvent event) {
        String dni = usuarioService.getCurrentUserDNI(); // Usar el servicio para obtener el DNI

        if (dni == null) {
            mostrarAlerta("Error", "No hay un usuario autenticado.", Alert.AlertType.ERROR);
            return;
        }

        if (yaVoto(dni)) {
            mostrarAlerta("Error", "Ya has votado. No puedes volver a votar.", Alert.AlertType.ERROR);
            return;
        }

        Partido partidoSeleccionado = tablaPartidos.getSelectionModel().getSelectedItem();
        if (partidoSeleccionado != null) {
            if (registrarVoto(partidoSeleccionado.getSiglas(), dni)) {
                if (marcarComoVotado(dni)) {
                    actualizarTabla();
                    mostrarAlerta("Éxito", "Tu voto ha sido registrado correctamente.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "Hubo un problema al actualizar tu estado de votación.", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "No se pudo registrar el voto.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Error", "Selecciona un partido para votar.", Alert.AlertType.ERROR);
        }
    }

    private boolean registrarVoto(String siglas, String dni) {
        String updatePartido = "UPDATE partido SET votos = votos + 1 WHERE siglas = ?";
        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(updatePartido)) {

            ps.setString(1, siglas);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean yaVoto(String dni) {
        String query = "SELECT HaVotado FROM usuario WHERE DNI = ?";
        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean haVotado = rs.getBoolean("HaVotado");
                System.out.println("Estado de HaVotado para " + dni + ": " + haVotado);
                return haVotado;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void actualizarTabla() {
        cargarDatosPartidos();
    }

    private boolean marcarComoVotado(String dni) {
        String updateUsuario = "UPDATE usuario SET HaVotado = 1 WHERE DNI = ?";
        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(updateUsuario)) {

            ps.setString(1, dni);
            int filasActualizadas = ps.executeUpdate();

            if (filasActualizadas > 0) {
                System.out.println("Usuario " + dni + " ha sido marcado como votado.");
                return true;
            } else {
                System.out.println("Error: No se pudo actualizar HaVotado para " + dni);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void mostrarAlerta(String title, String message, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void cerrarSesion(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesión");
        alert.setHeaderText("¿Estás seguro de que deseas cerrar sesión?");
        alert.setContentText("Se cerrará tu sesión y volverás a la pantalla de inicio.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                UsuarioSesion.cerrarSesion();

                cambiarPantallaLogin();
            }
        });
    }
    private void cambiarPantallaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 320, 240);

            Stage stage = (Stage) cerrarSesionBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el login", Alert.AlertType.ERROR);
        }
    }
}
