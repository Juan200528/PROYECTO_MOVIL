package com.juan.movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class InicioSesion extends AppCompatActivity {

    private EditText etCorreo, etContrasena;
    private AppCompatButton btnIniciarSesion;
    private TextView tvRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvRegistro = findViewById(R.id.tvRegistro);

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí podrías validar correo/contraseña si quieres

                // Redirigir al menú
                Intent intent = new Intent(InicioSesion.this, MenuActivity.class);
                startActivity(intent);
                finish(); // Esto evita volver a InicioSesion con el botón "atrás"
            }
        });

        tvRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InicioSesion.this, Registro.class);
                startActivity(intent);
            }
        });
    }
}
