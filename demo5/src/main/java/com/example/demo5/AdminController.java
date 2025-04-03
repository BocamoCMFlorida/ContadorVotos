package com.example.demo5;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.Optional;

public class AdminController {

    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, String> columnaDNI;
    @FXML
    private TableColumn<Usuario, String> columnaNombre;
    @FXML
    private TableColumn<Usuario, Integer> columnaEdad;
    @FXML
    private TableColumn<Usuario, String> columnaSexo;
    @FXML
    private TableColumn<Usuario, String> columnaHaVotado;
    @FXML
    private TableColumn<Usuario, Boolean> columnaAdmin; // Cambiar tipo a Boolean

    @FXML
    private Button btnAgregarUsuario;
    @FXML
    private Button btnEliminarUsuario;
    @FXML
    private Button btnModificarUsuario;

    private ObservableList<Usuario> usuariosList = FXCollections.observableArrayList();
    private DBUtil dbUtil = new DBUtil();

    @FXML
    public void initialize() {
        // Inicializamos las columnas de la tabla
        columnaDNI.setCellValueFactory(new PropertyValueFactory<>("DNI"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        columnaSexo.setCellValueFactory(new PropertyValueFactory<>("sexo"));
        columnaHaVotado.setCellValueFactory(new PropertyValueFactory<>("haVotado"));

        // Modificamos la columna Admin para que funcione correctamente con el tipo boolean
        columnaAdmin.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            return new SimpleBooleanProperty(usuario.isEs_admin());  // Devuelve un SimpleBooleanProperty
        });

        // Cargamos los datos en la tabla
        cargarDatosUsuarios();
    }

    private void cargarDatosUsuarios() {
        usuariosList.clear();
        String query = "SELECT DNI, nombre, edad, sexo, HaVotado, es_admin FROM usuario";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            // Verificar si la consulta devuelve resultados
            if (!rs.isBeforeFirst()) {
                System.out.println("No hay usuarios en la base de datos.");
            }

            while (rs.next()) {
                String dni = rs.getString("DNI");
                String nombre = rs.getString("nombre");
                int edad = rs.getInt("edad");
                String sexo = rs.getString("sexo");

                // Obtener el valor booleano de HaVotado, asegurándonos de que no sea NULL
                boolean haVotado = rs.getBoolean("HaVotado");
                if (rs.wasNull()) { // Si el valor de HaVotado es NULL en la base de datos, asignar false
                    haVotado = false;
                }

                // Obtener el valor booleano de es_admin, asegurándonos de que no sea NULL
                boolean esAdmin = rs.getBoolean("es_admin");
                if (rs.wasNull()) { // Si el valor de es_admin es NULL en la base de datos, asignar false
                    esAdmin = false;
                }

                // Crear el objeto Usuario y agregarlo a la lista
                usuariosList.add(new Usuario(dni, "", nombre, edad, sexo, haVotado, esAdmin));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Establecer los datos a la tabla
        tablaUsuarios.setItems(usuariosList);
    }

    // Agregar Usuario
    @FXML
    private void agregarUsuario() {
        // Crear formulario para agregar un nuevo usuario
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Agregar Usuario");

        // Crear un formulario de entrada con los campos necesarios
        // (DNI, nombre, edad, sexo, admin)
        TextField dniField = new TextField();
        dniField.setPromptText("DNI");
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");
        TextField edadField = new TextField();
        edadField.setPromptText("Edad");
        ComboBox<String> sexoComboBox = new ComboBox<>(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));
        CheckBox adminCheckBox = new CheckBox("Es Administrador?");

        // Configuramos el formulario
        dialog.getDialogPane().setContent(new VBox(dniField, nombreField, edadField, sexoComboBox, adminCheckBox));

        ButtonType buttonTypeGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeGuardar, ButtonType.CANCEL);

        // Procesar el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeGuardar) {
                String dni = dniField.getText();
                String nombre = nombreField.getText();
                int edad = Integer.parseInt(edadField.getText());
                String sexo = sexoComboBox.getValue();
                boolean esAdmin = adminCheckBox.isSelected();

                // Guardamos el usuario en la base de datos
                agregarUsuarioDB(dni, nombre, edad, sexo, esAdmin);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void agregarUsuarioDB(String dni, String nombre, int edad, String sexo, boolean esAdmin) {
        String query = "INSERT INTO usuario (DNI, nombre, edad, sexo, HaVotado, es_admin) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, dni);
            ps.setString(2, nombre);
            ps.setInt(3, edad);
            ps.setString(4, sexo);
            ps.setBoolean(5, false);  // Inicializamos HaVotado como false
            ps.setBoolean(6, esAdmin);
            ps.executeUpdate();
            cargarDatosUsuarios();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Eliminar Usuario
    @FXML
    private void eliminarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado != null) {
            // Confirmación de eliminación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Eliminar Usuario");
            alert.setHeaderText("¿Estás seguro de que deseas eliminar a este usuario?");
            alert.setContentText("DNI: " + usuarioSeleccionado.getDNI());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                eliminarUsuarioDB(usuarioSeleccionado);
            }
        } else {
            // Si no hay usuario seleccionado
            mostrarAlerta("Error", "Debes seleccionar un usuario para eliminar", Alert.AlertType.ERROR);
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

    // Modificar Usuario
    @FXML
    private void modificarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado != null) {
            // Crear formulario para modificar los datos del usuario
            TextInputDialog dialog = new TextInputDialog(usuarioSeleccionado.getNombre());
            dialog.setTitle("Modificar Usuario");
            dialog.setHeaderText("Modificar los detalles del usuario");

            // Mostrar formulario para editar el nombre
            dialog.getDialogPane().setContentText("Nuevo Nombre:");
            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String nuevoNombre = result.get();
                // Guardamos la modificación en la base de datos
                modificarUsuarioDB(usuarioSeleccionado, nuevoNombre);
            }
        } else {
            // Si no hay usuario seleccionado
            mostrarAlerta("Error", "Debes seleccionar un usuario para modificar", Alert.AlertType.ERROR);
        }
    }

    private void modificarUsuarioDB(Usuario usuario, String nuevoNombre) {
        String query = "UPDATE usuario SET nombre = ? WHERE DNI = ?";

        try (Connection conn = dbUtil.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, nuevoNombre);
            ps.setString(2, usuario.getDNI());
            ps.executeUpdate();
            cargarDatosUsuarios();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String title, String message, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
