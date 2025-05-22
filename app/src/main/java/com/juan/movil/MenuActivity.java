package com.juan.movil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.juan.movil.PantallaPrincipal;
import com.juan.movil.R;

public class MenuActivity extends AppCompatActivity {

    private TextView userNameToolbar;
    private TextView contentTitle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    private ShapeableImageView profileImageToolbar;
    private Uri currentImageUri = null;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Validar sesión y redirigir si no existe
        String nombre = sharedPreferences.getString("user_name", null);
        String email = sharedPreferences.getString("user_email", null);
        if (nombre == null || email == null) {
            Intent intent = new Intent(this, PantallaPrincipal.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        contentTitle = findViewById(R.id.content_title);
        userNameToolbar = findViewById(R.id.user_name_toolbar);
        profileImageToolbar = findViewById(R.id.profile_image_toolbar);

        cargarNombreUsuario();
        loadProfileImage();

        profileImageToolbar.setOnClickListener(v -> showProfilePopup());

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            currentImageUri = selectedImage;
                            profileImageToolbar.setImageURI(currentImageUri);
                            sharedPreferences.edit().putString("image_uri", currentImageUri.toString()).apply();
                            Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_menu);
        navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_principal, R.id.nav_buscar, R.id.nav_lista, R.id.nav_promocionadas,
                R.id.nav_notificaciones, R.id.nav_redes, R.id.nav_asistencia)
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setItemBackgroundResource(R.drawable.nav_item_background);

        contentTitle.setText("PanascOOP");
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            String label = destination.getLabel() != null ? destination.getLabel().toString() : "Sin título";
            contentTitle.setText(label);
        });
    }

    private void loadProfileImage() {
        String imageUriString = sharedPreferences.getString("image_uri", null);
        if (imageUriString != null) {
            try {
                currentImageUri = Uri.parse(imageUriString);
                profileImageToolbar.setImageURI(currentImageUri);
            } catch (Exception e) {
                profileImageToolbar.setImageResource(R.drawable.ic_persona);
            }
        } else {
            profileImageToolbar.setImageResource(R.drawable.ic_persona);
        }
    }

    private void cargarNombreUsuario() {
        String nombreCompleto = sharedPreferences.getString("user_name", "Usuario");
        String primerNombre = "Usuario";
        if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
            String[] partes = nombreCompleto.trim().split("\\s+");
            if (partes.length > 0) {
                primerNombre = partes[0];
            }
        }
        userNameToolbar.setText(primerNombre);
    }

    // ✅ MÉTODO MODIFICADO
    private void cerrarSesion() {
        sharedPreferences.edit().clear().apply();
        Intent intent = new Intent(this, PantallaPrincipal.class); // Redirige a InicioSesion
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showProfilePopup() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View popupView = getLayoutInflater().inflate(R.layout.popup_profile, null);
        builder.setView(popupView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ShapeableImageView popupProfileImage = popupView.findViewById(R.id.popup_profile_image);
        TextView tvNombre = popupView.findViewById(R.id.tvNombre);
        TextView tvCorreo = popupView.findViewById(R.id.tvCorreo);
        Button btnLogout = popupView.findViewById(R.id.btn_logout);

        if (currentImageUri != null) {
            popupProfileImage.setImageURI(currentImageUri);
        } else {
            popupProfileImage.setImageResource(R.drawable.ic_person);
        }

        String nombreCompleto = sharedPreferences.getString("user_name", "Usuario");
        String correo = sharedPreferences.getString("user_email", "admin@email.com");

        tvNombre.setText(nombreCompleto);
        tvCorreo.setText(correo);

        popupProfileImage.setOnClickListener(v -> {
            dialog.dismiss();
            showFullImagePopup();
        });

        btnLogout.setOnClickListener(v -> {
            cerrarSesion(); // Usamos el método corregido
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showFullImagePopup() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_imagen_grande, null);
        builder.setView(view);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
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
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
