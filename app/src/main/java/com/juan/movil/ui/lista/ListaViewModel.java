package com.juan.movil.ui.lista;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.juan.movil.api.ApiService;
import com.juan.movil.model.ListaResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ListaViewModel extends ViewModel {

    private final MutableLiveData<List<ListaResponse>> actividades = new MutableLiveData<>();

    public LiveData<List<ListaResponse>> getActividades() {
        return actividades;
    }

    public void obtenerActividadesDesdeApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-nrpu.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<List<ListaResponse>> call = apiService.obtenerActividades();
        call.enqueue(new Callback<List<ListaResponse>>() {
            @Override
            public void onResponse(Call<List<ListaResponse>> call, Response<List<ListaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    actividades.postValue(response.body());
                } else {
                    actividades.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<ListaResponse>> call, Throwable t) {
                actividades.postValue(null);
            }
        });
    }
}
