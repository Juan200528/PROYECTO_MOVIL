package com.juan.movil;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.juan.movil.api.ApiService;
import com.juan.movil.model.CrearListaResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CrearActividad extends AppCompatActivity {
    private static final String TAG = "CrearActividad";
    private static final int TARGET_WIDTH_DP = 84;
    private static final int TARGET_HEIGHT_DP = 56;
    private static final int IMAGE_RADIUS_DP = 16;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;

    private EditText etTitulo, etDescripcion, etFecha, etLugar, etResponsables, etImage;
    private ImageButton btnCalendario, btnSubir;
    private Button btnCrear;
    private ImageView ivActividadImagen;
    private Calendar calendario = Calendar.getInstance();
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private SharedPreferences sharedPreferences;
    private String imagenRuta;
    private Uri imagenUri; // Guardar Uri para enviar

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_actividad);

        inicializarVistas();
        configurarSharedPreferences();
        configurarImagePicker();
        configurarBotones();
        configurarRetrofit();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    private void inicializarVistas() {
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        etLugar = findViewById(R.id.etLugar);
        etResponsables = findViewById(R.id.etResponsables);
        etImage = findViewById(R.id.etImage);
        btnCalendario = findViewById(R.id.btnCalendario);
        btnSubir = findViewById(R.id.btnSubir);
        btnCrear = findViewById(R.id.btnCrear);
        ivActividadImagen = findViewById(R.id.ivActividadImagen);
        ivActividadImagen.setImageResource(R.drawable.default_image);
    }

    private void configurarSharedPreferences() {
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
    }

    private void configurarImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagenUri = result.getData().getData();
                        try {
                            Bitmap processedBitmap = procesarImagen(imagenUri);
                            ivActividadImagen.setImageBitmap(processedBitmap);
                            imagenRuta = saveOriginalImage(imagenUri);
                            etImage.setText("Imagen seleccionada ✅");
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar imagen: ", e);
                            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void configurarBotones() {
        btnCalendario.setOnClickListener(v -> mostrarCalendario());
        etFecha.setOnClickListener(v -> mostrarCalendario());
        btnSubir.setOnClickListener(v -> seleccionarImagen());

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
        btnCrear.setBackground(stateListDrawable);

        btnCrear.setOnClickListener(v -> enviarActividad());
    }

    private void configurarRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-nrpu.onrender.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private Bitmap procesarImagen(Uri imageUri) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(is, null, options);
        }

        int targetWidth = dpToPx(TARGET_WIDTH_DP);
        int targetHeight = dpToPx(TARGET_HEIGHT_DP);
        options.inSampleSize = calcularFactorEscalado(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;

        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmapOriginal = BitmapFactory.decodeStream(is, null, options);
            if (bitmapOriginal == null) return null;

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapOriginal, targetWidth, targetHeight, true);
            return recortarImagenConBordesRedondeados(scaledBitmap, dpToPx(IMAGE_RADIUS_DP));
        }
    }

    private Bitmap recortarImagenConBordesRedondeados(Bitmap bitmap, int radiusPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private int calcularFactorEscalado(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void mostrarCalendario() {
        int year = calendario.get(Calendar.YEAR);
        int month = calendario.get(Calendar.MONTH);
        int day = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    etFecha.setText(fechaSeleccionada);
                }, year, month, day);
        datePickerDialog.show();
    }

    private String saveOriginalImage(Uri imageUri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        if (inputStream == null) return null;

        File file = new File(getCacheDir(), "imagen_actividad.jpg");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return file.getAbsolutePath();
    }

    private void enviarActividad() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String lugar = etLugar.getText().toString().trim();
        String responsables = etResponsables.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() || fecha.isEmpty() || lugar.isEmpty() || responsables.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarFecha(fecha)) {
            Toast.makeText(this, "Formato de fecha incorrecto (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
            return;
        }

        int idCreador = sharedPreferences.getInt("user_id", -1);
        if (idCreador == -1) {
            Toast.makeText(this, "Usuario no identificado. Por favor inicia sesión.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagenRuta == null || imagenRuta.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        File archivoImagen = new File(imagenRuta);
        if (!archivoImagen.exists()) {
            Toast.makeText(this, "Archivo de imagen no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestTitulo = RequestBody.create(titulo, MediaType.parse("text/plain"));
        RequestBody requestDescripcion = RequestBody.create(descripcion, MediaType.parse("text/plain"));
        RequestBody requestFecha = RequestBody.create(fecha, MediaType.parse("text/plain"));
        RequestBody requestLugar = RequestBody.create(lugar, MediaType.parse("text/plain"));
        RequestBody requestResponsables = RequestBody.create(responsables, MediaType.parse("text/plain"));
        RequestBody requestIdCreador = RequestBody.create(String.valueOf(idCreador), MediaType.parse("text/plain"));

        RequestBody requestFile = RequestBody.create(archivoImagen, MediaType.parse("image/*"));
        MultipartBody.Part imagenPart = MultipartBody.Part.createFormData("imagen", archivoImagen.getName(), requestFile);

        Call<CrearListaResponse> call = apiService.crearActividad(
                requestTitulo,
                requestDescripcion,
                requestFecha,
                requestLugar,
                requestResponsables,
                requestIdCreador,
                imagenPart);

        call.enqueue(new Callback<CrearListaResponse>() {
            @Override
            public void onResponse(Call<CrearListaResponse> call, Response<CrearListaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CrearListaResponse resp = response.body();
                    if (resp.isSuccess()) {
                        Toast.makeText(CrearActividad.this, "Actividad creada con éxito", Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                    } else {
                        Toast.makeText(CrearActividad.this, "Error: " + resp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CrearActividad.this, "Error al crear la actividad", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CrearListaResponse> call, Throwable t) {
                Toast.makeText(CrearActividad.this, "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private boolean validarFecha(String fecha) {
        try {
            Date date = formatoFecha.parse(fecha);
            return date != null;
        } catch (ParseException e) {
            return false;
        }
    }

    private void limpiarCampos() {
        etTitulo.setText("");
        etDescripcion.setText("");
        etFecha.setText("");
        etLugar.setText("");
        etResponsables.setText("");
        etImage.setText("");
        ivActividadImagen.setImageResource(R.drawable.default_image);
        imagenRuta = null;
        imagenUri = null;
    }
}
