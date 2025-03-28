package com.example.demo5;

public class UsuarioSesion {

    // Variable estática para almacenar el DNI del usuario actual
    private static String dniActual;

    // Método para establecer el DNI del usuario autenticado
    public static void setDniActual(String dni) {
        dniActual = dni;
    }

    // Método para obtener el DNI del usuario actual
    public static String getDniActual() {
        return dniActual;
    }

    // Método para verificar si el usuario está autenticado
    public static boolean isUsuarioAutenticado() {
        return dniActual != null && !dniActual.isEmpty();
    }
}
