package com.juan.movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.juan.movil.api.ApiService;
import com.juan.movil.models.RegistroRequest;
import com.juan.movil.models.RegistroResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Registro extends AppCompatActivity {

    EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    TextView loginLinkTextView;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Vincular vistas
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        loginLinkTextView = findViewById(R.id.loginLinkTextView);

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-nrpu.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Registrar botón
        findViewById(R.id.btnRegistrar).setOnClickListener(v -> registrarUsuario());

        // Redirigir al login si ya tiene cuenta
        loginLinkTextView.setOnClickListener(v -> {
            startActivity(new Intent(Registro.this, InicioSesion.class));
            finish();
        });
    }

    private void registrarUsuario() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Validaciones
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        RegistroRequest request = new RegistroRequest(username, email, password);

        // Enviar solicitud de registro
        Call<RegistroResponse> call = apiService.registrarUsuario(request);
        call.enqueue(new Callback<RegistroResponse>() {
            @Override
            public void onResponse(Call<RegistroResponse> call, Response<RegistroResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Registro.this, "Registro exitoso", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Registro.this, InicioSesion.class));
                    finish();
                } else {
                    Toast.makeText(Registro.this, "Error en registro, verifica tus datos", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RegistroResponse> call, Throwable t) {
                Toast.makeText(Registro.this, "Error en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
