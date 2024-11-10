package com.example.mangodango;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mangodango.data.Usuario;

public class DatabaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "mangodango.db";
    private static final int DATABASE_VERSION = 1;

    // Constantes para nombres de tabla y columnas
    private static final String TABLE_USUARIOS = "usuarios";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOMBRE = "nombre";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASS = "pass";
    private static final String COLUMN_LATITUD = "latitud";
    private static final String COLUMN_LONGITUD = "longitud";
    private static final String COLUMN_FOTO = "foto";

    // SQL de creación de tabla
    private static final String SQL_CREATE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_PASS + " TEXT ," +
                    COLUMN_LATITUD + " REAL,"+
                    COLUMN_LONGITUD + " REAL,"+
                    COLUMN_FOTO + " BLOB "+
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USUARIOS);

        // Crear índice para búsquedas por email
        db.execSQL("CREATE INDEX idx_usuarios_email ON " + TABLE_USUARIOS + "(" + COLUMN_EMAIL + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Aquí manejarías las migraciones de manera más segura en producción
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }

    public boolean insertarUsuario(String nombre, String email) {
        if (nombre == null || email == null || nombre.trim().isEmpty() || email.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_NOMBRE, nombre.trim());
            values.put(COLUMN_EMAIL, email.trim().toLowerCase());

            long result = db.insertWithOnConflict(
                    TABLE_USUARIOS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
            );

            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public boolean existeUsuario(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_USUARIOS,
                    new String[]{COLUMN_ID},
                    COLUMN_EMAIL + " = ?",
                    new String[]{email.trim().toLowerCase()},
                    null,
                    null,
                    null
            );
            return cursor.getCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    public Usuario obtenerUsuarioPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("usuarios", new String[]{"nombre", "email","foto"}, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex("foto"));
            String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
            String email = cursor.getString(cursor.getColumnIndex("email"));
            cursor.close();
            return new Usuario(nombre, email, imageBytes); // Asegúrate de tener una clase Usuario con estos campos
        }
        return null;
    }
    public int obtenerUsuarioIdPorEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USUARIOS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + " = ?",
                new String[]{email.trim().toLowerCase()},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
            return userId;
        } else {
            cursor.close();
            return -1; // Si no se encuentra el usuario
        }
    }

    public boolean eliminarCuenta(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_USUARIOS, COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsDeleted > 0;  // Si se eliminó al menos una fila, la cuenta se eliminó correctamente
    }

    public boolean actualizarDatosUsuario(int usuarioId, String nuevoNombre, String nuevoEmail, String nuevaContraseña) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nombre", nuevoNombre);
        values.put("email", nuevoEmail);
        values.put("pass", nuevaContraseña);

        // Actualiza el usuario con el id proporcionado
        int rowsUpdated = db.update("usuarios", values, "id = ?", new String[]{String.valueOf(usuarioId)});

        return rowsUpdated > 0; // Devuelve true si se actualizó al menos un registro
    }

}
