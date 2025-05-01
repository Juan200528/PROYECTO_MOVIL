package com.juan.movil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;

public class MenuActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private SharedPreferences sharedPreferences;
    private ImageView profileImageToolbar;
    private TextView userNameToolbar;
    private LinearLayout profileContainer;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); // Asegúrate de que este layout incluye app_bar_menu

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("PanascOOP");

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        profileImageToolbar = findViewById(R.id.profile_image_toolbar);
        userNameToolbar = findViewById(R.id.user_name_toolbar);
        profileContainer = findViewById(R.id.profile_container);
        fab = findViewById(R.id.fab);

        // Cargar el nombre de usuario
        loadUserName();

        // Cargar la foto de perfil si existe
        loadProfileImage();

        // Configurar el OnClickListener para mostrar el PopupMenu
        profileContainer.setOnClickListener(v -> showPopupMenu(v));

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
        String profileImagePath = sharedPreferences.getString("profile_image_path", null);
        if (profileImagePath != null) {
            File profileImageFile = new File(profileImagePath);
            if (profileImageFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; // Adjust as needed
                profileImageToolbar.setImageBitmap(BitmapFactory.decodeFile(profileImagePath, options));
            } else {
                profileImageToolbar.setImageResource(R.drawable.ic_default_profile);
                Toast.makeText(this, "Error al cargar la imagen de perfil", Toast.LENGTH_SHORT).show();
            }
        } else {
            profileImageToolbar.setImageResource(R.drawable.ic_default_profile);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_profile) {
                Toast.makeText(this, "Ir al perfil", Toast.LENGTH_SHORT).show(); // Placeholder
                return true;
            } else if (itemId == R.id.action_logout) {
                cerrarSesion();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void cerrarSesion() {
        sharedPreferences.edit().clear().apply();
        //  Intent intent = new Intent(this, LoginActivity.class); // Reemplaza LoginActivity con tu actividad de inicio de sesión
        //  startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Infla el menú genérico (puede que quieras quitarlo o adaptarlo)
        // getMenuInflater().inflate(R.menu.menu, menu);
        return false; // Deshabilitar el menú de opciones de la Toolbar
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}