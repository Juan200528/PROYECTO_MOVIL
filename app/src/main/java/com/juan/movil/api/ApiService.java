package com.juan.movil.api;

import com.juan.movil.model.LoginRequest;
import com.juan.movil.model.LoginResponse;
import com.juan.movil.model.RegistroRequest;
import com.juan.movil.model.RegistroResponse;
import com.juan.movil.model.ListaResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    // Endpoint para registrar un usuario
    @POST("/api/auth/register")
    Call<RegistroResponse> registrarUsuario(@Body RegistroRequest registroRequest);

    // Endpoint para iniciar sesión
    @POST("/api/auth/login")
    Call<LoginResponse> loginUsuario(@Body LoginRequest loginRequest);

    // Endpoint para obtener actividades (lista de todos los usuarios)
    @GET("/api/actividad")
    Call<List<ListaResponse>> obtenerActividades();
}
