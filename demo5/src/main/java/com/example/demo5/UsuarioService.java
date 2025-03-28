package com.example.demo5;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioService {
    private static final Logger logger = Logger.getLogger(UsuarioService.class.getName());

    // Método para obtener el DNI del usuario actual
    public String getCurrentUserDNI() {
        String dniUsuario = UsuarioSesion.getDniActual(); // Obtener desde la sesión o contexto

        if (dniUsuario == null || dniUsuario.trim().isEmpty()) {
            logger.log(Level.WARNING, "⚠ No hay un usuario autenticado.");
            return null;
        }

        return dniUsuario;
    }
}