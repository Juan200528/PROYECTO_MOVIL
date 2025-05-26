package com.juan.movil;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.juan.movil.api.ApiService;
import com.juan.movil.db.ManagerDb;
import com.juan.movil.model.CrearActividadResponse;
import com.juan.movil.models.Actividad;

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
    private static final int TARGET_WIDTH_DP = 200;
    private static final int TARGET_HEIGHT_DP = 200;
    private static final int IMAGE_RADIUS_DP = 16;

    private EditText etTitulo, etDescripcion, etFecha, etLugar, etResponsables;
    private Button btnCrear;
    private ImageButton btnSeleccionarImagen, btnFecha;
    private ImageView ivImagen;

    private String imagenRuta = null;
    private Calendar calendario = Calendar.getInstance();
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private SharedPreferences sharedPreferences;
    private ManagerDb managerDb;
    private ApiService apiService;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmapProcesado = procesarImagen(selectedImageUri);
                            if (bitmapProcesado != null) {
                                ivImagen.setImageBitmap(bitmapProcesado);
                                imagenRuta = saveOriginalImage(selectedImageUri);
                            } else {
                                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando imagen: ", e);
                            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_actividad);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        etLugar = findViewById(R.id.etLugar);
        etResponsables = findViewById(R.id.etResponsables);
        btnCrear = findViewById(R.id.btnCrear);
        btnSeleccionarImagen = findViewById(R.id.btnSubir);
        btnFecha = findViewById(R.id.btnCalendario);
        ivImagen = findViewById(R.id.ivActividadImagen);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        managerDb = new ManagerDb(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-nrpu.onrender.com/")  // Cambia por tu URL real
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        btnSeleccionarImagen.setOnClickListener(v -> seleccionarImagen());
        btnFecha.setOnClickListener(v -> mostrarCalendario());
        btnCrear.setOnClickListener(v -> guardarActividad());
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
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

            Bitmap scaledBitmap = escalarAlCentro(bitmapOriginal, targetWidth, targetHeight);
            return aplicarMascaraRedondeada(scaledBitmap, targetWidth, targetHeight);
        }
    }

    private int calcularFactorEscalado(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        while ((width / inSampleSize) > reqWidth * 2 || (height / inSampleSize) > reqHeight * 2) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    private Bitmap escalarAlCentro(Bitmap original, int targetWidth, int targetHeight) {
        float srcAspect = (float) original.getWidth() / original.getHeight();
        float dstAspect = (float) targetWidth / targetHeight;

        Rect srcRect = new Rect();
        if (srcAspect > dstAspect) {
            int srcWidth = (int) (original.getHeight() * dstAspect);
            int left = (original.getWidth() - srcWidth) / 2;
            srcRect.set(left, 0, left + srcWidth, original.getHeight());
        } else {
            int srcHeight = (int) (original.getWidth() / dstAspect);
            int top = (original.getHeight() - srcHeight) / 2;
            srcRect.set(0, top, original.getWidth(), top + srcHeight);
        }

        return Bitmap.createBitmap(original, srcRect.left, srcRect.top, srcRect.width(), srcRect.height());
    }

    private Bitmap aplicarMascaraRedondeada(Bitmap bitmap, int ancho, int alto) {
        Bitmap output = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, ancho, alto);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, dpToPx(IMAGE_RADIUS_DP), dpToPx(IMAGE_RADIUS_DP), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        return output;
    }

    private String saveOriginalImage(Uri imageUri) throws Exception {
        File directory = new File(getFilesDir(), "actividad_imagenes");
        if (!directory.exists()) directory.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        File file = new File(directory, "IMG_" + timeStamp + ".jpg");

        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return file.getAbsolutePath();
    }

    private void mostrarCalendario() {
        DatePickerDialog dialogo = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog,
                (view, año, mes, dia) -> actualizarFechaSeleccionada(año, mes, dia),
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH));
        dialogo.setTitle("Seleccione una fecha");
        dialogo.show();
    }

    private void actualizarFechaSeleccionada(int año, int mes, int dia) {
        calendario.set(Calendar.YEAR, año);
        calendario.set(Calendar.MONTH, mes);
        calendario.set(Calendar.DAY_OF_MONTH, dia);
        etFecha.setText(formatoFecha.format(calendario.getTime()));
    }

    private void guardarActividad() {
        if (!validarCamposObligatorios()) return;

        try {
            Date fechaSeleccionada = formatoFecha.parse(etFecha.getText().toString().trim());
            Date fechaActual = new Date();

            Calendar calSelected = Calendar.getInstance();
            calSelected.setTime(fechaSeleccionada);
            calSelected.set(Calendar.HOUR_OF_DAY, 0);
            calSelected.set(Calendar.MINUTE, 0);
            calSelected.set(Calendar.SECOND, 0);
            calSelected.set(Calendar.MILLISECOND, 0);

            Calendar calNow = Calendar.getInstance();
            calNow.setTime(fechaActual);
            calNow.set(Calendar.HOUR_OF_DAY, 0);
            calNow.set(Calendar.MINUTE, 0);
            calNow.set(Calendar.SECOND, 0);
            calNow.set(Calendar.MILLISECOND, 0);

            if (calSelected.before(calNow)) {
                Toast.makeText(this, "La fecha no puede ser anterior a hoy", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCrear.setEnabled(false);
            btnCrear.setText("Creando...");

            String token = sharedPreferences.getString("token", null);
            if (token == null) {
                Toast.makeText(this, "No autorizado. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
                btnCrear.setEnabled(true);
                btnCrear.setText("Crear Actividad");
                return;
            }

            // Preparar datos para enviar a la API
            RequestBody titulo = RequestBody.create(MediaType.parse("text/plain"), etTitulo.getText().toString().trim());
            RequestBody descripcion = RequestBody.create(MediaType.parse("text/plain"), etDescripcion.getText().toString().trim());
            RequestBody fecha = RequestBody.create(MediaType.parse("text/plain"), etFecha.getText().toString().trim());
            RequestBody lugar = RequestBody.create(MediaType.parse("text/plain"), etLugar.getText().toString().trim());
            RequestBody responsables = RequestBody.create(MediaType.parse("text/plain"), etResponsables.getText().toString().trim());

            int idCreador = sharedPreferences.getInt("user_id", -1);
            if (idCreador == -1) {
                Toast.makeText(this, "Error con el usuario. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
                btnCrear.setEnabled(true);
                btnCrear.setText("Crear Actividad");
                return;
            }
            RequestBody idCreadorBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idCreador));

            MultipartBody.Part parteImagen = null;
            if (imagenRuta != null) {
                File archivoImagen = new File(imagenRuta);
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), archivoImagen);
                parteImagen = MultipartBody.Part.createFormData("imagen", archivoImagen.getName(), requestFile);
            }

            Call<CrearActividadResponse> call = apiService.crearActividad(
                    "Bearer " + token,
                    titulo, descripcion, fecha, lugar, responsables, idCreadorBody, parteImagen
            );

            call.enqueue(new Callback<CrearActividadResponse>() {
                @Override
                public void onResponse(@NonNull Call<CrearActividadResponse> call, @NonNull Response<CrearActividadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        CrearActividadResponse respuesta = response.body();
                        if ("ok".equalsIgnoreCase(respuesta.getEstado())) {
                            // Guardar en la BD local
                            Actividad actividad = respuesta.getActividad();
                            if (actividad != null) {
                                long id = managerDb.insertarActividad(actividad);
                                if (id != -1) {
                                    Toast.makeText(CrearActividad.this, "Actividad creada exitosamente", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(CrearActividad.this, "Error guardando en la BD local", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(CrearActividad.this, "Error al obtener la actividad creada", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CrearActividad.this, "Error al crear la actividad: " + respuesta.getMensaje(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CrearActividad.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                    btnCrear.setEnabled(true);
                    btnCrear.setText("Crear Actividad");
                }

                @Override
                public void onFailure(@NonNull Call<CrearActividadResponse> call, @NonNull Throwable t) {
                    Toast.makeText(CrearActividad.this, "Error al crear la actividad: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    btnCrear.setEnabled(true);
                    btnCrear.setText("Crear Actividad");
                }
            });

        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al guardar actividad", e);
        }
    }

    private boolean validarCamposObligatorios() {
        if (etTitulo.getText().toString().trim().isEmpty()) {
            etTitulo.setError("Ingrese un título");
            etTitulo.requestFocus();
            return false;
        }
        if (etDescripcion.getText().toString().trim().isEmpty()) {
            etDescripcion.setError("Ingrese una descripción");
            etDescripcion.requestFocus();
            return false;
        }
        if (etFecha.getText().toString().trim().isEmpty()) {
            etFecha.setError("Ingrese una fecha");
            etFecha.requestFocus();
            return false;
        }
        if (etLugar.getText().toString().trim().isEmpty()) {
            etLugar.setError("Ingrese un lugar");
            etLugar.requestFocus();
            return false;
        }
        if (etResponsables.getText().toString().trim().isEmpty()) {
            etResponsables.setError("Ingrese responsables");
            etResponsables.requestFocus();
            return false;
        }
        return true;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
