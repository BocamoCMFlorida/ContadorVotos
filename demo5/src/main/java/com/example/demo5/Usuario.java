package com.example.demo5;

public class Usuario {
    private String DNI;
    private String contraseña;
    private String nombre;
    private int edad;
    private String sexo;
    private boolean HaVotado;

    // Variable estática para almacenar el usuario autenticado
    private static Usuario usuarioActual;

    public Usuario(String DNI, String contraseña, String nombre, int edad, String sexo, boolean haVotado) {
        this.DNI = DNI;
        this.contraseña = contraseña;
        this.nombre = nombre;
        this.edad = edad;
        this.sexo = sexo;
        this.HaVotado = haVotado;
    }

    // Métodos Getter y Setter
    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public boolean isHaVotado() {
        return HaVotado;
    }

    public void setHaVotado(boolean haVotado) {
        HaVotado = haVotado;
    }

    // Métodos para manejar el usuario autenticado
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
