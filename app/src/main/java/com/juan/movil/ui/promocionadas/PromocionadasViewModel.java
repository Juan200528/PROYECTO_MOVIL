package com.juan.movil.ui.promocionadas;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.juan.movil.api.ApiService;
import com.juan.movil.models.Actividad;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PromocionadasViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Actividad>> actividadesPromocionadas = new MutableLiveData<>();
    private final ApiService apiService;
    private static final String BASE_URL = "https://backend-nrpu.onrender.com/api/";
    private static final String TAG = "PromocionadasViewModel";

    public PromocionadasViewModel(@NonNull Application application) {
        super(application);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        cargarActividadesPromocionadas();
    }

    public LiveData<List<Actividad>> getActividadesPromocionadas() {
        return actividadesPromocionadas;
    }

    public void cargarActividadesPromocionadas() {
        String token = "Bearer " + obtenerToken();

        Call<List<Actividad>> call = apiService.obtenerActividadesPromocionadas(token);
        call.enqueue(new Callback<List<Actividad>>() {
            @Override
            public void onResponse(Call<List<Actividad>> call, Response<List<Actividad>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    actividadesPromocionadas.setValue(response.body());
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Actividad>> call, Throwable t) {
                Log.e(TAG, "Error en la red: " + t.getMessage(), t);
            }
        });
    }

    private String obtenerToken() {
        SharedPreferences prefs = getApplication().getSharedPreferences("sesion", Context.MODE_PRIVATE);
        return prefs.getString("token", ""); // Asegúrate de guardar el token al iniciar sesión
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
