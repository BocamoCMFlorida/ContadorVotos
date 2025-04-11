package com.example.demo5;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class AdminPartidosController {

    @FXML private TableView<Partido> tablaPartidos;
    @FXML private TableColumn<Partido, Integer> columnaID;
    @FXML private TableColumn<Partido, String> columnaNombre;
    @FXML private TableColumn<Partido, String> columnaSiglas;
    @FXML private TableColumn<Partido, Integer> columnaVotos;

    @FXML private Button btnAgregarPartido;
    @FXML private Button btnEliminarPartido;
    @FXML private Button btnModificarPartido;
    @FXML private Button btnvolverAtras;
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnVerUsuarios;
    @FXML private AnchorPane formAgregarPartido;
    @FXML private TextField nombrePartidoField;
    @FXML private TextField siglasPartidoField;
    @FXML private TextField votosPartidoField;
    @FXML
    private ObservableList<Partido> partidosList = FXCollections.observableArrayList();
    private DBUtil dbUtil = new DBUtil();

    @FXML
    public void initialize() {
        // Asegurarse de que las columnas están en el orden correcto
        columnaID.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaSiglas.setCellValueFactory(new PropertyValueFactory<>("siglas"));
        columnaVotos.setCellValueFactory(new PropertyValueFactory<>("votos"));

        cargarDatosPartidos();
    }

    private void cargarDatosPartidos() {
        partidosList.clear();
        String query = "SELECT id, nombre, siglas, votos FROM partido";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String siglas = rs.getString("siglas"); // Cambié esto
                int votos = rs.getInt("votos");

                partidosList.add(new Partido(id, nombre, votos, siglas));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tablaPartidos.setItems(partidosList);
    }

    @FXML
    private void agregarPartido() {
        Dialog<Partido> dialog = new Dialog<>();
        dialog.setTitle("Agregar Partido");

        TextField nombreField = new TextField();
        TextField siglasField = new TextField();
        TextField votosField = new TextField();

        VBox contenido = new VBox(10,
                new Label("Nombre"), nombreField,
                new Label("Siglas"), siglasField,
                new Label("Votos"), votosField
        );

        dialog.getDialogPane().setContent(contenido);
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                try {
                    int votos = Integer.parseInt(votosField.getText());
                    String nombre = nombreField.getText();
                    String siglas = siglasField.getText();

                    if (nombre.isEmpty() || siglas.isEmpty()) {
                        mostrarAlerta("Campos vacíos", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
                        return null;
                    }

                    agregarPartidoDB(0, nombre, siglas, votos);  // id = 0 porque es autoincremental
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Votos inválidos", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }



    @FXML
    private void guardarPartido() {
        String nombre = nombrePartidoField.getText();
        String siglas = siglasPartidoField.getText();
        String votosText = votosPartidoField.getText();

        if (nombre.isEmpty() || siglas.isEmpty() || votosText.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor, completa todos los campos", Alert.AlertType.WARNING);
            return;
        }

        int votos;
        try {
            votos = Integer.parseInt(votosText);
        } catch (NumberFormatException e) {
            mostrarAlerta("Votos inválidos", "Ingresa un número válido para los votos", Alert.AlertType.ERROR);
            return;
        }

        // Aquí id es 0 porque es autoincremental pero no se por que no lo hace bien
        int id = 0;
        agregarPartidoDB(id, nombre, siglas, votos);
        limpiarFormulario();
        formAgregarPartido.setVisible(false);
    }

    private void agregarPartidoDB(int id, String nombre, String siglas, int votos) {
        String query = "INSERT INTO partido (nombre, siglas, votos) VALUES (?, ?, ?)"; // Eliminar el `id` ya que es autoincremental

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, nombre);
            ps.setString(2, siglas);
            ps.setInt(3, votos);
            ps.executeUpdate();
            cargarDatosPartidos();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelarAgregarPartido() {
        limpiarFormulario();
        formAgregarPartido.setVisible(false);
    }

    private void limpiarFormulario() {
        nombrePartidoField.clear();
        siglasPartidoField.clear();
        votosPartidoField.clear();
    }

    @FXML
    private void eliminarPartido() {
        Partido partidoSeleccionado = tablaPartidos.getSelectionModel().getSelectedItem();

        if (partidoSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Eliminar Partido");
            alert.setHeaderText("¿Eliminar al partido?");
            alert.setContentText("ID: " + partidoSeleccionado.getId() + " - Nombre: " + partidoSeleccionado.getNombre());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                eliminarPartidoDB(partidoSeleccionado);
            }
        } else {
            mostrarAlerta("Sin selección", "Selecciona un partido para eliminar", Alert.AlertType.ERROR);
        }
    }

    private void eliminarPartidoDB(Partido partido) {
        String query = "DELETE FROM partido WHERE id = ?";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, partido.getId());
            ps.executeUpdate();
            cargarDatosPartidos();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modificarPartido() {
        Partido partidoSeleccionado = tablaPartidos.getSelectionModel().getSelectedItem();

        if (partidoSeleccionado == null) {
            mostrarAlerta("Sin selección", "Selecciona un partido para modificar", Alert.AlertType.WARNING);
            return;
        }

        Dialog<Partido> dialog = new Dialog<>();
        dialog.setTitle("Modificar Partido");

        TextField nombreField = new TextField(partidoSeleccionado.getNombre());
        TextField siglasField = new TextField(partidoSeleccionado.getSiglas());
        TextField votosField = new TextField(String.valueOf(partidoSeleccionado.getVotos()));

        VBox contenido = new VBox(10,
                new Label("Nombre"), nombreField,
                new Label("Siglas"), siglasField,
                new Label("Votos"), votosField
        );

        dialog.getDialogPane().setContent(contenido);
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                try {
                    int votos = Integer.parseInt(votosField.getText());

                    modificarPartidoDB(
                            partidoSeleccionado.getId(),
                            nombreField.getText(),
                            siglasField.getText(),
                            votos
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Votos inválidos", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void modificarPartidoDB(int id, String nombre, String siglas, int votos) {
        String query = "UPDATE partido SET nombre = ?, siglas = ?, votos = ? WHERE id = ?";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, nombre);
            ps.setString(2, siglas);
            ps.setInt(3, votos);
            ps.setInt(4, id);
            ps.executeUpdate();
            cargarDatosPartidos();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al modificar", "No se pudo actualizar el partido", Alert.AlertType.ERROR);
        }
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
            Scene scene = new Scene(loader.load(), 700, 640);
            Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el login", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cambiarPantallaUsuarios(MouseEvent event) {
        cargarVistaUsuarios();
    }

    private void cargarVistaUsuarios() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-view-usuarios.fxml"));
            Scene scene = new Scene(loader.load(), 700, 640);
            Stage stage = (Stage) btnVerUsuarios.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Panel de Usuarios");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista de usuarios", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    @FXML
    private void volverPantallaAnterior() {
        volverAtras();
    }

    @FXML
    private void volverAtras() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
            Scene scene = new Scene(loader.load(), 700, 640);
            Stage stage = (Stage) btnvolverAtras.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Panel de Administración");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver a la pantalla anterior", Alert.AlertType.ERROR);
        }
    }


}
