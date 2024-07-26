package com.example.reproductor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView imageView = findViewById(R.id.imageViewpp);
        ImageView profileImage = findViewById(R.id.profile_image);

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
        });

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Settings.class);
            startActivity(intent);
        });
    }
}

