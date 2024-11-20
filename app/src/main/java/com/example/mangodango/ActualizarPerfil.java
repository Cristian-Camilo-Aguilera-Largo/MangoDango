package com.example.mangodango;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mangodango.data.Usuario;

public class ActualizarPerfil extends AppCompatActivity {
    private TextView NombrePersona;
    private TextView CorreoPersona;
    private DatabaseHelper databaseHelper;
    private Integer usuarioId;
    private Button eliminarCuentaButton;
    private ImageView imagenPersona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_actualizar_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EdgeToEdge.enable(this);
        Button changeButton = findViewById(R.id.cambiarDatos);

        ButtonHandler buttonHandler = new ButtonHandler(this);
        buttonHandler.setupChangeDataButton(changeButton);

        imagenPersona = findViewById(R.id.ImagenPersona);
        NombrePersona = findViewById(R.id.NombrePersona);
        CorreoPersona = findViewById(R.id.CorreoPersona);
        databaseHelper = new DatabaseHelper(this);
        usuarioId = obtenerUsuarioAutenticadoId();

        try {
            cargarDatosUsuario();
        } catch (Exception e) {
            Log.e("ActualizarPerfil", "Error al cargar datos del usuario", e);
            Toast.makeText(this, "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show();
        }
        Button eliminarCuentaButton = findViewById(R.id.eliminarCuenta);
        eliminarCuentaButton.setOnClickListener(v -> {
            // Obtener el ID del usuario desde SharedPreferences
            Integer usuarioId = obtenerUsuarioAutenticadoId();

            // Eliminar la cuenta desde la base de datos
            boolean cuentaEliminada = databaseHelper.eliminarCuenta(usuarioId);

            if (cuentaEliminada) {
                // Cerrar sesión (eliminar el user_id de SharedPreferences)
                cerrarSesion();

                // Mostrar mensaje de éxito
                Toast.makeText(this, "Cuenta eliminada y sesión cerrada", Toast.LENGTH_SHORT).show();

                // Redirigir a la pantalla de inicio de sesión o cualquier otra actividad
                Intent intent = new Intent(ActualizarPerfil.this, MainActivity.class);
                startActivity(intent);
                finish();  // Finaliza la actividad actual para que no se pueda regresar
            } else {
                // Si hubo algún error al eliminar la cuenta
                Toast.makeText(this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void cargarDatosUsuario() {
        // Obtén el usuario desde la base de datos

        Usuario usuario = databaseHelper.obtenerUsuarioPorId(usuarioId);

        if (usuario != null) {
            // Asigna los datos a los TextView
            NombrePersona.setText(usuario.getNombre());
            CorreoPersona.setText(usuario.getCorreo());
            // Cargar la imagen de perfil desde la base de datos
            try{
                byte[] imageBytes = usuario.getFoto();  // Supongo que tienes un método getFoto() que devuelve un byte[]
                if (imageBytes != null) {
                    Log.d("ActualizarPerfil", "Imagen recuperada con éxito");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    // Asegúrate de que tienes este ImageView en tu layout
                    imagenPersona.setImageBitmap(bitmap);  // Establece la imagen en el ImageView
                } else {
                    // Si no hay imagen en la base de datos, puedes poner una imagen predeterminada
                    Log.d("ActualizarPerfil", "No hay imagen en la base de datos.");

                    imagenPersona.setImageResource(R.drawable.person_circle);  // Usa una imagen predeterminada
                }
            } catch (Exception e){
                Log.e("ActualizarPerfil", "Error al cargar la imagen del usuario", e);
                imagenPersona.setImageResource(R.drawable.person_circle);
            }

        } else {
            Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private Integer obtenerUsuarioAutenticadoId() {
        SharedPreferences preferences = getSharedPreferences("user_prefs",MODE_PRIVATE);
        return preferences.getInt("user_id",-1); // Reemplaza con la lógica adecuada
    }
    private void cerrarSesion() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("user_id");  // Elimina el ID del usuario de las preferencias
        editor.apply();
    }
    public void mostrarImagen(byte[] imageBytes) {
        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imagenPersona.setImageBitmap(bitmap); // Asegúrate de que `imagenPersona` es un ImageView
        }
    }

}