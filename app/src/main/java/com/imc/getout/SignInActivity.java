package com.imc.getout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private EditText emailtText,passwordText;
    private TextView forgotPassword;
    private FirebaseAuth firebaseAuth;
    private FirebaseMessaging firebaseMessaging;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailtText = findViewById(R.id.textView7);
        passwordText = findViewById(R.id.password);
        forgotPassword = findViewById(R.id.sign_in_forgot_password);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseMessaging = FirebaseMessaging.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        Intent start = getIntent();
        Bundle bundle = start.getExtras();

        if (user != null) {
            if (user.getPhoneNumber().isEmpty()) {
                Intent intent = new Intent(SignInActivity.this,PhoneVerificationActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(SignInActivity.this,MainActivity.class);
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                startActivity(intent);
                finish();
            }
        }

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this,ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }


    public void signInButtonClicked(View view){
        String email = emailtText.getText().toString();
        String password = passwordText.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(final AuthResult authResult) {
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String token = instanceIdResult.getToken();

                        DocumentReference documentReference = firebaseFirestore.collection("Users").document(authResult.getUser().getUid());

                        HashMap<String,Object> update = new HashMap<>();
                        update.put("pushToken",token);

                        documentReference.set(update,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (authResult.getUser().getPhoneNumber() == null) {
                                    Intent intent = new Intent(SignInActivity.this,PhoneVerificationActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(SignInActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignInActivity.this,"Hatalı Giriş",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void signUpClicked(View view){
        Intent intent = new Intent(SignInActivity.this,SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
