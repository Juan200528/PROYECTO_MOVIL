package com.juan.movil.ui.promocionadas;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
//import com.example.movil_figma.db.ManagerDb;
//import com.example.movil_figma.model.Actividad;
import java.util.List;

public class PromocionadasViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Actividad>> actividadesPromocionadas;
    private final ManagerDb managerDb;

    public PromocionadasViewModel(Application application) {
        super(application);
        actividadesPromocionadas = new MutableLiveData<>();
        managerDb = new ManagerDb(application);
        cargarActividadesPromocionadas();
    }

    public LiveData<List<Actividad>> getActividadesPromocionadas() {
        return actividadesPromocionadas;
    }

    public void cargarActividadesPromocionadas() {
        managerDb.open();
        List<Actividad> lista = managerDb.obtenerActividadesPromocionadas();
        actividadesPromocionadas.setValue(lista);
        managerDb.close();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        managerDb.close();
    }
}