package com.juan.movil.ui.configuracion;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.juan.movil.db.ManagerDb;
import com.juan.movil.model.Notificacion;
import java.util.List;

public class ConfiguracionViewModel extends AndroidViewModel {
    private ManagerDb managerDb;
    private MutableLiveData<List<Notificacion>> notificaciones;

    public ConfiguracionViewModel(Application application) {
        super(application);
        managerDb = new ManagerDb(application);
        managerDb.open();
        notificaciones = new MutableLiveData<>();
    }

    public LiveData<List<Notificacion>> getNotificaciones() {
        return notificaciones;
    }

    public void cargarNotificaciones(int userId) {
        List<Notificacion> notificacionList = managerDb.obtenerNotificacionesPorUsuario(userId);
        notificaciones.setValue(notificacionList);
    }

    public void insertarNotificacion(Notificacion notificacion) {
        managerDb.insertarNotificacion(notificacion);
        actualizarNotificaciones(notificacion.getIdUsuario());
    }

    public void actualizarNotificaciones(int userId) {
        managerDb.actualizarDiasRestantes();
        List<Notificacion> notificacionList = managerDb.obtenerNotificacionesPorUsuario(userId);
        notificaciones.setValue(notificacionList);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        managerDb.close();
    }
}