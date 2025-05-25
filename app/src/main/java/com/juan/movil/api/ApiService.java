package com.juan.movil.api;

import com.juan.movil.model.CrearListaRequest;
import com.juan.movil.model.CrearListaResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("/actividades")
    Call<CrearListaResponse> crearActividad(
            @Part("titulo") RequestBody titulo,
            @Part("descripcion") RequestBody descripcion,
            @Part("fecha") RequestBody fecha,
            @Part("lugar") RequestBody lugar,
            @Part("responsables") RequestBody responsables,
            @Part("idCreador") RequestBody idCreador,
            @Part MultipartBody.Part imagen
    );
}
