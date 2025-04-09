package com.example.demo5;

public class Partido {
    private int id;
    private String nombre;
    private int votos;
    private String siglas;

    // Constructor
    public Partido(int id, String nombre, int votos, String siglas) {
        this.id = id;
        this.nombre = nombre;
        this.votos = votos;
        this.siglas = siglas;
    }
    public Partido( String nombre, int votos, String siglas) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Constructor sin parámetros
    public Partido() {
    }
}
