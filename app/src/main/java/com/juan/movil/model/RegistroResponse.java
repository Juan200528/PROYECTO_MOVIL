package com.juan.movil.model;

import com.google.gson.annotations.SerializedName;

public class RegistroResponse {

    @SerializedName("msg") // <-- Ajusta si el backend devuelve "message" u otro campo
    private String message;

    public String getMessage() {
        return message;
    }
}