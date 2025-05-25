package com.juan.movil.ui.principal;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.movil.CrearActividad;
import com.juan.movil.R;
import com.juan.movil.models.Actividad;
import com.juan.movil.models.ActividadAdapter;
import com.juan.movil.db.ManagerDb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PrincipalFragment extends Fragment implements ActividadAdapter.OnActividadClickListener {

    private RecyclerView recyclerActividades;
    private TextView tvMisActividades;
    private TextView tvEmptyActividades;
    private Button btnCreateActivity;
    private ActividadAdapter actividadAdapter;
    private ManagerDb managerDb;
    private List<ActividadAdapter.Item> itemList;
    private ActivityResultLauncher<Intent> createActivityLauncher;
    private boolean isRecyclerVisible = false;
    private int userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        cargarActividades();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_principal, container, false);

        recyclerActividades = root.findViewById(R.id.recyclerActividades);
        tvMisActividades = root.findViewById(R.id.tvMisActividades);
        tvEmptyActividades = root.findViewById(R.id.tvEmptyActividades);
        btnCreateActivity = root.findViewById(R.id.button_create_activity);

        tvMisActividades.setTextColor(Color.parseColor("#0865FE"));
        tvMisActividades.setPaintFlags(tvMisActividades.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        managerDb = new ManagerDb(getContext());
        managerDb.open();

        // ✅ Leer user_id como String y convertirlo a int
        String userIdStr = requireContext()
                .getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE)
                .getString("user_id", "-1");

        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            userId = -1;
            Log.e("PrincipalFragment", "Error al convertir user_id a entero", e);
        }

        recyclerActividades.setLayoutManager(new LinearLayoutManager(getContext()));
        itemList = new ArrayList<>();
        actividadAdapter = new ActividadAdapter(itemList, this, this::mostrarDialogoEliminar,
                this::mostrarDialogoEditar, this::mostrarDialogoDetalles);
        actividadAdapter.setManagerDb(managerDb);
        recyclerActividades.setAdapter(actividadAdapter);

        recyclerActividades.setVisibility(View.GONE);
        tvEmptyActividades.setVisibility(View.GONE);

        cargarActividades();

        tvMisActividades.setOnClickListener(v -> {
            isRecyclerVisible = !isRecyclerVisible;
            actualizarVisibilidad();
        });

        btnCreateActivity.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CrearActividad.class);
            createActivityLauncher.launch(intent);
        });

        return root;
    }

    private void cargarActividades() {
        itemList.clear();
        if (userId != -1) {
            List<Actividad> noPasadas = managerDb.obtenerActividadesPorUsuario(userId);
            for (Actividad actividad : noPasadas) {
                Log.d("PrincipalFragment", "No pasada: " + actividad.getTitulo() + ", Fecha: " + actividad.getFecha());
                itemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_ACTIVIDAD, actividad, null, null));
            }

            List<Actividad> actividadesPasadas = managerDb.obtenerActividadesPasadasPorUsuario(userId);
            if (!actividadesPasadas.isEmpty()) {
                itemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_TITULO, null, getString(R.string.actividades_pasadas), null));
                itemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_PASADAS, null, null, actividadesPasadas));
                for (Actividad actividad : actividadesPasadas) {
                    Log.d("PrincipalFragment", "Pasada: " + actividad.getTitulo() + ", Fecha: " + actividad.getFecha());
                }
            }
        }
        actividadAdapter.notifyDataSetChanged();
        actualizarVisibilidad();
    }

    private void actualizarVisibilidad() {
        if (isRecyclerVisible) {
            recyclerActividades.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
            tvEmptyActividades.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            recyclerActividades.setVisibility(View.GONE);
            tvEmptyActividades.setVisibility(View.GONE);
        }
    }

    private void mostrarDialogoEliminar(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_eliminar_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnConfirmar.setOnClickListener(v -> {
            managerDb.eliminarActividad(actividad.getId());
            cargarActividades();
            Toast.makeText(getContext(), "Actividad eliminada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoEditar(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_editar_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
        EditText etEditarTitulo = dialog.findViewById(R.id.etEditarTitulo);
        EditText etEditarDescripcion = dialog.findViewById(R.id.etEditarDescripcion);
        EditText etEditarFecha = dialog.findViewById(R.id.etEditarFecha);
        EditText etEditarLugar = dialog.findViewById(R.id.etEditarLugar);
        EditText etEditarResponsables = dialog.findViewById(R.id.etEditarResponsables);
        Button btnGuardarCambios = dialog.findViewById(R.id.btnGuardarCambios);

        etEditarTitulo.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "");
        etEditarDescripcion.setText(actividad.getDescripcion() != null ? actividad.getDescripcion() : "");
        etEditarFecha.setText(actividad.getFecha() != null ? actividad.getFecha() : "");
        etEditarLugar.setText(actividad.getLugar() != null ? actividad.getLugar() : "");
        etEditarResponsables.setText(actividad.getResponsables() != null ? actividad.getResponsables() : "");

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        btnGuardarCambios.setOnClickListener(v -> {
            actividad.setTitulo(etEditarTitulo.getText().toString());
            actividad.setDescripcion(etEditarDescripcion.getText().toString());
            String nuevaFecha = etEditarFecha.getText().toString();
            actividad.setFecha(nuevaFecha);
            actividad.setLugar(etEditarLugar.getText().toString());
            actividad.setResponsables(etEditarResponsables.getText().toString());

            managerDb.actualizarActividad(actividad);
            cargarActividades();
            Toast.makeText(getContext(), "Actividad actualizada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoDetalles(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);
        TextView tvTituloDetalle = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcionDetalle = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFechaDetalle = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugarDetalle = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsablesDetalle = dialog.findViewById(R.id.tvResponsablesDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        tvTituloDetalle.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "Sin título");
        tvDescripcionDetalle.setText(actividad.getDescripcion() != null ? actividad.getDescripcion() : "Sin descripción");
        tvFechaDetalle.setText(actividad.getFecha() != null ? actividad.getFecha() : "Sin fecha");
        tvLugarDetalle.setText(actividad.getLugar() != null ? actividad.getLugar() : "Sin lugar");
        tvResponsablesDetalle.setText(actividad.getResponsables() != null ? actividad.getResponsables() : "Sin responsables");

        String imagenRuta = actividad.getImagenRuta();
        if (imagenRuta != null && !imagenRuta.isEmpty()) {
            File imgFile = new File(imagenRuta);
            if (imgFile.exists()) {
                ivImagenDetalle.setImageURI(Uri.fromFile(imgFile));
            } else {
                ivImagenDetalle.setImageResource(R.drawable.default_image);
            }
        } else {
            ivImagenDetalle.setImageResource(R.drawable.default_image);
        }

        btnVolver.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (managerDb != null) {
            managerDb.close();
        }
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        Toast.makeText(getContext(), "Clic en actividad: " + (actividad.getTitulo() != null ? actividad.getTitulo() : "Sin título"), Toast.LENGTH_SHORT).show();
    }
}