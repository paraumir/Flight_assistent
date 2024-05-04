package com.example.kurs;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.kurs.databinding.ActivityAuthenticationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class authentification extends AppCompatActivity {

    private ActivityAuthenticationBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = binding.signupEmail.getText().toString().trim();
                String pass = binding.signupPassword.getText().toString().trim();

                if (user.isEmpty()){
                    binding.signupEmail.setError("Поле не может быть пустым");
                }
                if (pass.isEmpty()){
                    binding.signupPassword.setError("Поле не может быть пустым");
                } else{
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(authentification.this, "Вы зарегистрированы", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(authentification.this, login.class));
                            } else {
                                Toast.makeText(authentification.this, "Ошибка регистрации" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        binding.loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(authentification.this, login.class));
            }
        });
    }
}
