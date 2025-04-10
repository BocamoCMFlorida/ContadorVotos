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

public class AdminController {

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> columnaDNI;
    @FXML private TableColumn<Usuario, String> columnaNombre;
    @FXML private TableColumn<Usuario, Integer> columnaEdad;
    @FXML private TableColumn<Usuario, String> columnaSexo;
    @FXML private TableColumn<Usuario, String> columnaContraseña;
    @FXML private TableColumn<Usuario, String> columnaHaVotado;
    @FXML private TableColumn<Usuario, Boolean> columnaAdmin;


    @FXML private Button btnAgregarUsuario;
    @FXML private Button btnEliminarUsuario;
    @FXML private Button btnModificarUsuario;
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnVerpartidos;
    // Formulario embebido (opcional)
    @FXML private AnchorPane formAgregarUsuario;
    @FXML private TextField dniField;
    @FXML private TextField nombreField;
    @FXML private  TextField contraseñaField;

    @FXML private TextField edadField;
    @FXML private ComboBox<String> sexoComboBox;
    @FXML private CheckBox adminCheckBox;

    private ObservableList<Usuario> usuariosList = FXCollections.observableArrayList();
    private DBUtil dbUtil = new DBUtil();

    @FXML
    public void initialize() {
        columnaDNI.setCellValueFactory(new PropertyValueFactory<>("DNI"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        columnaSexo.setCellValueFactory(new PropertyValueFactory<>("sexo"));
        columnaContraseña.setCellValueFactory(new PropertyValueFactory<>("contraseña"));
        columnaHaVotado.setCellValueFactory(new PropertyValueFactory<>("haVotado"));
        columnaAdmin.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isEs_admin()));

        sexoComboBox.setItems(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));

        cargarDatosUsuarios();
    }

    // CAMBIO: agregar columna de contraseña al SELECT
    private void cargarDatosUsuarios() {
        usuariosList.clear();
        String query = "SELECT DNI, nombre, edad, sexo, contraseña, HaVotado, es_admin FROM usuario";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String dni = rs.getString("DNI");
                String nombre = rs.getString("nombre");
                int edad = rs.getInt("edad");
                String sexo = rs.getString("sexo");
                String contraseña = rs.getString("contraseña");
                boolean haVotado = rs.getBoolean("HaVotado");
                if (rs.wasNull()) haVotado = false;
                boolean esAdmin = rs.getBoolean("es_admin");
                if (rs.wasNull()) esAdmin = false;

                // Asegúrate que el orden de parámetros coincida con tu constructor de Usuario
                usuariosList.add(new Usuario(dni, contraseña, nombre, edad, sexo, haVotado, esAdmin));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los usuarios", Alert.AlertType.ERROR);
        }

        tablaUsuarios.setItems(usuariosList);
    }


    @FXML
    private void agregarUsuario() {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Agregar Usuario");
        // Campos del formulario
        TextField dniField = new TextField();
        TextField nombreField = new TextField();
        TextField edadField = new TextField();
        TextField contraseñaField = new TextField();

        ComboBox<String> sexoComboBox = new ComboBox<>(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));;
        CheckBox adminCheckBox = new CheckBox("¿Es Administrador?");
        VBox contenido = new VBox(10,
                new Label("DNI"), dniField,
                new Label("Nombre"), nombreField,
                new Label("Edad"), edadField,
                new Label("Sexo"), sexoComboBox,
                new Label("contraseña"),contraseñaField,
                adminCheckBox

        );

        dialog.getDialogPane().setContent(contenido);
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                try {
                    int edad = Integer.parseInt(edadField.getText());

                    agregarUsuarioDB(
                            dniField.getText(),
                            nombreField.getText(),
                            edad,
                            sexoComboBox.getValue(),
                            adminCheckBox.isSelected(),
                            contraseñaField.getText()
                            );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Edad inválida", Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    @FXML
    private void guardarUsuario() {
        String dni = dniField.getText();
        String nombre = nombreField.getText();
        String edadText = edadField.getText();
        String sexo = sexoComboBox.getValue();
        String contraseña = contraseñaField.getText();
        boolean esAdmin = adminCheckBox.isSelected();

        // Validaciones
        if (dni.isEmpty() || nombre.isEmpty() || edadText.isEmpty() || sexo == null || contraseña.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor, completa todos los campos", Alert.AlertType.WARNING);
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadText);
        } catch (NumberFormatException e) {
            mostrarAlerta("Edad inválida", "Ingresa un número válido para la edad", Alert.AlertType.ERROR);
            return;
        }

        // Llamar al método correcto con contraseña
        agregarUsuarioDB(dni, nombre, edad, sexo, esAdmin, contraseña);

        limpiarFormulario();
        formAgregarUsuario.setVisible(false);
    }


    // CAMBIO IMPORTANTE: este método tenía error en la cantidad de parámetros en la consulta SQL
    private void agregarUsuarioDB(String dni, String nombre, int edad, String sexo, boolean esAdmin, String contraseña) {
        String query = "INSERT INTO usuario (DNI, nombre, edad, sexo, contraseña, HaVotado, es_admin) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, dni);
            ps.setString(2, nombre);
            ps.setInt(3, edad);
            ps.setString(4, sexo);
            ps.setString(5, contraseña); // Corregido: antes estaba usando setBoolean por error
            ps.setBoolean(6, false); // HaVotado por defecto false
            ps.setBoolean(7, esAdmin);
            ps.executeUpdate();
            cargarDatosUsuarios();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelarAgregarUsuario() {
        limpiarFormulario();
        formAgregarUsuario.setVisible(false);
    }

    private void limpiarFormulario() {
        dniField.clear();
        nombreField.clear();
        edadField.clear();
        sexoComboBox.setValue(null);
        adminCheckBox.setSelected(false);
    }

    @FXML
    private void eliminarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Eliminar Usuario");
            alert.setHeaderText("¿Eliminar al usuario?");
            alert.setContentText("DNI: " + usuarioSeleccionado.getDNI());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                eliminarUsuarioDB(usuarioSeleccionado);
            }
        } else {
            mostrarAlerta("Sin selección", "Selecciona un usuario para eliminar", Alert.AlertType.ERROR);
        }
    }

    private void eliminarUsuarioDB(Usuario usuario) {
        String query = "DELETE FROM usuario WHERE DNI = ?";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, usuario.getDNI());
            ps.executeUpdate();
            cargarDatosUsuarios();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modificarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null) {
            mostrarAlerta("Sin selección", "Selecciona un usuario para modificar", Alert.AlertType.WARNING);
            return;
        }

        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Modificar Usuario");

        // Campos del formulario
        TextField dniField = new TextField(usuarioSeleccionado.getDNI());
        TextField nombreField = new TextField(usuarioSeleccionado.getNombre());
        TextField edadField = new TextField(String.valueOf(usuarioSeleccionado.getEdad()));
        ComboBox<String> sexoComboBox = new ComboBox<>(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));
        sexoComboBox.setValue(usuarioSeleccionado.getSexo());
        CheckBox adminCheckBox = new CheckBox("¿Es Administrador?");
        adminCheckBox.setSelected(usuarioSeleccionado.isEs_admin());
        CheckBox haVotadoCheckBox = new CheckBox("¿Ha Votado?");
        haVotadoCheckBox.setSelected(usuarioSeleccionado.isHaVotado());

        VBox contenido = new VBox(10,
                new Label("DNI"), dniField,
                new Label("Nombre"), nombreField,
                new Label("Edad"), edadField,
                new Label("Sexo"), sexoComboBox,
                adminCheckBox,
                haVotadoCheckBox
        );

        dialog.getDialogPane().setContent(contenido);
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                try {
                    int edad = Integer.parseInt(edadField.getText());

                    modificarUsuarioDBCompleto(
                            usuarioSeleccionado.getDNI(),
                            dniField.getText(),
                            nombreField.getText(),
                            edad,
                            sexoComboBox.getValue(),
                            haVotadoCheckBox.isSelected(),
                            adminCheckBox.isSelected()
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Edad inválida", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }



    private void modificarUsuarioDBCompleto(String dniAnterior, String nuevoDni, String nombre, int edad, String sexo, boolean haVotado, boolean esAdmin) {
        String query = "UPDATE usuario SET DNI = ?, nombre = ?, edad = ?, sexo = ?, HaVotado = ?, es_admin = ? WHERE DNI = ?";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, nuevoDni);
            ps.setString(2, nombre);
            ps.setInt(3, edad);
            ps.setString(4, sexo);
            ps.setBoolean(5, haVotado);
            ps.setBoolean(6, esAdmin);
            ps.setString(7, dniAnterior);
            ps.executeUpdate();
            cargarDatosUsuarios();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al modificar", "No se pudo actualizar el usuario", Alert.AlertType.ERROR);
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
    private void cambiarPantallaPartidos(MouseEvent event) {
        cargarVistaPartidos();
    }

    private void cargarVistaPartidos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-view-partidos.fxml"));
            Scene scene = new Scene(loader.load(), 700, 640);
            Stage stage = (Stage) btnVerpartidos.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Panel de Partidos");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista de partidos", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


}
