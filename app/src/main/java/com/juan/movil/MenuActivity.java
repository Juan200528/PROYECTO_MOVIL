package com.juan.movil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private SharedPreferences sharedPreferences;
    private ImageView toolbarProfileImage;
    private ImageView popupProfileImage;
    private String currentProfileImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        toolbarProfileImage = findViewById(R.id.profile_image_toolbar);

        // Cargar la ruta de la imagen actual
        currentProfileImagePath = sharedPreferences.getString("profile_image_path", null);

        // Cargar imagen de perfil
        loadProfileImage(toolbarProfileImage);

        toolbarProfileImage.setOnClickListener(v -> openImagePicker());
        findViewById(R.id.profile_container).setOnClickListener(v -> showProfilePopup(v));
    }

    private void loadProfileImage(ImageView imageView) {
        if (currentProfileImagePath != null && new File(currentProfileImagePath).exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(currentProfileImagePath);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile image: " + e.getMessage());
                setDefaultImage(imageView);
            }
        } else {
            setDefaultImage(imageView);
        }
    }

    private void setDefaultImage(ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_person);
    }

    private void showProfilePopup(View anchorView) {
        try {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_profile, null);

            TextView welcomeText = popupView.findViewById(R.id.welcomeText);
            popupProfileImage = popupView.findViewById(R.id.profileImage);
            TextView btnCerrarSesion = popupView.findViewById(R.id.btnCerrarSesion);

            String name = sharedPreferences.getString("user_name", "Usuario");
            welcomeText.setText("Bienvenido, " + name);

            // Cargar la misma imagen en el popup
            loadProfileImage(popupProfileImage);

            // Permitir cambiar la imagen desde el popup también
            popupProfileImage.setOnClickListener(v -> openImagePicker());

            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true
            );

            popupWindow.setElevation(10f);
            popupWindow.showAsDropDown(anchorView, 0, 20);

            btnCerrarSesion.setOnClickListener(v -> {
                cerrarSesion();
                popupWindow.dismiss();
            });

        } catch (Exception e) {
            Log.e(TAG, "Error showing profile popup: " + e.getMessage());
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    // Guardar la imagen en almacenamiento interno
                    String newImagePath = saveImageToInternalStorage(imageUri);

                    if (newImagePath != null) {
                        // Actualizar la ruta actual
                        currentProfileImagePath = newImagePath;

                        // Guardar en preferencias
                        sharedPreferences.edit()
                                .putString("profile_image_path", newImagePath)
                                .apply();

                        // Actualizar todas las vistas
                        updateAllProfileImages();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving image: " + e.getMessage());
                }
            }
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        // Crear directorio si no existe
        File directory = new File(getFilesDir(), "profile_images");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Crear archivo
        File file = new File(directory, "user_profile.jpg");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return file.getAbsolutePath();
        }
    }

    private void updateAllProfileImages() {
        if (currentProfileImagePath != null) {
            // Actualizar imagen en la barra de herramientas
            loadProfileImage(toolbarProfileImage);

            // Actualizar imagen en el popup si está visible
            if (popupProfileImage != null) {
                loadProfileImage(popupProfileImage);
            }
        }
    }

    private void cerrarSesion() {
        sharedPreferences.edit().clear().apply();

        // Opcional: eliminar la imagen guardada al cerrar sesión
        if (currentProfileImagePath != null) {
            new File(currentProfileImagePath).delete();
        }

        finish();
    }
}