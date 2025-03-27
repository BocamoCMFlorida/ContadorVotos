package com.example.demo5;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            // Configurar los parámetros de la consulta
            ps.setString(1, dni);
            ps.setString(2, password);

            // Ejecutar la consulta
            try (ResultSet rs = ps.executeQuery()) {
                // Si se encuentra un registro que coincida con el DNI y la contraseña, se realiza login
                if (rs.next()) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("general.fxml"));
            Scene scene = new Scene(loader.load());

            // Obtener el Stage actual y cambiar la escena
            Stage stage = (Stage) dniField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("General");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la pantalla principal", Alert.AlertType.ERROR);
        }
    }
}
