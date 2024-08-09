package com.example.reproductor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class ProfilePicture extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewProfilePicture;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);

        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        Button buttonSaveProfilePicture = findViewById(R.id.buttonSaveProfilePicture);

        // Inicializar Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        buttonSelectImage.setOnClickListener(v -> openImageChooser());

        buttonSaveProfilePicture.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImage();
            } else {
                Toast.makeText(ProfilePicture.this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageViewProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        StorageReference ref = storageReference.child("profile_pictures/" + UUID.randomUUID().toString());

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Toast.makeText(ProfilePicture.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                    // Save profile picture upload status in SharedPreferences
                    getSharedPreferences("userPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("profilePictureUploaded", true)
                            .apply();

                    // Redirect to the Home activity
                    Intent intent = new Intent(ProfilePicture.this, Home.class);
                    startActivity(intent);
                    finish();
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(ProfilePicture.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


}
