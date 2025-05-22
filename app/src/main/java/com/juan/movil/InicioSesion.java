package com.juan.movil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.juan.movil.db.ManagerDb;

public class InicioSesion extends AppCompatActivity {

    private EditText etCorreo, etContrasena;
    private Button btnIniciarSesion;
    private TextView tvRegistro;
    private ManagerDb managerDb;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvRegistro = findViewById(R.id.tvRegistro);

        managerDb = new ManagerDb(this);
        managerDb.open();

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        configurarBotonIniciarSesion();
        configurarTextoRegistrate();

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Si viene un email de registro previo, ponerlo en el campo correo
        String emailRegistrado = getIntent().getStringExtra("email_registrado");
        if (emailRegistrado != null) {
            etCorreo.setText(emailRegistrado);
        }
    }

    private void configurarBotonIniciarSesion() {
        btnIniciarSesion.setBackground(null);

        GradientDrawable gradientDrawableNormal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#03683E"), Color.parseColor("#064349")});
        gradientDrawableNormal.setCornerRadius(80f);

        GradientDrawable gradientDrawablePressed = new GradientDrawable();
        gradientDrawablePressed.setColor(Color.parseColor("#063449"));
        gradientDrawablePressed.setCornerRadius(80f);

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, gradientDrawablePressed);
        stateListDrawable.addState(new int[]{}, gradientDrawableNormal);

        btnIniciarSesion.setBackground(stateListDrawable);
    }

    private void configurarTextoRegistrate() {
        String fullText = "¿No tienes una cuenta? Registrate";
        SpannableString spannableString = new SpannableString(fullText);

        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#064349")),
                0, 22,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#39B1E0")),
                22, fullText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(InicioSesion.this, Registro.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#39B1E0"));
                ds.setUnderlineText(false);
            }
        };

        spannableString.setSpan(
                clickableSpan,
                22, fullText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvRegistro.setText(spannableString);
        tvRegistro.setMovementMethod(LinkMovementMethod.getInstance());
        tvRegistro.setHighlightColor(Color.TRANSPARENT);
    }

    private void iniciarSesion() {
        String email = etCorreo.getText().toString().trim();
        String password = etContrasena.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = managerDb.validarUsuario(email, password);
        if (userId != -1) {
            String nombreCompleto = managerDb.getUserNameById(userId);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_id", userId);
            editor.putString("user_email", email);
            editor.putString("user_name", (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) ? nombreCompleto : "Usuario");
            editor.apply();

            Intent intent = new Intent(this, com.juan.movil.MenuActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (managerDb != null) {
            managerDb.close();
        }
    }
}
