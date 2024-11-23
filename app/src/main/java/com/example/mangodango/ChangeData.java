package com.example.mangodango;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ChangeData extends AppCompatActivity {
    private EditText NuevoNombre, NuevoEmail, NuevaContraseña;
    private Button changeDataButton;
    private DatabaseHelper databaseHelper;
    private Integer usuarioId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private Bitmap selectedImageBitmap;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int CAMERA_PERMISSION_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Configuracion abrir camara
        Button abrirCamara = findViewById(R.id.abrirCamara);
        abrirCamara.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });

        ImageButton backButton = findViewById(R.id.volver);
        ButtonHandler buttonHandler = new ButtonHandler(this);

        buttonHandler.setupActualizarDatos(backButton);

        // Inicialización de las vistas
        profileImageView = findViewById(R.id.profileImageView);
        Button selectImageButton = findViewById(R.id.selectImageButton);

        selectImageButton.setOnClickListener(v -> openImagePicker());
        NuevoNombre = findViewById(R.id.NuevoNombre);
        NuevoEmail = findViewById(R.id.NuevoEmail);
        NuevaContraseña = findViewById(R.id.NuevaContraseña);
        changeDataButton = findViewById(R.id.changeData);

        databaseHelper = new DatabaseHelper(this);
        usuarioId = obtenerUsuarioAutenticadoId(); // Asume que tienes un método para obtener el ID del usuario autenticado

        // Configuración del botón para cambiar los datos
        changeDataButton.setOnClickListener(v -> {
            String nuevoNombre = NuevoNombre.getText().toString();
            String nuevoEmail = NuevoEmail.getText().toString();
            String nuevaContraseña = NuevaContraseña.getText().toString();
            byte[] imageBytes = null;
            if (selectedImageBitmap != null) {
                imageBytes = convertBitmapToByteArray(selectedImageBitmap);
            }
            if (!nuevoNombre.isEmpty() && !nuevoEmail.isEmpty() && !nuevaContraseña.isEmpty()) {
                boolean datosActualizados = databaseHelper.actualizarDatosUsuario(usuarioId, nuevoNombre, nuevoEmail, nuevaContraseña, imageBytes);

                if (datosActualizados) {
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    // Puedes redirigir a otra pantalla si lo deseas
                    Intent intent = new Intent(ChangeData.this, ActualizarPerfil.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Método para obtener el ID del usuario desde SharedPreferences
    private Integer obtenerUsuarioAutenticadoId() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return preferences.getInt("user_id", -1);
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap photoBitmap = (Bitmap) extras.get("data");
            if (photoBitmap != null) {
                selectedImageBitmap = photoBitmap;
                profileImageView.setImageBitmap(selectedImageBitmap); // Mostrar la foto en el ImageView

            }
        }else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                // Redimensionar la imagen
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(selectedImageBitmap, 500, 500, true);

                selectedImageBitmap = resizedBitmap;
                profileImageView.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        return stream.toByteArray();
    }


}