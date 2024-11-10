package com.example.mangodango.data;

public class Usuario {
    private int id;
    private String nombre;
    private String correo;
    private byte[] foto;  // Este es el campo que almacena la imagen como BLOB

    public Usuario(String nombre, String correo,byte[] foto) {
        this.nombre = nombre;
        this.correo = correo;
        this.foto = foto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }
}

