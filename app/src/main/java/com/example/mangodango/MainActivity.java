package com.example.mangodango;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.api.CommonStatusCodes;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private DatabaseHelper dbHelper;

    private static final String TAG = "InicioDeSesion";
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private Button button;
    private static final int REQ_ONE_TAP = 2;
    private boolean showOneTapUI = true;

    private int intentosFailedLogin = 0;
    private static final int MAX_INTENTOS = 200;
    private static final long TIEMPO_BLOQUEO = 30 * 60 * 1000; // 30 minutos en milisegundos

    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        emailEditText = findViewById(R.id.correo);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.ingresar);

        Button registerButton = findViewById(R.id.button3);

        ButtonHandler buttonHandler = new ButtonHandler(this);
        buttonHandler.setupRegisterButton(registerButton);

        loginButton.setOnClickListener(view -> handleLogin());

        inicializarVistas();
        configurarGoogleSignIn();
        configurarActivityResultLauncher();
        configurarBotonLogin();

    }

    private void handleLogin(){
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Aquí puedes validar las credenciales
        if (validateLogin(email, password)) {

            int userId = dbHelper.obtenerUsuarioIdPorEmail(email);

            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_id", userId);
            editor.apply();

            // Si la validación es exitosa
            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
            // Cambia la interfaz o navega a otra pantalla
            Intent intent = new Intent(MainActivity.this, ListProductsActivity.class);
            startActivity(intent);
        } else {
            // Si la validación falla
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateLogin(String email, String password){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM usuarios WHERE email = ? AND pass = ?";
        Cursor cursor = db.rawQuery(query, new String[] {email, password});

        boolean loginSuccessful = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return loginSuccessful;
    }

    private void inicializarVistas() {
        button = findViewById(R.id.btn_google);
        Log.d(TAG, "Vistas inicializadas");
    }

    private void configurarBotonLogin() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botón de inicio de sesión presionado");
                if (intentosFailedLogin >= MAX_INTENTOS) {
                    Toast.makeText(MainActivity.this, "Has alcanzado el límite de intentos. Por favor, espera antes de intentar de nuevo.", Toast.LENGTH_LONG).show();
                    return;
                }
                iniciarProcesoLogin();
            }
        });
    }

    private void iniciarProcesoLogin() {
        Log.d(TAG, "Iniciando proceso de login");
        button.setEnabled(false);

        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        Log.d(TAG, "beginSignIn exitoso");
                        IntentSenderRequest intentSenderRequest =
                                new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                        activityResultLauncher.launch(intentSenderRequest);
                    }
                })
                .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "beginSignIn falló" + e.getMessage(), e);
                        button.setEnabled(true);
                        manejarErrorLogin(e);
                    }
                });
    }

    private void manejarErrorLogin(Exception e) {
        intentosFailedLogin++;
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            if (apiException.getStatusCode() == CommonStatusCodes.CANCELED) {
                Log.d(TAG, "One-tap dialog was closed.");
                showOneTapUI = false;
            } else {
                Log.e(TAG, "Error de inicio de sesión: " + apiException.getStatusCode());
                Toast.makeText(MainActivity.this, "Error al iniciar sesión: " + getErrorMessage(apiException.getStatusCode()), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Error no específico: " + e.getMessage(), e);
            Toast.makeText(MainActivity.this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (intentosFailedLogin >= MAX_INTENTOS) {
            Log.w(TAG, "Máximo de intentos alcanzado");
            Toast.makeText(MainActivity.this, "Has alcanzado el límite de intentos fallidos. Inténtalo de nuevo más tarde.", Toast.LENGTH_LONG).show();
            button.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    button.setEnabled(true);
                    intentosFailedLogin = 0;
                    Log.d(TAG, "Reinicio de intentos de login");
                }
            }, TIEMPO_BLOQUEO);
        }
    }

    private void configurarGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.id_g_client))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(false)
                .build();
        Log.d(TAG, "Google Sign-In configurado");
    }

    private void configurarActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                            String idToken = credential.getGoogleIdToken();

                            if (idToken != null) {
                                // Obtener los datos del usuario
                                String email = credential.getId();
                                String nombre = credential.getDisplayName();
                                String photoUrl = credential.getProfilePictureUri() != null ? credential.getProfilePictureUri().toString() : null;

                                // Verificar que tenemos los datos necesarios
                                if (email == null || nombre == null) {
                                    Log.e(TAG, "Datos de usuario incompletos");
                                    Toast.makeText(this, "No se pudieron obtener los datos necesarios", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Log.d(TAG, "Datos obtenidos - Email: " + email + ", Nombre: " + nombre+ ", Foto: " + photoUrl);

                                // Primero verificar si el usuario ya existe
                                if (usuarioExiste(email)) {
                                    Log.d(TAG, "Usuario existente, procediendo al login directo");
                                    procederAListProducts();
                                    return;
                                }

                                // Si no existe, intentar registrarlo
                                if (registrarNuevoUsuario(nombre, email, photoUrl)) {
                                    Log.d(TAG, "Nuevo usuario registrado exitosamente");
                                    procederAListProducts();
                                } else {
                                    Log.e(TAG, "Error al registrar nuevo usuario");
                                    Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "ID Token es null");
                                Toast.makeText(this, "Error en la autenticación", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ApiException e) {
                            Log.e(TAG, "Error en Google Sign In: " + e.getStatusCode(), e);
                            Toast.makeText(this, "Error: " + getErrorMessage(e.getStatusCode()), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Sign-in cancelado por el usuario");
                        Intent intent = new Intent(MainActivity.this, ListProductsActivity.class);
                        startActivity(intent);

                    }
                    button.setEnabled(true);
                });
    }

    private boolean usuarioExiste(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    "usuarios",
                    new String[]{"id"},
                    "email = ?",
                    new String[]{email},
                    null,
                    null,
                    null
            );
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar usuario existente", e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    private byte[] descargarImagen(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Error al descargar la imagen: " + e.getMessage(), e);
            return null;
        }
    }

    private boolean registrarNuevoUsuario(String nombre, String email, String photoUrl) {
        try {
            // Generar una contraseña aleatoria para usuarios de Google
            String passwordTemp = generarPasswordTemporal();

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            values.put("email", email);
            values.put("pass", passwordTemp); // Guardamos la contraseña temporal

            // Descargar y guardar la foto como byte[]
            byte[] fotoBytes = null;
            if (photoUrl != null) {
                fotoBytes = descargarImagen(photoUrl);
            }
            values.put("foto", fotoBytes);

            long resultado = db.insert("usuarios", null, values);
            db.close();

            if (resultado != -1) {
                Log.d(TAG, "Usuario registrado correctamente en la base de datos");
                return true;
            } else {
                Log.e(TAG, "Error al insertar en la base de datos");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al registrar usuario", e);
            return false;
        }
    }

    private String generarPasswordTemporal() {
        // Genera una contraseña aleatoria de 16 caracteres
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int index = (int) (Math.random() * caracteres.length());
            password.append(caracteres.charAt(index));
        }
        return password.toString();
    }

    private void procederAListProducts() {
        Intent intent = new Intent(MainActivity.this, ListProductsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Cerramos MainActivity para que no puedan volver atrás
    }
    private String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case CommonStatusCodes.API_NOT_CONNECTED:
                return "API no conectada";
            case CommonStatusCodes.DEVELOPER_ERROR:
                return "Error de desarrollador. Verifica la configuración.";
            case CommonStatusCodes.ERROR:
                return "Error desconocido";
            case CommonStatusCodes.INTERNAL_ERROR:
                return "Error interno de Google Play Services";
            case CommonStatusCodes.INVALID_ACCOUNT:
                return "Cuenta inválida";
            case CommonStatusCodes.SIGN_IN_REQUIRED:
                return "Se requiere iniciar sesión";
            case CommonStatusCodes.NETWORK_ERROR:
                return "Error de red. Verifica tu conexión a internet.";
            case CommonStatusCodes.CANCELED:
                return "Operación cancelada";
            case CommonStatusCodes.TIMEOUT:
                return "Tiempo de espera agotado";
            default:
                return "Error " + statusCode;
        }
    }
}