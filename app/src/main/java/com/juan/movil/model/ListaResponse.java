package com.juan.movil.model;

public class ListaResponse {
    private int id;
    private String titulo;
    private String descripcion;
    private String fecha;
    private String hora;
    private String lugar;
    private String imagenUrl;
    private int userId;

    // Getters y setters

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getLugar() {
        return lugar;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public int getUserId() {
        return userId;
    }
}
