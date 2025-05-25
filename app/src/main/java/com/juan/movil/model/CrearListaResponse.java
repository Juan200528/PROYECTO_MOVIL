package com.juan.movil.model;

public class CrearListaResponse {
    private boolean success;
    private String message;
    private Actividad data;

    // Puedes agregar clase interna Actividad si quieres mapear datos
    public static class Actividad {
        private int id;
        private String titulo;
        private String descripcion;
        private String fecha;
        private String lugar;
        private String responsables;
        private int idCreador;
        private String estado;
        private String imagenRuta;

        // Getters y setters
        public int getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getDescripcion() { return descripcion; }
        public String getFecha() { return fecha; }
        public String getLugar() { return lugar; }
        public String getResponsables() { return responsables; }
        public int getIdCreador() { return idCreador; }
        public String getEstado() { return estado; }
        public String getImagenRuta() { return imagenRuta; }
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Actividad getData() { return data; }
}
