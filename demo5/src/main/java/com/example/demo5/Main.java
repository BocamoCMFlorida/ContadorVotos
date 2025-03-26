package com.example.demo5;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        UsuarioModel um = new UsuarioModel(); // Create an instance of UsuarioModel

        // Get the list of users from the UsuarioModel
        ArrayList<Usuario> usuarios = um.getUsuario();

        // Check if the list is not empty and iterate over it
        for (Usuario usuario : usuarios) {
            System.out.println("DNI: " + usuario.getDNI());
            System.out.println("Nombre: " + usuario.getNombre());
            System.out.println("Edad: " + usuario.getEdad());
            System.out.println("Sexo: " + usuario.getSexo());
            System.out.println("Ha Votado: " + (usuario.isHaVotado() ? "Sí" : "No"));
            System.out.println("------------");
        }
    }
}