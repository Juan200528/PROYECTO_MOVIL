package com.juan.movil.api;

import com.juan.movil.model.CrearActividadResponse;
import com.juan.movil.model.LoginRequest;
import com.juan.movil.model.LoginResponse;
import com.juan.movil.model.RegistroRequest;
import com.juan.movil.model.RegistroResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // Endpoint for user registration
    @POST("/api/auth/register")
    Call<RegistroResponse> registrarUsuario(@Body RegistroRequest registroRequest);

    // Endpoint for user login
    @POST("/api/auth/login")
    Call<LoginResponse> loginUsuario(@Body LoginRequest loginRequest);

    @Multipart
    @POST("actividades")
    Call<CrearActividadResponse> crearActividad(
            @Header("Authorization") String token,
            @Part("titulo") RequestBody titulo,
            @Part("descripcion") RequestBody descripcion,
            @Part("fecha") RequestBody fecha,
            @Part("lugar") RequestBody lugar,
            @Part("responsables") RequestBody responsables,
            @Part MultipartBody.Part imagen
    );

}