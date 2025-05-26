package com.juan.movil.model;

import com.juan.movil.models.Actividad;

public class CrearActividadResponse {
    private String estado;
    private String mensaje;
    private Actividad actividad;

    public String getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Actividad getActividad() {
        return actividad;
    }
}
