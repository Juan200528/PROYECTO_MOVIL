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
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.juan.movil.api.ApiService;
import com.juan.movil.model.LoginRequest;
import com.juan.movil.model.LoginResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InicioSesion extends AppCompatActivity {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "user_name";

    private EditText etCorreo, etContrasena;
    private AppCompatButton btnIniciarSesion;
    private TextView tvRegistro;

    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvRegistro = findViewById(R.id.tvRegistro);

        configurarBotonIniciarSesion();
        configurarTextoRegistrate();
        initRetrofit();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Si se recibió un email desde registro, ponerlo en el campo
        String emailRegistrado = getIntent().getStringExtra("email_registrado");
        if (emailRegistrado != null) {
            etCorreo.setText(emailRegistrado);
        }
    }

    private void initRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-nrpu.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void configurarBotonIniciarSesion() {
        btnIniciarSesion.setBackground(null);

        GradientDrawable normal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#03683E"), Color.parseColor("#064349")}
        );
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
        String fullText = "¿No tienes una cuenta? Registrate";
        SpannableString spannable = new SpannableString(fullText);

        // Color al texto normal
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#064349")),
                0, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(InicioSesion.this, Registro.class));
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(Color.parseColor("#39B1E0")); // color del link
                ds.setUnderlineText(false); // sin subrayado
            }
        };

        // Aplicar click solo a la palabra "Registrate"
        spannable.setSpan(clickableSpan,
                22, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvRegistro.setText(spannable);
        tvRegistro.setMovementMethod(LinkMovementMethod.getInstance());
        tvRegistro.setHighlightColor(Color.TRANSPARENT);
    }

    private void iniciarSesion() {
        String email = etCorreo.getText().toString().trim();
        String password = etContrasena.getText().toString().trim();

        // Validaciones básicas
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);

        // Llamada asíncrona para iniciar sesión
        apiService.loginUsuario(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    String username = response.body().getUsername();

                    if (token == null || token.trim().isEmpty()) {
                        Toast.makeText(InicioSesion.this, "Token inválido. Credenciales incorrectas.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Guardar token y usuario en SharedPreferences
                    guardarCredenciales(token, username, email);

                    Toast.makeText(InicioSesion.this, "Inicio de sesión exitoso. Bienvenido, " + username, Toast.LENGTH_LONG).show();

                    // Navegar a la actividad menú
                    Intent intent = new Intent(InicioSesion.this, MenuActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    // Obtener error del body si es posible
                    String mensajeError = "Error al iniciar sesión";
                    try {
                        if (response.errorBody() != null)
                            mensajeError = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Toast.makeText(InicioSesion.this, mensajeError, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(InicioSesion.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void guardarCredenciales(String token, String nombre, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, (nombre != null && !nombre.trim().isEmpty()) ? nombre : "Usuario");
        editor.putString("user_email", email);
        editor.apply();
    }
}
