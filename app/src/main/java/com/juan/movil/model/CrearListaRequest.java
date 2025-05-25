package com.juan.movil.model;

public class CrearListaRequest {
    private String titulo;
    private String descripcion;
    private String fecha;
    private String lugar;
    private String responsables;
    private int idCreador;
    private String imagenRuta; // Opcional, en caso de enviar url

    // Constructor
    public CrearListaRequest(String titulo, String descripcion, String fecha, String lugar, String responsables, int idCreador, String imagenRuta) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.lugar = lugar;
        this.responsables = responsables;
        this.idCreador = idCreador;
        this.imagenRuta = imagenRuta;
    }

    // Getters y setters si los necesitas
}
