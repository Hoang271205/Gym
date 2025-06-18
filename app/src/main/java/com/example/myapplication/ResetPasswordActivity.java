package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ResetPasswordActivity";

    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private String email;
    private boolean isVerified;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        databaseHelper = new DatabaseHelper(this);

        getIntentData();
        initViews();
        setupClickListeners();

        if (!isVerified) {
            Log.w(TAG, "User not verified, redirecting to login");
            Toast.makeText(this, "Xác thực bắt buộc", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        Log.d(TAG, "ResetPasswordActivity created for email: " + email);
    }

    // ✅ Fixed back button - Block back completely
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Vui lòng hoàn thành đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
        super.onBackPressed();

    }

    private void getIntentData() {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        isVerified = intent.getBooleanExtra("verified", false);
    }

    private void initViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(v -> handleResetPassword());
    }

    private void handleResetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        Log.d(TAG, "Attempting to reset password for: " + email);

        if (!validatePasswords(newPassword, confirmPassword)) {
            return;
        }

        if (updatePasswordInDatabase(email, newPassword)) {
            Log.d(TAG, "✅ Password updated successfully");
            showSuccessAndRedirect();
        } else {
            Log.e(TAG, "❌ Failed to update password");
            Toast.makeText(this, "❌ Lỗi cập nhật mật khẩu. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validatePasswords(String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Mật khẩu mới là bắt buộc");
            etNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etNewPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean updatePasswordInDatabase(String email, String newPassword) {
        try {
            // ✅ Dùng method có sẵn của bạn
            User user = databaseHelper.getUserByEmail(email);
            if (user == null) {
                Log.e(TAG, "User not found: " + email);
                return false;
            }

            // ✅ Dùng method updateUserPassword mới
            boolean success = databaseHelper.updateUserPassword(user.getId(), newPassword);
            Log.d(TAG, "Password update result: " + success);
            return success;

        } catch (Exception e) {
            Log.e(TAG, "Error updating password", e);
            return false;
        }
    }

    private void showSuccessAndRedirect() {
        Toast.makeText(this, "✅ Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.", Toast.LENGTH_LONG).show();

        btnResetPassword.postDelayed(() -> {
            redirectToLogin();
        }, 2000);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ResetPasswordActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("reset_success", true);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}