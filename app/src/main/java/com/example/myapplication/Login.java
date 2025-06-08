package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegisterHere;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Khởi tạo database helper
        databaseHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterHere = findViewById(R.id.tvRegisterHere);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        tvRegisterHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Kiểm tra admin account
        if (email.equals("admin@example.com") && password.equals("123456")) {
            Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Login.this, AdminActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Kiểm tra user trong database
        if (databaseHelper.checkUser(email, password)) {
            User user = databaseHelper.getUserByEmail(email);
            Toast.makeText(this, "Welcome " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();

            // Truyền thông tin user sang MainActivity
            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.putExtra("user_id", user.getId());
            intent.putExtra("username", user.getUsername());
            intent.putExtra("email", user.getEmail());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}