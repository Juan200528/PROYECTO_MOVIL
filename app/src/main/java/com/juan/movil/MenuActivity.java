package com.juan.movil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class MenuActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private SharedPreferences sharedPreferences;
    private ShapeableImageView profileImageToolbar;
    private TextView userNameToolbar;
    private LinearLayout profileContainer;
    private FloatingActionButton fab;
    private Uri currentImageUri = null;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("PanascOOP");

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        profileImageToolbar = findViewById(R.id.profile_image_toolbar);
        userNameToolbar = findViewById(R.id.user_name_toolbar);
        fab = findViewById(R.id.fab);

        loadUserName();
        loadProfileImage();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            currentImageUri = selectedImage;
                            profileImageToolbar.setImageURI(currentImageUri);
                            sharedPreferences.edit().putString("image_uri", selectedImage.toString()).apply();
                            Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        profileImageToolbar.setOnClickListener(v -> showProfilePopup());

        fab.setOnClickListener(view -> Snackbar.make(view, R.string.snackbar_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_action, null)
                .setAnchorView(R.id.fab)
                .show());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_principal,
                R.id.nav_buscar,
                R.id.nav_lista,
                R.id.nav_promocionadas,
                R.id.nav_notificaciones,
                R.id.nav_redes,
                R.id.nav_asistencia
        ).setOpenableLayout(drawer).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void loadUserName() {
        String userName = sharedPreferences.getString("user_name", "Usuario");
        userNameToolbar.setText(userName);
    }

    private void loadProfileImage() {
        String imageUri = sharedPreferences.getString("image_uri", null);
        if (imageUri != null) {
            currentImageUri = Uri.parse(imageUri);
            profileImageToolbar.setImageURI(currentImageUri);
        } else {
            profileImageToolbar.setImageResource(R.drawable.ic_persona);
        }
    }

    private void showProfilePopup() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setCancelable(true);

        View popupView = getLayoutInflater().inflate(R.layout.popup_profile, null);
        builder.setView(popupView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ShapeableImageView popupProfileImage = popupView.findViewById(R.id.popup_profile_image);
        Button btnLogout = popupView.findViewById(R.id.btn_logout);

        if (currentImageUri != null) {
            popupProfileImage.setImageURI(currentImageUri);
        } else {
            popupProfileImage.setImageResource(R.drawable.ic_persona);
        }

        popupProfileImage.setOnClickListener(v -> {
            dialog.dismiss();
            showFullImagePopup();
        });

        btnLogout.setOnClickListener(v -> {
            sharedPreferences.edit().clear().apply();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, LoginActivity.class)); // Si lo necesitas
            finish();
        });

        dialog.show();
    }

    private void showFullImagePopup() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setCancelable(true);

        View view = getLayoutInflater().inflate(R.layout.popup_imagen_grande, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ShapeableImageView fullImage = view.findViewById(R.id.full_profile_image);
        Button btnCambiar = view.findViewById(R.id.btn_cambiar_foto);
        Button btnEliminar = view.findViewById(R.id.btn_eliminar_foto);
        ImageButton btnCerrar = view.findViewById(R.id.btn_close_popup);

        if (currentImageUri != null) {
            fullImage.setImageURI(currentImageUri);
        } else {
            fullImage.setImageResource(R.drawable.ic_persona);
        }

        btnCambiar.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });

        btnEliminar.setOnClickListener(v -> {
            currentImageUri = null;
            sharedPreferences.edit().remove("image_uri").apply();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false; // Desactivado
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
