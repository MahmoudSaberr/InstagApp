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

public class SignInActivity extends AppCompatActivity {

    private EditText emailSignIn_et,passSignIn_et;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        emailSignIn_et = findViewById(R.id.sign_in_email_et);
        passSignIn_et = findViewById(R.id.sign_in_password_et);
        Button signIn_btn = findViewById(R.id.sign_in_btn);
        TextView signUp_tv = findViewById(R.id.sign_up_tv);

        signUp_tv.setOnClickListener(view ->
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));


        signIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailSignIn_et.getText().toString();
                String password = passSignIn_et.getText().toString();

                if(!email.isEmpty() && !password.isEmpty()) {
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SignInActivity.this, "Login Successfully !", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignInActivity.this,MainActivity.class));
                                finish();
                            }
                            else {
                                //noinspection ConstantConditions
                                Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(SignInActivity.this, "Please Enter Email and Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}