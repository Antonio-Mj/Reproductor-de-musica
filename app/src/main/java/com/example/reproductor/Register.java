package com.example.reproductor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextUsername, editTextAddress, editTextDateOfBirth;
    TextInputLayout passwordLayout, confirmPasswordLayout, usernameLayout, addressLayout, dateOfBirthLayout;
    Button btnRegister;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    ProgressBar progressBar;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializar campos
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirmPassword);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        editTextUsername = findViewById(R.id.name);
        editTextAddress = findViewById(R.id.address);
        editTextDateOfBirth = findViewById(R.id.dateOfBirth);
        addressLayout = findViewById(R.id.addresslLayout);
        dateOfBirthLayout = findViewById(R.id.dateOfBirthLayout);
        usernameLayout = findViewById(R.id.usernameLayout);

        // Configurar DatePickerDialog
        editTextDateOfBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    Register.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Validar que la fecha seleccionada esté dentro del rango permitido
                        if (selectedYear >= 1950 && selectedYear <= year) {
                            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                            editTextDateOfBirth.setText(selectedDate);
                        } else {
                            Toast.makeText(Register.this, "Please select a year between 1950 and " + year, Toast.LENGTH_SHORT).show();
                        }
                    }, year, month, day);

            datePickerDialog.show();
        });

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = String.valueOf(editTextEmail.getText());
            String username = String.valueOf(editTextUsername.getText());
            String password = String.valueOf(editTextPassword.getText());
            String confirmPassword = String.valueOf(editTextConfirmPassword.getText());
            String address = String.valueOf(editTextAddress.getText());
            String dateOfBirth = String.valueOf(editTextDateOfBirth.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(Register.this, "Enter username", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(address)) {
                Toast.makeText(Register.this, "Enter address", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(dateOfBirth)) {
                Toast.makeText(Register.this, "Enter date of birth", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (!password.equals(confirmPassword)) {
                passwordLayout.setError("Passwords do not match");
                confirmPasswordLayout.setError("Passwords do not match");
                progressBar.setVisibility(View.GONE);
                return;
            } else {
                passwordLayout.setError(null);
                confirmPasswordLayout.setError(null);
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Guarda los datos en Firebase Realtime Database
                                String userId = user.getUid();
                                User userProfile = new User(username, email, password, address, dateOfBirth);
                                mDatabase.child("users").child(userId).setValue(userProfile)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(task11 -> {
                                                            if (task11.isSuccessful()) {
                                                                Toast.makeText(Register.this, "Verification email sent. Please check your email.", Toast.LENGTH_LONG).show();
                                                                mAuth.signOut();
                                                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                Toast.makeText(Register.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(Register.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }


    // Clase interna para almacenar la información del usuario
    public static class User {
        public String username;
        public String email;
        public String password;
        public String address;
        public String dateOfBirth;

        public User(String username, String email, String password, String address, String dateOfBirth) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.address = address;
            this.dateOfBirth = dateOfBirth;
        }
    }
}