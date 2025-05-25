package com.juan.movil.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("username")
    private String username;

    public LoginResponse() {
        // Constructor vacío para Gson
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
