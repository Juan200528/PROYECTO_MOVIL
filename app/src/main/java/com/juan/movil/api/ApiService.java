package com.juan.movil.api;

import com.juan.movil.model.LoginRequest;
import com.juan.movil.model.LoginResponse;
import com.juan.movil.model.RegistroRequest;
import com.juan.movil.model.RegistroResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // Endpoint for user registration
    @POST("/api/auth/register")
    Call<RegistroResponse> registrarUsuario(@Body RegistroRequest registroRequest);

    // Endpoint for user login
    @POST("/api/auth/login")
    Call<LoginResponse> loginUsuario(@Body LoginRequest loginRequest);
}