package com.juan.movil.ui.principal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.juan.movil.R; // Asegúrate de que R sea la referencia correcta a tus recursos

public class PrincipalFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_principal_fragment, container, false);

        // Aquí puedes obtener referencias a tus Views (Button, TextView, etc.)
        // utilizando root.findViewById(R.id.button_create_activity);
        // y root.findViewById(R.id.text_my_activities);
        // y configurar Listeners o realizar otras acciones.

        return root;
    }
}
