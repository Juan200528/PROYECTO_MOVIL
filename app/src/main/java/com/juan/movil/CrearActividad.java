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
import com.juan.movil.db.ManagerDb;
import com.juan.movil.model.CrearActividadRequest;
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
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
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

    // URL del backend - Reemplaza con tu URL real
    // VVVV IMPORTANTÍSIMO: Cambia esta URL por la de tu backend real VVVV
    private static final String BASE_URL = "https://tu-backend-url.com/api/"; // <--- ¡AQUÍ!

    private EditText etTitulo, etDescripcion, etFecha, etLugar, etResponsables, etImage;
    private ImageButton btnCalendario, btnSubir;
    private Button btnCrear;
    private ImageView ivActividadImagen;
    private Calendar calendario = Calendar.getInstance();
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private ManagerDb managerDb;
    private SharedPreferences sharedPreferences;
    private String imagenRuta;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Servicio API para comunicación con backend
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_actividad);

        inicializarVistas();
        configurarBaseDatos();
        configurarApiService();
        configurarImagePicker();
        configurarBotones();

        // Solicitar permiso de lectura de almacenamiento externo si no está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Asegurarse de cerrar la base de datos cuando la actividad se destruye
        if (managerDb != null) {
            managerDb.close();
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
        // Establecer una imagen por defecto
        ivActividadImagen.setImageResource(R.drawable.default_image);
    }

    private void configurarBaseDatos() {
        managerDb = new ManagerDb(this);
        managerDb.open(); // Abrir la conexión a la base de datos
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
    }

    private void configurarApiService() {
        // Configurar interceptor de logging para ver las peticiones y respuestas en Logcat
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Configurar cliente HTTP con tiempos de espera y el interceptor de logging
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS) // Tiempo máximo para establecer la conexión
                .readTimeout(30, TimeUnit.SECONDS)    // Tiempo máximo para leer la respuesta
                .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo máximo para escribir la petición
                .build();

        // Crear instancia de Retrofit para las llamadas al API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-nrpu.onrender.com") // La URL base de tu API
                .client(okHttpClient) // Usar el cliente HTTP configurado
                .addConverterFactory(GsonConverterFactory.create()) // Convertidor de JSON a objetos Java
                .build();

        // Inicializar el servicio API a partir de la interfaz
        apiService = retrofit.create(ApiService.class);
    }

    private void configurarImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            // Procesar la imagen (escalar, redondear) para mostrarla en el ImageView
                            Bitmap processedBitmap = procesarImagen(imageUri);
                            ivActividadImagen.setImageBitmap(processedBitmap);
                            // Guardar la imagen original en el almacenamiento interno de la app
                            imagenRuta = saveOriginalImage(imageUri);
                            etImage.setText("Imagen seleccionada ✅"); // Indicar que se seleccionó una imagen
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar imagen: ", e);
                            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void configurarBotones() {
        // Configurar el botón de calendario para mostrar el DatePickerDialog
        btnCalendario.setOnClickListener(v -> mostrarCalendario());
        // También permitir abrir el calendario al tocar el EditText de fecha
        etFecha.setOnClickListener(v -> mostrarCalendario());
        // Configurar el botón de subir imagen para abrir el selector de imágenes
        btnSubir.setOnClickListener(v -> seleccionarImagen());

        // --- Configuración visual del botón "Crear" con gradiente y estado de presionado ---
        // Gradiente para el estado normal del botón
        GradientDrawable gradientDrawableNormal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#03683E"), Color.parseColor("#064349")});
        gradientDrawableNormal.setCornerRadius(80f); // Esquinas redondeadas

        // Color sólido para el estado presionado del botón
        GradientDrawable gradientDrawablePressed = new GradientDrawable();
        gradientDrawablePressed.setColor(Color.parseColor("#063449"));
        gradientDrawablePressed.setCornerRadius(80f);

        // Crear un StateListDrawable para manejar los diferentes estados del botón
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, gradientDrawablePressed); // Estado presionado
        stateListDrawable.addState(new int[]{}, gradientDrawableNormal); // Estado normal
        btnCrear.setBackground(stateListDrawable); // Aplicar el estilo al botón

        // Configurar el listener para el botón "Crear"
        btnCrear.setOnClickListener(v -> guardarActividad());
    }

    // Abre el selector de imágenes de la galería
    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Procesa la imagen seleccionada para redimensionarla y aplicarle una máscara redondeada
    private Bitmap procesarImagen(Uri imageUri) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // Decodificar solo los límites de la imagen

        // Leer dimensiones de la imagen sin cargarla en memoria completamente
        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(is, null, options);
        }

        // Calcular las dimensiones de destino en píxeles
        int targetWidth = dpToPx(TARGET_WIDTH_DP);
        int targetHeight = dpToPx(TARGET_HEIGHT_DP);
        // Calcular el factor de escalado (inSampleSize) para decodificar eficientemente
        options.inSampleSize = calcularFactorEscalado(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false; // Ahora decodificar la imagen completa

        // Decodificar la imagen escalada y aplicar la máscara redondeada
        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmapOriginal = BitmapFactory.decodeStream(is, null, options);
            if (bitmapOriginal == null) return null;

            Bitmap scaledBitmap = escalarAlCentro(bitmapOriginal, targetWidth, targetHeight);
            return aplicarMascaraRedondeada(scaledBitmap, targetWidth, targetHeight);
        }
    }

    // Calcula el factor de escalado (inSampleSize) para BitmapFactory
    private int calcularFactorEscalado(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        // Si la imagen es más grande que las dimensiones requeridas, reducirla
        while ((width / inSampleSize) > reqWidth * 2 || (height / inSampleSize) > reqHeight * 2) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    // Escala y recorta un bitmap al centro para ajustarlo a las dimensiones de destino manteniendo el aspecto
    private Bitmap escalarAlCentro(Bitmap original, int targetWidth, int targetHeight) {
        float srcAspect = (float) original.getWidth() / original.getHeight();
        float dstAspect = (float) targetWidth / targetHeight;

        Rect srcRect = new Rect();
        if (srcAspect > dstAspect) {
            // Recortar ancho para ajustarse al alto
            int srcWidth = (int) (original.getHeight() * dstAspect);
            int left = (original.getWidth() - srcWidth) / 2;
            srcRect.set(left, 0, left + srcWidth, original.getHeight());
        } else {
            // Recortar alto para ajustarse al ancho
            int srcHeight = (int) (original.getWidth() / dstAspect);
            int top = (original.getHeight() - srcHeight) / 2;
            srcRect.set(0, top, original.getWidth(), top + srcHeight);
        }

        return Bitmap.createBitmap(original, srcRect.left, srcRect.top, srcRect.width(), srcRect.height());
    }

    // Aplica una máscara redondeada a un bitmap
    private Bitmap aplicarMascaraRedondeada(Bitmap bitmap, int ancho, int alto) {
        Bitmap output = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, ancho, alto);
        RectF rectF = new RectF(rect); // RectF para esquinas redondeadas
        paint.setAntiAlias(true); // Suavizado de bordes
        canvas.drawRoundRect(rectF, dpToPx(IMAGE_RADIUS_DP), dpToPx(IMAGE_RADIUS_DP), paint); // Dibujar el rectángulo redondeado
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); // Modo de transferencia para "recortar" la imagen
        canvas.drawBitmap(bitmap, null, rect, paint); // Dibujar la imagen sobre el rectángulo redondeado
        return output;
    }

    // Guarda la imagen original seleccionada en el almacenamiento interno de la aplicación
    private String saveOriginalImage(Uri imageUri) throws Exception {
        // Crear un directorio para las imágenes de actividades si no existe
        File directory = new File(getFilesDir(), "actividad_imagenes");
        if (!directory.exists()) directory.mkdirs();

        // Generar un nombre de archivo único basado en la fecha y hora
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        File file = new File(directory, "IMG_" + timeStamp + ".jpg");

        // Copiar el contenido de la imagen de la URI al archivo local
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return file.getAbsolutePath(); // Devolver la ruta absoluta del archivo guardado
    }

    // Muestra el diálogo de selección de fecha
    private void mostrarCalendario() {
        DatePickerDialog dialogo = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog, // Estilo del diálogo
                (view, año, mes, dia) -> actualizarFechaSeleccionada(año, mes, dia), // Listener para cuando se selecciona una fecha
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH));
        dialogo.setTitle("Seleccione una fecha");
        dialogo.show();
    }

    // Actualiza el campo de fecha con la fecha seleccionada en el calendario
    private void actualizarFechaSeleccionada(int año, int mes, int dia) {
        calendario.set(Calendar.YEAR, año);
        calendario.set(Calendar.MONTH, mes);
        calendario.set(Calendar.DAY_OF_MONTH, dia);
        etFecha.setText(formatoFecha.format(calendario.getTime()));
    }

    // Guarda la actividad, primero intentando enviarla al backend y luego guardando localmente
    private void guardarActividad() {
        // Validar campos obligatorios antes de continuar
        if (!validarCamposObligatorios()) return;

        // Validar que la fecha no sea anterior a hoy
        try {
            Date fechaSeleccionada = formatoFecha.parse(etFecha.getText().toString().trim());
            Date fechaActual = new Date(); // Obtener la fecha actual

            // Comparar solo la fecha, ignorando la hora
            Calendar calSelected = Calendar.getInstance();
            calSelected.setTime(fechaSeleccionada);
            calSelected.set(Calendar.HOUR_OF_DAY, 0);
            calSelected.set(Calendar.MINUTE, 0);
            calSelected.set(Calendar.SECOND, 0);
            calSelected.set(Calendar.MILLISECOND, 0);

            Calendar calActual = Calendar.getInstance();
            calActual.setTime(fechaActual);
            calActual.set(Calendar.HOUR_OF_DAY, 0);
            calActual.set(Calendar.MINUTE, 0);
            calActual.set(Calendar.SECOND, 0);
            calActual.set(Calendar.MILLISECOND, 0);


            if (calSelected.before(calActual)) {
                Toast.makeText(this, "La fecha no puede ser anterior a hoy", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construir el objeto de solicitud para el backend
        CrearActividadRequest request = new CrearActividadRequest();
        request.setTitulo(etTitulo.getText().toString().trim());
        request.setDescripcion(etDescripcion.getText().toString().trim());
        request.setFecha(etFecha.getText().toString().trim());
        request.setLugar(etLugar.getText().toString().trim());
        request.setResponsables(etResponsables.getText().toString().trim());
        // Obtener el ID del creador de SharedPreferences
        request.setIdCreador(sharedPreferences.getInt("user_id", -1));
        request.setEstado("activo"); // Estado por defecto
        request.setImagenRuta(imagenRuta != null ? imagenRuta : ""); // Ruta de la imagen guardada localmente

        // Deshabilitar el botón y cambiar el texto para indicar que se está guardando
        btnCrear.setEnabled(false);
        btnCrear.setText("Guardando...");

        // Intentar guardar en el backend primero
        enviarActividadAlBackend(request);
    }

    // Envía la solicitud de creación de actividad al backend
    private void enviarActividadAlBackend(CrearActividadRequest request) {
        String token = sharedPreferences.getString("auth_token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "No hay token de autenticación.", Toast.LENGTH_SHORT).show();
            btnCrear.setEnabled(true);
            btnCrear.setText("Crear Actividad");
            return;
        }

        // Crear RequestBody para los campos de texto
        RequestBody titulo = RequestBody.create(request.getTitulo(), okhttp3.MultipartBody.FORM);
        RequestBody descripcion = RequestBody.create(request.getDescripcion(), okhttp3.MultipartBody.FORM);
        RequestBody fecha = RequestBody.create(request.getFecha(), okhttp3.MultipartBody.FORM);
        RequestBody lugar = RequestBody.create(request.getLugar(), okhttp3.MultipartBody.FORM);
        RequestBody responsables = RequestBody.create(request.getResponsables(), okhttp3.MultipartBody.FORM);

        MultipartBody.Part imagenPart = null;

        // Preparar imagen si existe ruta y archivo
        if (request.getImagenRuta() != null && !request.getImagenRuta().isEmpty()) {
            File file = new File(request.getImagenRuta());
            if (file.exists()) {
                RequestBody requestFile = RequestBody.create(file, okhttp3.MediaType.parse("image/*"));
                imagenPart = MultipartBody.Part.createFormData("imagen", file.getName(), requestFile);
            }
        }

        Call<CrearActividadResponse> call = apiService.crearActividad(
                "Bearer " + token,
                titulo,
                descripcion,
                fecha,
                lugar,
                responsables,
                imagenPart
        );

        call.enqueue(new Callback<CrearActividadResponse>() {
            @Override
            public void onResponse(Call<CrearActividadResponse> call, Response<CrearActividadResponse> response) {
                btnCrear.setEnabled(true);
                btnCrear.setText("Crear Actividad");

                if (response.isSuccessful() && response.body() != null) {
                    CrearActividadResponse actividadResponse = response.body();

                    if (actividadResponse.isSuccess()) {
                        guardarActividadLocalDesdeResponse(actividadResponse, request);
                        Toast.makeText(CrearActividad.this, actividadResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String errorMsg = actividadResponse.getError() != null ?
                                actividadResponse.getError() : "Error desconocido del servidor";
                        Toast.makeText(CrearActividad.this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error del servidor: " + errorMsg);
                    }
                } else {
                    Log.e(TAG, "Error HTTP: " + response.code() + " - " + response.message());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Error Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer errorBody: ", e);
                    }
                    Toast.makeText(CrearActividad.this, "Error al conectar con el servidor. Guardando localmente.", Toast.LENGTH_LONG).show();
                    guardarActividadLocalSoloRequest(request);
                }
            }

            @Override
            public void onFailure(Call<CrearActividadResponse> call, Throwable t) {
                btnCrear.setEnabled(true);
                btnCrear.setText("Crear Actividad");

                Log.e(TAG, "Error de conexión o red: " + t.getMessage(), t);
                Toast.makeText(CrearActividad.this, "No se pudo conectar al servidor. Actividad guardada localmente.", Toast.LENGTH_LONG).show();
                guardarActividadLocalSoloRequest(request);
            }
        });
    }

    // Guarda la actividad localmente utilizando los datos de la respuesta del backend (si disponible)
    private void guardarActividadLocalDesdeResponse(CrearActividadResponse response, CrearActividadRequest request) {
        Actividad actividad = new Actividad();

        if (response.getData() != null) {
            // Usar los datos de la respuesta del servidor si están disponibles y son válidos
            CrearActividadResponse.ActividadData data = response.getData();
            actividad.setTitulo(data.getTitulo());
            actividad.setDescripcion(data.getDescripcion());
            actividad.setFecha(data.getFecha());
            actividad.setLugar(data.getLugar());
            actividad.setResponsables(data.getResponsables());
            actividad.setIdCreador(data.getIdCreador());
            actividad.setEstado(data.getEstado());
            actividad.setImagenRuta(data.getImagenRuta()); // Usar la ruta de imagen del backend si la envía
        } else {
            // Fallback: Si no hay datos en la respuesta del servidor, usar los del request original
            Log.w(TAG, "No hay datos de actividad en la respuesta del servidor. Usando datos del request.");
            actividad.setTitulo(request.getTitulo());
            actividad.setDescripcion(request.getDescripcion());
            actividad.setFecha(request.getFecha());
            actividad.setLugar(request.getLugar());
            actividad.setResponsables(request.getResponsables());
            actividad.setIdCreador(request.getIdCreador());
            actividad.setEstado(request.getEstado());
            actividad.setImagenRuta(request.getImagenRuta());
        }

        long resultado = managerDb.insertarActividad(actividad);
        if (resultado == -1) {
            Log.e(TAG, "Error al guardar actividad localmente después del éxito en servidor");
            Toast.makeText(this, "Error local al guardar la actividad.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Actividad guardada localmente (desde respuesta del servidor)");
        }
    }

    // Guarda la actividad localmente usando los datos de la solicitud original (usado como fallback)
    private void guardarActividadLocalSoloRequest(CrearActividadRequest request) {
        Actividad actividad = new Actividad();
        actividad.setTitulo(request.getTitulo());
        actividad.setDescripcion(request.getDescripcion());
        actividad.setFecha(request.getFecha());
        actividad.setLugar(request.getLugar());
        actividad.setResponsables(request.getResponsables());
        actividad.setIdCreador(request.getIdCreador());
        actividad.setEstado(request.getEstado());
        actividad.setImagenRuta(request.getImagenRuta());

        long resultado = managerDb.insertarActividad(actividad);
        if (resultado != -1) {
            Toast.makeText(this, "Actividad guardada localmente (sin conexión)", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error al crear la actividad localmente.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al guardar actividad localmente (sin conexión)");
        }
    }

    // Valida que los campos obligatorios no estén vacíos
    private boolean validarCamposObligatorios() {
        if (etTitulo.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etDescripcion.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "La descripción es obligatoria", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etFecha.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "La fecha es obligatoria", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etLugar.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "El lugar es obligatorio", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Convierte unidades de densidad de píxeles (dp) a píxeles (px)
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}