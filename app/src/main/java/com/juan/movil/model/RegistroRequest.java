package com.juan.movil.model;

public class RegistroRequest {
    private String name;
    private String email;
    private String password;

    public RegistroRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
