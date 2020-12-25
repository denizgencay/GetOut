package com.imc.getout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextView text;
    private EditText email;
    private Button send;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        firebaseAuth = FirebaseAuth.getInstance();

        text = findViewById(R.id.forgot_password_text);
        email = findViewById(R.id.forgot_password_email);
        send = findViewById(R.id.forgot_password_send_button);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!email.getText().toString().isEmpty()) {
                   firebaseAuth.sendPasswordResetEmail(email.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           Toast.makeText(ResetPasswordActivity.this, "Şifre sıfırlama e-postanız gönderilmiştir.", Toast.LENGTH_SHORT).show();
                           Intent intent = new Intent(ResetPasswordActivity.this,SignInActivity.class);
                           startActivity(intent);
                           finish();
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Toast.makeText(ResetPasswordActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                       }
                   });
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Lütfen e-posta adresinizi giriniz.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
