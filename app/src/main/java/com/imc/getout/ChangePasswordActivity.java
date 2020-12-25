package com.imc.getout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText oldPassword,newPassword,newPasswordAgain;
    private Button send;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPassword = findViewById(R.id.old_password_text);
        newPassword = findViewById(R.id.new_password_text);
        newPasswordAgain = findViewById(R.id.new_password_again_text);
        send = findViewById(R.id.change_password_send_button);
        progressBar = findViewById(R.id.change_password_progress_bar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                send.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                final String oldPasswordText = oldPassword.getText().toString();
                final String newPasswordText = newPassword.getText().toString();
                final String newPasswordAgainText = newPasswordAgain.getText().toString();

                String uid = firebaseAuth.getCurrentUser().getUid();
                final DocumentReference documentReference = firebaseFirestore.collection("Passwords").document(uid);

                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String password = (String) documentSnapshot.getString("password");

                        if (password.equals(oldPasswordText)) {
                            if (newPasswordText.equals(password)) {
                                progressBar.setVisibility(View.INVISIBLE);
                                send.setVisibility(View.VISIBLE);
                                Toast.makeText(ChangePasswordActivity.this, "Yeni şifreniz ile eski şifreniz aynı olamaz.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (newPasswordText.equals(newPasswordAgainText)) {
                                    String email = firebaseAuth.getCurrentUser().getEmail();
                                    AuthCredential credential = EmailAuthProvider.getCredential(email,password);

                                    firebaseUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            firebaseAuth.getCurrentUser().updatePassword(newPasswordText).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    HashMap<String,Object> update = new HashMap<>();
                                                    update.put("password",newPasswordText);

                                                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            send.setVisibility(View.VISIBLE);
                                                            Toast.makeText(ChangePasswordActivity.this, "Şifreniz başarıyla değiştirilmiştir.", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(ChangePasswordActivity.this,MainActivity.class);
                                                            finish();
                                                            startActivity(intent);
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    send.setVisibility(View.VISIBLE);
                                                    if (e.getClass().toString().equals("class com.google.firebase.auth.FirebaseAuthWeakPasswordException")) {
                                                        Toast.makeText(ChangePasswordActivity.this, "Şifreniz en az 6 karakterli olmalıdır.", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(ChangePasswordActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    send.setVisibility(View.VISIBLE);
                                    Toast.makeText(ChangePasswordActivity.this, "Girdiğiniz yeni şifreler birbirinden farklı lütfen kontrol ediniz.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            send.setVisibility(View.VISIBLE);
                            Toast.makeText(ChangePasswordActivity.this, "Lütfen önceki şifrenizi doğru giriniz.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}
