package com.example.instagblog.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailSignUp_et,passSignUp_et;
    private Button signUp_btn;
    private TextView signIn_tv;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        emailSignUp_et = findViewById(R.id.sign_up_email_et);
        passSignUp_et = findViewById(R.id.sign_up_password_et);
        signUp_btn =findViewById(R.id.sign_up_btn);
        signIn_tv =findViewById(R.id.sign_in_tv);

        signIn_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });

        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailSignUp_et.getText().toString();
                String password = passSignUp_et.getText().toString();

                if(!email.isEmpty() && !password.isEmpty()) {
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SignUpActivity.this, "Registered Successfully !", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this,SetUpActivity.class));
                                finish();
                            }
                            else {
                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(SignUpActivity.this, "Please Enter Email and Password", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}