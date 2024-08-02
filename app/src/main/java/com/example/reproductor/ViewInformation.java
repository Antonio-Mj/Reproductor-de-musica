package com.example.reproductor;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ViewInformation extends AppCompatActivity {

    EditText etUsername, etEmail, etAddress, etBirthday;
    Button btnSave, btnBack, btnDelete;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_information);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etBirthday = findViewById(R.id.etBirthday);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("users").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User userProfile = dataSnapshot.getValue(User.class);
                    if (userProfile != null) {
                        etUsername.setText(userProfile.username);
                        etEmail.setText(userProfile.email);
                        etAddress.setText(userProfile.address);
                        etBirthday.setText(userProfile.dateOfBirth);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //Toast.makeText(ViewInformation.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnSave.setOnClickListener(v -> saveUserInformation());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ViewInformation.this, Home.class);
            startActivity(intent);
            finish();
        });

        btnDelete.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void saveUserInformation() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dateOfBirth = etBirthday.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || address.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            User updatedUser = new User(username, email, address, dateOfBirth);
            mDatabase.child("users").child(userId).setValue(updatedUser)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ViewInformation.this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ViewInformation.this, "Failed to update information", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account?");

        builder.setPositiveButton("Delete", (dialog, which) -> deleteUserAccount());

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("users").child(userId).removeValue()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            user.delete().addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    Toast.makeText(ViewInformation.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ViewInformation.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(ViewInformation.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(ViewInformation.this, "Failed to delete user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Clase interna para almacenar la información del usuario
    public static class User {
        public String username;
        public String email;
        public String address;
        public String dateOfBirth;

        public User() {
            // Constructor vacío necesario para Firebase
        }

        public User(String username, String email, String address, String dateOfBirth) {
            this.username = username;
            this.email = email;
            this.address = address;
            this.dateOfBirth = dateOfBirth;
        }
    }
}