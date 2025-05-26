package com.juan.movil.model;

import com.google.gson.annotations.SerializedName;

public class CrearActividadResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ActividadData data;

    @SerializedName("error")
    private String error;

    // Constructor vacío
    public CrearActividadResponse() {
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ActividadData getData() {
        return data;
    }

    public void setData(ActividadData data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    // Clase interna para los datos de la actividad creada
    public static class ActividadData {

        @SerializedName("id")
        private int id;

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

        @SerializedName("fecha_creacion")
        private String fechaCreacion;

        // Constructor vacío
        public ActividadData() {
        }

        // Getters y Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

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

        public String getFechaCreacion() {
            return fechaCreacion;
        }

        public void setFechaCreacion(String fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
        }
    }
}