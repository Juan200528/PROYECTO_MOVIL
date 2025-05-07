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
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private static final int PICK_IMAGE_REQUEST = 1;  // Código para seleccionar imagen
    private SharedPreferences sharedPreferences;
    private ImageView toolbarProfileImage;  // Imagen en la barra de herramientas
    private ImageView popupProfileImage;   // Imagen en el popup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        toolbarProfileImage = findViewById(R.id.profile_image_toolbar);

        // Cargar imagen de perfil cuando la actividad se inicie
        loadToolbarProfileImage();

        // Abrir la galería cuando se haga clic en la imagen de perfil
        toolbarProfileImage.setOnClickListener(v -> openImagePicker());

        // Mostrar el Popup cuando el usuario haga clic en su perfil
        findViewById(R.id.profile_container).setOnClickListener(v -> showProfilePopup(v));
    }

    private void loadToolbarProfileImage() {
        // Cargar la imagen de perfil guardada en SharedPreferences
        String imagePath = sharedPreferences.getString("profile_image_path", null);
        if (imagePath != null && new File(imagePath).exists()) {
            toolbarProfileImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        } else {
            toolbarProfileImage.setImageResource(R.drawable.ic_person); // Imagen predeterminada
        }
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

            // Cargar imagen de perfil para el popup
            loadProfileImageForPopup();

            // Configurar el clic para cambiar imagen también en el popup
            popupProfileImage.setOnClickListener(v -> openImagePicker());

            final PopupWindow popupWindow = new PopupWindow(
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
            Log.e(TAG, "Error al mostrar el perfil popup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProfileImageForPopup() {
        // Cargar la imagen de perfil desde las preferencias compartidas
        String imagePath = sharedPreferences.getString("profile_image_path", null);
        if (imagePath != null && new File(imagePath).exists()) {
            popupProfileImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        } else {
            popupProfileImage.setImageResource(R.drawable.ic_person);
        }
    }

    private void openImagePicker() {
        // Abrir la galería para que el usuario elija una imagen
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                // Obtener el InputStream de la URI de la imagen seleccionada
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);

                // Actualizar ambas imágenes
                toolbarProfileImage.setImageBitmap(selectedImage);
                if (popupProfileImage != null) {
                    popupProfileImage.setImageBitmap(selectedImage);
                }

                // Guardar la URI de la imagen seleccionada en SharedPreferences
                sharedPreferences.edit().putString("profile_image_path", imageUri.toString()).apply();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cerrarSesion() {
        sharedPreferences.edit().clear().apply();
        finish(); // Cerrar la actividad actual (cerrar sesión)
    }
}