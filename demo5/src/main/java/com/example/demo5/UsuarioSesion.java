package com.example.demo5;

public class UsuarioSesion {

    // Variable estática para almacenar el DNI del usuario actual
    private static String dniActual;

    public static void setDniActual(String dni) {
        dniActual = dni;
    }

    public static String getDniActual() {
        return dniActual;
    }

    public static boolean isUsuarioAutenticado() {
        return dniActual != null && !dniActual.isEmpty();
    }
    public static void cerrarSesion() {
        dniActual = null;
    }
}
