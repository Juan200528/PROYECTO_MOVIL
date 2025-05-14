package com.juan.movil;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

public class MenuActivity extends AppCompatActivity {

    private ShapeableImageView profileImageToolbar;
    private Uri currentImageUri = null;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        profileImageToolbar = findViewById(R.id.profile_image_toolbar);
        // Imagen por defecto
        profileImageToolbar.setImageResource(R.drawable.ic_persona);

        // Al hacer clic en la imagen de perfil, se muestra el popup
        profileImageToolbar.setOnClickListener(v -> showProfilePopup());

        // Configuramos el lanzador para seleccionar imagen de la galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            currentImageUri = selectedImage;
                            // Actualiza la imagen en el toolbar
                            profileImageToolbar.setImageURI(currentImageUri);
                            Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showProfilePopup() {
        // Usamos MaterialAlertDialogBuilder para un estilo material
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setCancelable(true);

        // Inflamos el layout del popup
        android.view.View popupView = getLayoutInflater().inflate(R.layout.popup_profile, null);
        builder.setView(popupView);

        // Creamos el diálogo y personalizamos el fondo
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Obtenemos las vistas del popup
        ShapeableImageView popupProfileImage = popupView.findViewById(R.id.popup_profile_image);
        Button btnLogout = popupView.findViewById(R.id.btn_logout);

        // Asigna la imagen actual o la imagen por defecto
        if (currentImageUri != null) {
            popupProfileImage.setImageURI(currentImageUri);
        } else {
            popupProfileImage.setImageResource(R.drawable.ic_persona);
        }

        // Al hacer clic en la imagen se cierra este popup y se abre el de imagen completa
        popupProfileImage.setOnClickListener(v -> {
            dialog.dismiss();
            showFullImagePopup();
        });

        // Cierre de sesión (puedes agregar lógica adicional aquí)
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showFullImagePopup() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setCancelable(true);

        android.view.View view = getLayoutInflater().inflate(R.layout.popup_imagen_grande, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Obtenemos las vistas del popup de imagen completa
        ShapeableImageView fullImage = view.findViewById(R.id.full_profile_image);
        Button btnCambiar = view.findViewById(R.id.btn_cambiar_foto);
        Button btnEliminar = view.findViewById(R.id.btn_eliminar_foto);
        ImageButton btnCerrar = view.findViewById(R.id.btn_close_popup);

        // Asigna la imagen actual o la por defecto
        if (currentImageUri != null) {
            fullImage.setImageURI(currentImageUri);
        } else {
            fullImage.setImageResource(R.drawable.ic_persona);
        }

        // Al cambiar imagen, se abre la galería
        btnCambiar.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });

        // Eliminar imagen: se restaura la imagen por defecto
        btnEliminar.setOnClickListener(v -> {
            currentImageUri = null;
            profileImageToolbar.setImageResource(R.drawable.ic_persona);
            Toast.makeText(this, "Imagen eliminada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
}
