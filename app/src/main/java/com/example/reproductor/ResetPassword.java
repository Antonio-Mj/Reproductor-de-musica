package com.example.reproductor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    private EditText mEditTextEmail;
    private Button mButtonResetPassword;
    private String email = "";
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(this);
        mEditTextEmail = findViewById(R.id.editTextEmail);
        mButtonResetPassword = findViewById(R.id.btn_ResetPassword);
        textView = findViewById(R.id.loginNow);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        TextView resetPasswordMessage = findViewById(R.id.resetPasswordMessage);
        ImageView logo = findViewById(R.id.imageView4);
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(rotateAnimation);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        resetPasswordMessage.startAnimation(fadeIn);

        mButtonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEditTextEmail.getText().toString().trim();

                if (!email.isEmpty()) {
                    mDialog.setMessage("Por favor espere...");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    resetPassword();
                } else {
                    Toast.makeText(ResetPassword.this, "Debe ingresar el email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void resetPassword() {
        mAuth.setLanguageCode("es");
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(ResetPassword.this, "Se ha enviado un email para restablecer la contraseña", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ResetPassword.this, "Error al enviar el email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
