package com.example.demo5;

public class Partido {
    private String nombre;
    private int votos;
    private String siglas;

    // Constructor
    public Partido(String nombre, int votos, String siglas) {
        this.nombre = nombre;
        this.votos = votos;
        this.siglas = siglas;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getVotos() {
        return votos;
    }

    public void setVotos(int votos) {
        this.votos = votos;
    }

    public String getSiglas() {
        return siglas;
    }

    public void setSiglas(String siglas) {
        this.siglas = siglas;
    }

    // Constructor sin parámetros
    public Partido() {
    }
}
