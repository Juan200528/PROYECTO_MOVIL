package com.juan.movil.model;

/**
 * Clase para representar la petición de creación de actividad
 * Usado para enviar datos al backend cuando se crea una nueva actividad
 */
public class CrearActividadRequest {

    private String titulo;
    private String descripcion;
    private String fecha;
    private String lugar;
    private String responsables;
    // La imagen se maneja como MultipartBody.Part en la petición HTTP

    // Constructor por defecto
    public CrearActividadRequest() {
    }

    // Constructor con parámetros
    public CrearActividadRequest(String titulo, String descripcion, String fecha,
                                 String lugar, String responsables) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.lugar = lugar;
        this.responsables = responsables;
    }

    // Getters y Setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getResponsables() {
        return responsables;
    }

    public void setResponsables(String responsables) {
        this.responsables = responsables;
    }

    @Override
    public String toString() {
        return "CrearActividadRequest{" +
                "titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fecha='" + fecha + '\'' +
                ", lugar='" + lugar + '\'' +
                ", responsables='" + responsables + '\'' +
                '}';
    }
}