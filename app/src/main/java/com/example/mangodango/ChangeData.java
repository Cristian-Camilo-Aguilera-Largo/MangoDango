package com.example.mangodango;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangeData extends AppCompatActivity {
    private EditText NuevoNombre, NuevoEmail, NuevaContraseña;
    private Button changeDataButton;
    private DatabaseHelper databaseHelper;
    private Integer usuarioId;
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
        // Inicialización de las vistas
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

            if (!nuevoNombre.isEmpty() && !nuevoEmail.isEmpty() && !nuevaContraseña.isEmpty()) {
                boolean datosActualizados = databaseHelper.actualizarDatosUsuario(usuarioId, nuevoNombre, nuevoEmail, nuevaContraseña);

                if (datosActualizados) {
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    // Puedes redirigir a otra pantalla si lo deseas
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
}