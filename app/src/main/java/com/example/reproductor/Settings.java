package com.example.reproductor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Settings extends AppCompatActivity {
    Button btn_back;
    TextView tvChangeInformation;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();


        btn_back = findViewById(R.id.btn_back);
        tvChangeInformation = findViewById(R.id.tvChangeInformation);

        tvChangeInformation.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, ViewInformation.class);
            startActivity(intent);
        });

        btn_back.setOnClickListener(v -> {
            // Cerrar sesión
            mAuth.signOut();
//            Toast.makeText(Settings.this, "Logoout", Toast.LENGTH_SHORT).show();

            // Redirigir a la pantalla de inicio de sesión
            Intent intent = new Intent(Settings.this, Login.class);
            startActivity(intent);
            finish();
        });

        // Manejar el botón de retroceso
        this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHome();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(Settings.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
