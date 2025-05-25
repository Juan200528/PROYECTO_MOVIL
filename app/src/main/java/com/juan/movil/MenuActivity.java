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

public class MenuActivity extends AppCompatActivity {

    private TextView userNameToolbar, contentTitle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ShapeableImageView profileImageToolbar;
    private Uri currentImageUri = null;
    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Mostrar mensaje de bienvenida
        Toast.makeText(this, "¡Bienvenido al menú!", Toast.LENGTH_SHORT).show();

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        if (!verificarSesion()) return;

        inicializarUI();
        configurarToolbar();
        configurarDrawer();
        configurarNavegacion();

        // Configurar galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            currentImageUri = selectedImage;
                            profileImageToolbar.setImageURI(currentImageUri);
                            guardarImagenEnPreferencias(currentImageUri);
                            Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private boolean verificarSesion() {
        String nombre = sharedPreferences.getString("user_name", null);
        String email = sharedPreferences.getString("user_email", null);

        Toast.makeText(this, "Nombre: " + nombre + "\nEmail: " + email, Toast.LENGTH_LONG).show();

        if (nombre == null || email == null) {
            startActivity(new Intent(this, MenuActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return false;
        }
        return true;
    }


    private void inicializarUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        contentTitle = findViewById(R.id.content_title);
        userNameToolbar = findViewById(R.id.user_name_toolbar);
        profileImageToolbar = findViewById(R.id.profile_image_toolbar);

        cargarNombreUsuario();
        currentImageUri = obtenerImagenDesdePreferencias();
        if (currentImageUri != null) {
            profileImageToolbar.setImageURI(currentImageUri);
        } else {
            profileImageToolbar.setImageResource(R.drawable.ic_persona);
        }

        profileImageToolbar.setOnClickListener(v -> showProfilePopup());
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configurarDrawer() {
        navigationView.setItemBackgroundResource(R.drawable.nav_item_background);
    }

    private void configurarNavegacion() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_menu);
        navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_principal, R.id.nav_buscar, R.id.nav_lista,
                R.id.nav_promocionadas, R.id.nav_notificaciones,
                R.id.nav_redes, R.id.nav_asistencia)
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        contentTitle.setText("PanascOOP");
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            contentTitle.setText(destination.getLabel() != null ? destination.getLabel().toString() : "Sin título");
        });
    }

    private void guardarImagenEnPreferencias(Uri imageUri) {
        sharedPreferences.edit().putString("image_uri", imageUri.toString()).apply();
    }

    private Uri obtenerImagenDesdePreferencias() {
        String uriStr = sharedPreferences.getString("image_uri", null);
        return uriStr != null ? Uri.parse(uriStr) : null;
    }

    private void cargarNombreUsuario() {
        String nombre = sharedPreferences.getString("user_name", "Usuario");
        String primerNombre = nombre.split("\\s+")[0];
        userNameToolbar.setText(primerNombre);
    }

    private void cerrarSesion() {
        sharedPreferences.edit().clear().apply();
        startActivity(new Intent(this, PantallaPrincipal.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void showProfilePopup() {
        View view = getLayoutInflater().inflate(R.layout.popup_profile, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ShapeableImageView img = view.findViewById(R.id.popup_profile_image);
        TextView nombre = view.findViewById(R.id.tvNombre);
        TextView correo = view.findViewById(R.id.tvCorreo);
        Button logout = view.findViewById(R.id.btn_logout);

        img.setImageURI(currentImageUri != null ? currentImageUri : null);
        if (currentImageUri == null) img.setImageResource(R.drawable.ic_person);

        nombre.setText(sharedPreferences.getString("user_name", "Usuario"));
        correo.setText(sharedPreferences.getString("user_email", "admin@email.com"));

        img.setOnClickListener(v -> {
            dialog.dismiss();
            showFullImagePopup();
        });

        logout.setOnClickListener(v -> {
            cerrarSesion();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showFullImagePopup() {
        View view = getLayoutInflater().inflate(R.layout.popup_imagen_grande, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ShapeableImageView imageView = view.findViewById(R.id.full_profile_image);
        Button cambiar = view.findViewById(R.id.btn_cambiar_foto);
        Button eliminar = view.findViewById(R.id.btn_eliminar_foto);
        ImageButton cerrar = view.findViewById(R.id.btn_close_popup);

        imageView.setImageURI(currentImageUri != null ? currentImageUri : null);
        if (currentImageUri == null) imageView.setImageResource(R.drawable.ic_persona);

        cambiar.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });

        eliminar.setOnClickListener(v -> {
            currentImageUri = null;
            sharedPreferences.edit().remove("image_uri").apply();
            profileImageToolbar.setImageResource(R.drawable.ic_persona);
            Toast.makeText(this, "Imagen eliminada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        cerrar.setOnClickListener(v -> dialog.dismiss());
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
