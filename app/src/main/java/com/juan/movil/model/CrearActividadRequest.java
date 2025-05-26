package com.juan.movil.model;

import com.google.gson.annotations.SerializedName;

public class CrearActividadRequest {

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("lugar")
    private String lugar;

    @SerializedName("responsables")
    private String responsables;

    @SerializedName("id_creador")
    private int idCreador;

    @SerializedName("estado")
    private String estado;

    @SerializedName("imagen_ruta")
    private String imagenRuta;

    // Constructor vacío
    public CrearActividadRequest() {
    }

    // Constructor completo
    public CrearActividadRequest(String titulo, String descripcion, String fecha,
                                 String lugar, String responsables, int idCreador,
                                 String estado, String imagenRuta) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.lugar = lugar;
        this.responsables = responsables;
        this.idCreador = idCreador;
        this.estado = estado;
        this.imagenRuta = imagenRuta;
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

    public int getIdCreador() {
        return idCreador;
    }

    public void setIdCreador(int idCreador) {
        this.idCreador = idCreador;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getImagenRuta() {
        return imagenRuta;
    }

    public void setImagenRuta(String imagenRuta) {
        this.imagenRuta = imagenRuta;
    }
}