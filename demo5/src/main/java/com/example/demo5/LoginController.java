package com.example.demo5;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField dniField;  // Campo de texto para DNI

    @FXML
    private PasswordField passwordField;  // Campo de texto para la contraseña

    @FXML
    private void handleLogin() {
        String dni = dniField.getText();
        String password = passwordField.getText();

        // Validación de las credenciales con la base de datos
        if (validarLogin(dni, password)) {
            System.out.println("Login exitoso");

            // Guardar el DNI del usuario autenticado en la sesión
            UsuarioSesion.setDniActual(dni); // Aquí guardamos el DNI

            cambiarPantalla();  // Cambio de pantalla después del login exitoso
        } else {
            mostrarAlerta("Error", "Credenciales incorrectas", Alert.AlertType.ERROR);
        }
    }


    // Método para validar el login mediante la base de datos
    private boolean validarLogin(String dni, String password) {
        // Definir la consulta SQL para verificar las credenciales
        String query = "SELECT * FROM usuario WHERE DNI = ? AND contraseña = ?";

        // Crear una instancia de DBUtil para acceder a la conexión
        DBUtil dbUtil = new DBUtil();  // Instanciamos la clase DBUtil
        Connection conn = dbUtil.getConexion();  // Obtener la conexión

        // Conexión a la base de datos
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, dni);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                // Si se encuentra un registro que coincida con el DNI y la contraseña, se realiza login
                if (rs.next()) {
                    // Obtener si el usuario es administrador
                    boolean esAdmin = rs.getBoolean("es_admin");

                    Usuario usuario = new Usuario(
                            rs.getString("DNI"),
                            rs.getString("contraseña"),
                            rs.getString("nombre"),
                            rs.getInt("edad"),
                            rs.getString("sexo"),
                            rs.getBoolean("HaVotado"),
                            esAdmin
                    );

                    // Guardar el usuario autenticado en la sesión
                    Usuario.setUsuarioActual(usuario);

                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbUtil.cerrarConexion();  // Cerrar la conexión después de usarla
        }

        // Si no se encuentra el usuario o hay un error, retorna false
        return false;
    }


    private void mostrarAlerta(String title, String message, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //cargar pantalla
    private void cambiarPantalla() {
        try {
            Usuario usuarioActual = Usuario.getUsuarioActual();
            FXMLLoader loader;

            if (usuarioActual.isEs_admin()) {
                // Preguntar si quiere entrar como admin o usuario
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Modo de acceso");
                alert.setHeaderText("Eres administrador");
                alert.setContentText("¿Cómo deseas ingresar?");

                ButtonType botonAdmin = new ButtonType("Administrador");
                ButtonType botonUsuario = new ButtonType("Usuario");

                alert.getButtonTypes().setAll(botonAdmin, botonUsuario);
                Optional<ButtonType> resultado = alert.showAndWait();

                if (resultado.isPresent() && resultado.get() == botonAdmin) {
                    loader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
                } else {
                    loader = new FXMLLoader(getClass().getResource("general.fxml"));
                }
            } else {
                // Si es usuario normal, cargar la pantalla correspondiente
                loader = new FXMLLoader(getClass().getResource("general.fxml"));
            }

            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) dniField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("General");

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la pantalla principal", Alert.AlertType.ERROR);
        }
    }
    }
