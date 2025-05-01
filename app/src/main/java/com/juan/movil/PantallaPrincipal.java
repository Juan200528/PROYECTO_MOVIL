package com.juan.movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class PantallaPrincipal extends AppCompatActivity {

    private Button btnRegister;
    private AppCompatButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal); // Asegúrate que coincide con tu XML

        // Inicializar los botones
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        // Configurar el listener para el botón de Registro
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir actividad de registro
                Intent intent = new Intent(PantallaPrincipal.this, Register.class);
                startActivity(intent);
            }
        });

        // Configurar el listener para el botón de Iniciar Sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir actividad de login
                Intent intent = new Intent(PantallaPrincipal.this, Inicio_Sesion.class);
                startActivity(intent);
            }
        });
    }
}