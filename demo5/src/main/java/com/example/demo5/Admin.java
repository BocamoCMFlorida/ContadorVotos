package com.example.demo5;

public class Admin extends Usuario{

    public Admin(String DNI, String contraseña, String nombre, int edad, String sexo, boolean haVotado,boolean es_admin) {
        super(DNI, contraseña, nombre, edad, sexo, haVotado,es_admin);
    }
}
