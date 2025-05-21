package com.juan.movil.ui.lista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.movil.R;
import com.juan.movil.models.ListaResponse;

import java.util.ArrayList;
import java.util.List;

public class ListaFragment extends Fragment {

    private RecyclerView recyclerView;
    private ListaAdapter adapter;
    private ListaViewModel viewModel;
    private List<ListaResponse> listaActividades = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista, container, false);

        recyclerView = view.findViewById(R.id.recyclerLista);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ListaAdapter(listaActividades);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ListaViewModel.class);
        viewModel.getActividades().observe(getViewLifecycleOwner(), actividades -> {
            listaActividades.clear();
            if (actividades != null) {
                listaActividades.addAll(actividades);
            }
            adapter.notifyDataSetChanged();
        });

        viewModel.obtenerActividadesDesdeApi();

        return view;
    }
}
