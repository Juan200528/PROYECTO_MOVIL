package com.juan.movil.model;

import com.google.gson.annotations.SerializedName;

/**
 * Clase para representar la respuesta del backend al crear una actividad
 * Incluye información sobre el éxito/fallo de la operación y los datos de la actividad creada
 */
public class CrearActividadResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    @SerializedName("data")
    private ActividadData data;

    // Constructor por defecto
    public CrearActividadResponse() {
    }

    // Constructor con parámetros básicos
    public CrearActividadResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ActividadData getData() {
        return data;
    }

    public void setData(ActividadData data) {
        this.data = data;
    }

    /**
     * Clase interna para representar los datos de la actividad creada
     * Contiene la información completa de la actividad tal como fue guardada en el backend
     */
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

        @SerializedName("fecha_actualizacion")
        private String fechaActualizacion;

        // Constructor por defecto
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

        public String getFechaActualizacion() {
            return fechaActualizacion;
        }

        public void setFechaActualizacion(String fechaActualizacion) {
            this.fechaActualizacion = fechaActualizacion;
        }

        @Override
        public String toString() {
            return "ActividadData{" +
                    "id=" + id +
                    ", titulo='" + titulo + '\'' +
                    ", descripcion='" + descripcion + '\'' +
                    ", fecha='" + fecha + '\'' +
                    ", lugar='" + lugar + '\'' +
                    ", responsables='" + responsables + '\'' +
                    ", idCreador=" + idCreador +
                    ", estado='" + estado + '\'' +
                    ", imagenRuta='" + imagenRuta + '\'' +
                    ", fechaCreacion='" + fechaCreacion + '\'' +
                    ", fechaActualizacion='" + fechaActualizacion + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CrearActividadResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", error='" + error + '\'' +
                ", data=" + data +
                '}';
    }
}