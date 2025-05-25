package com.juan.movil.ui.compartir;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.juan.movil.R;

public class CompartirFragment extends Fragment {

    private CompartirViewModel compartirViewModel;
    private EditText etDescripcion, etEnlace;
    private Button btnPublicar;
    private ImageButton btnFacebook, btnInstagram;
    private String selectedSocialMedia = null;

    public CompartirFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_compartir, container, false);

        compartirViewModel = new ViewModelProvider(this).get(CompartirViewModel.class);
        etDescripcion = root.findViewById(R.id.etDescripcion);
        etEnlace = root.findViewById(R.id.etEnlace);
        btnPublicar = root.findViewById(R.id.btnPublicar);
        btnFacebook = root.findViewById(R.id.btnFacebook);
        btnInstagram = root.findViewById(R.id.btnInstagram);

        // Cargar enlace desde argumentos si existe
        Bundle args = getArguments();
        if (args != null && args.containsKey("enlace")) {
            etEnlace.setText(args.getString("enlace"));
        }

        btnFacebook.setOnClickListener(v -> {
            selectedSocialMedia = "facebook";
            btnFacebook.setBackgroundResource(R.drawable.selected_social_media_background);
            btnInstagram.setBackgroundResource(android.R.color.transparent);
        });

        btnInstagram.setOnClickListener(v -> {
            selectedSocialMedia = "instagram";
            btnInstagram.setBackgroundResource(R.drawable.selected_social_media_background);
            btnFacebook.setBackgroundResource(android.R.color.transparent);
        });

        btnPublicar.setOnClickListener(v -> {
            String descripcion = etDescripcion.getText().toString().trim();
            String enlace = etEnlace.getText().toString().trim();

            if (descripcion.isEmpty() || enlace.isEmpty()) {
                Toast.makeText(getContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedSocialMedia == null) {
                Toast.makeText(getContext(), "Seleccione una red social", Toast.LENGTH_SHORT).show();
                return;
            }

            String shareText = descripcion + "\n" + enlace;
            Intent shareIntent = new Intent(Intent.ACTION_VIEW);
            if ("facebook".equals(selectedSocialMedia)) {
                shareIntent.setData(Uri.parse("https://www.facebook.com/sharer/sharer.php?u=" + Uri.encode(enlace)));
            } else if ("instagram".equals(selectedSocialMedia)) {
                shareIntent.setData(Uri.parse("https://www.instagram.com/share?url=" + Uri.encode(enlace)));
            }
            startActivity(shareIntent);

            Toast.makeText(getContext(), "Actividad compartida en " + selectedSocialMedia, Toast.LENGTH_SHORT).show();
            etDescripcion.setText("");
            selectedSocialMedia = null;
            btnFacebook.setBackgroundResource(android.R.color.transparent);
            btnInstagram.setBackgroundResource(android.R.color.transparent);
        });

        return root;
    }
}