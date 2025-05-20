package com.juan.movil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.juan.movil.api.ApiService;
import com.juan.movil.model.LoginRequest;
import com.juan.movil.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InicioSesion extends AppCompatActivity {

    EditText etCorreo, etContrasena;
    AppCompatButton btnIniciarSesion;
    TextView tvRegistro;

    ApiService apiService;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvRegistro = findViewById(R.id.tvRegistro);

        // Configurar botón con estilo
        configurarBotonIniciarSesion();

        // Texto "¿No tienes una cuenta? Regístrate" con estilos y acción
        configurarTextoRegistrate();

        // Retrofit y API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-nrpu.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Listener del botón
        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Rellenar correo si viene desde Registro
        String emailRegistrado = getIntent().getStringExtra("email_registrado");
        if (emailRegistrado != null) {
            etCorreo.setText(emailRegistrado);
        }
    }

    private void configurarBotonIniciarSesion() {
        btnIniciarSesion.setBackground(null);

        GradientDrawable normal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#03683E"), Color.parseColor("#064349")});
        normal.setCornerRadius(80f);

        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(Color.parseColor("#063449"));
        pressed.setCornerRadius(80f);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{}, normal);

        btnIniciarSesion.setBackground(states);
    }

    private void configurarTextoRegistrate() {
        String textoCompleto = "¿No tienes una cuenta? Registrate";
        SpannableString spannable = new SpannableString(textoCompleto);

        // Color del texto fijo
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#064349")),
                0, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Color del texto clickeable
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#39B1E0")),
                22, textoCompleto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Acción clickeable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(InicioSesion.this, Registro.class));
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(Color.parseColor("#39B1E0"));
            }
        };

        spannable.setSpan(clickableSpan, 22, textoCompleto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvRegistro.setText(spannable);
        tvRegistro.setMovementMethod(LinkMovementMethod.getInstance());
        tvRegistro.setHighlightColor(Color.TRANSPARENT);
    }

    private void iniciarSesion() {
        String email = etCorreo.getText().toString().trim();
        String password = etContrasena.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);

        apiService.loginUsuario(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    String nombre = response.body().getNombre(); // Asegúrate de que el backend lo envíe

                    guardarCredenciales(token, nombre);
                    Toast.makeText(InicioSesion.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(InicioSesion.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(InicioSesion.this, "Credenciales incorrectas", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(InicioSesion.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void guardarCredenciales(String token, String nombre) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("user_name", nombre != null ? nombre : "Usuario");
        editor.apply();
    }
}
