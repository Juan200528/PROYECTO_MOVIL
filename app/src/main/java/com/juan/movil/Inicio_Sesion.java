package com.juan.movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Inicio_Sesion extends AppCompatActivity {

    private EditText etCorreo, etContrasena;
    private Button btnIniciarSesion;
    private TextView tvRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        // Inicializar vistas
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvRegistro = findViewById(R.id.tvRegistro);

        // Autocompletar correo si viene del registro
        String emailRegistrado = getIntent().getStringExtra("email_registrado");
        if(emailRegistrado != null) {
            etCorreo.setText(emailRegistrado);
            etContrasena.requestFocus(); // Mueve el foco a contraseña
        }

        // Configurar el listener para el botón de Iniciar Sesión
        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Configurar el listener para el texto de Registro
        tvRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(Inicio_Sesion.this, Register.class);
            startActivity(intent);
        });
    }

    private void iniciarSesion() {
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        // Validaciones básicas
        if (correo.isEmpty()) {
            etCorreo.setError("Ingrese su correo electrónico");
            etCorreo.requestFocus();
            return;
        }

        if (contrasena.isEmpty()) {
            etContrasena.setError("Ingrese su contraseña");
            etContrasena.requestFocus();
            return;
        }

        // Aquí iría la lógica de autenticación real
        // Ejemplo con Firebase Authentication:
        autenticarUsuario(correo, contrasena);
    }

    private void autenticarUsuario(String email, String password) {
        // Esto es un ejemplo con Firebase, reemplaza con tu lógica real
        /*
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Login exitoso
                    irAPantallaPrincipal();
                } else {
                    // Error en login
                    Toast.makeText(Inicio_Sesion.this,
                        "Error: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
        */

        // SIMULACIÓN TEMPORAL (eliminar cuando implementes autenticación real)
        if(!password.isEmpty()) { // Solo para pruebas
            irAPantallaPrincipal();
        }
    }

    private void irAPantallaPrincipal() {
        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PantallaPrincipal.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Cierra esta actividad
    }
}