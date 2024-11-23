package com.example.mangodango;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ButtonHandler {
    private Context context;

    public ButtonHandler(Context context) {
        this.context = context;
    }

    //Metodo para manejar el evento de clic del boton de registro
    public void setupRegisterButton(Button registerButton) {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RegisterActivity.class);
                context.startActivity(intent);
            }
        });
    }

    //Metodo para manejar el evento de clic del boton de inicio de sesion
    public void setupLProductsButton(Button productsButton) {
        productsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ListProductsActivity.class);
                context.startActivity(intent);
            }
        });
    }

    //Metodo para manejar el evento de clic del boton de carrito
    public void setupCartButton(ImageButton cartButton) {
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CartActivity.class);
                context.startActivity(intent);
            }
        });
    }

    public void setupDatosButton(ImageButton datosButton) {
        datosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActualizarPerfil.class);
                context.startActivity(intent);
            }
        });
    }

    public void setupChangeDataButton(Button cambiarDatosButton) {
        cambiarDatosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChangeData.class);
                context.startActivity(intent);
            }
        });
    }

    public void setupActualizarDatos(ImageButton actualizarDatosButton) {
        actualizarDatosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActualizarPerfil.class);
                context.startActivity(intent);
            }
        });
    }

    public void setupVolverLista(ImageButton volverlistaButton) {
        volverlistaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ListProductsActivity.class);
                context.startActivity(intent);
            }
        });
    }
}
