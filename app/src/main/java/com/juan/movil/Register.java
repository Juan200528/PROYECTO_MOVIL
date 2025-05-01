package com.juan.movil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginLinkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Asegúrate que coincida con tu XML

        // Inicializar vistas
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginLinkTextView = findViewById(R.id.loginLinkTextView);

        // Configurar el listener para el botón de Registro
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });

        // Configurar el listener para el texto de Iniciar Sesión
        loginLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir a la actividad de login
                Intent intent = new Intent(Register.this, Inicio_Sesion.class);
                startActivity(intent);
                finish(); // Opcional: cierra esta actividad
            }
        });
    }

    private void registrarUsuario() {
        String nombreCompleto = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombreCompleto)) {
            fullNameEditText.setError("Ingrese su nombre completo");
            fullNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Ingrese su correo electrónico");
            emailEditText.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Ingrese un correo válido");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Ingrese una contraseña");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Las contraseñas no coinciden");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Si todas las validaciones pasan
        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();

        // Aquí iría la lógica para registrar el usuario en tu backend
        // Por ejemplo: registrarEnFirebase(nombreCompleto, email, password);

        // Redirigir a la pantalla principal después del registro
        Intent intent = new Intent(Register.this, Inicio_Sesion.class);
        startActivity(intent);
        finish(); // Cierra esta actividad para que no se pueda volver atrás
    }

    // Método de ejemplo para registrar en Firebase (opcional)
    /*
    private void registrarEnFirebase(String nombre, String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Registro exitoso
                    guardarDatosAdicionales(nombre, email);
                } else {
                    // Error en el registro
                    Toast.makeText(RegistroActivity.this,
                        "Error: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    */
}