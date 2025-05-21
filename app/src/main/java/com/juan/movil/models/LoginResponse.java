package com.juan.movil.models;

public class LoginResponse {
    private String token;
    private String nombre; // o como se llame el campo en tu backend

    public String getToken() {
        return token;
    }

    public String getNombre() {
        return nombre;
    }
}

