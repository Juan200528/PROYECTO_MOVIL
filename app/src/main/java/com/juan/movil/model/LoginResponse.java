package com.juan.movil.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("nombre")
    private String nombre;

    public String getToken() {
        return token;
    }

    public String getNombre() {
        return nombre;
    }

    // Setters si necesitas (opcional)

    public void setToken(String token) {
        this.token = token;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
