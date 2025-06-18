package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

        databaseHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterHere = findViewById(R.id.tvRegisterHere);

        // ✅ KIỂM TRA SESSION HIỆN TẠI
        checkExistingSession();

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

    private void checkExistingSession() {
        if (DatabaseHelper.isLoggedIn(this)) {
            String userId = DatabaseHelper.getCurrentUserId(this);
            String username = DatabaseHelper.getCurrentUsername(this);
            String email = DatabaseHelper.getCurrentEmail(this);

            Log.d("Login", "✅ User already logged in: " + userId);
            navigateToMainActivity(userId, username, email);
        }
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

        // Admin account
        if (email.equals("admin@example.com") && password.equals("123456")) {
            Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();

            DatabaseHelper.saveUserSession(this, "admin", "Admin", "admin@example.com");

            Intent intent = new Intent(Login.this, AdminActivity.class);
            intent.putExtra("user_id", "admin");
            intent.putExtra("username", "Admin");
            intent.putExtra("email", "admin@example.com");
            startActivity(intent);
            finish();
            return;
        }

        // ✅ KIỂM TRA USER THỰC TẾ TRONG DATABASE
        if (databaseHelper.checkUser(email, password)) {
            User user = databaseHelper.getUserByEmail(email);

            if (user != null) {
                Toast.makeText(this, "Welcome " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();

                // ✅ SỬ DỤNG USER ID THỰC TẾ TỪ DATABASE
                String userId = String.valueOf(user.getId()); // ID từ database, không phải hardcode

                // ✅ LƯU SESSION VỚI THÔNG TIN THỰC
                DatabaseHelper.saveUserSession(this, userId, user.getUsername(), user.getEmail());

                Log.d("Login", "✅ User logged in: ID=" + userId + ", Username=" + user.getUsername() + ", Email=" + user.getEmail());

                navigateToMainActivity(userId, user.getUsername(), user.getEmail());
            } else {
                Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMainActivity(String userId, String username, String email) {
        Intent intent = new Intent(Login.this, MainActivity.class);

        // ✅ QUAN TRỌNG: Convert string userId thành int
        try {
            int userIdInt = Integer.parseInt(userId);
            intent.putExtra("user_id", userIdInt); // Truyền int, không phải string
        } catch (NumberFormatException e) {
            intent.putExtra("user_id", -1);
        }

        intent.putExtra("username", username);
        intent.putExtra("email", email);

        Log.d("Login", "📤 Navigating to MainActivity - UserID: " + userId + ", Username: " + username);

        startActivity(intent);
        finish();
    }

    public static void logout(android.content.Context context) {
        DatabaseHelper.clearUserSession(context);
        Intent intent = new Intent(context, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Log.d("Login", "✅ User logged out successfully");
    }
}