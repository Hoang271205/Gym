package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";

    private EditText etEmail;
    private Button btnSendCode;
    private ProgressBar progressBar;
    private TextView tvStatusMessage, tvBackToLogin;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();

        Log.d(TAG, "ForgotPasswordActivity created");
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        progressBar = findViewById(R.id.progressBar);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnSendCode.setOnClickListener(v -> handleSendCode());

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, Login.class);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void handleSendCode() {
        String email = etEmail.getText().toString().trim();
        Log.d(TAG, "Attempting to send code to: " + email);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email là bắt buộc");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Vui lòng nhập email hợp lệ");
            etEmail.requestFocus();
            return;
        }

        if (!databaseHelper.checkEmail(email)) {
            showStatusMessage("❌ Không tìm thấy email trong hệ thống. Vui lòng kiểm tra lại hoặc đăng ký tài khoản mới.", false);
            return;
        }

        if (!EmailConfig.isEmailConfigured(this)) {
            showGmailConfigDialog();
            return;
        }

        Log.d(TAG, "Email found, sending verification code...");
        sendVerificationCode(email);
    }

    private void sendVerificationCode(String email) {
        showLoading(true);
        showStatusMessage("📧 Đang gửi mã xác thực qua Gmail...", true);

        EmailService.sendVerificationCode(this, email, new EmailService.EmailCallback() {
            @Override
            public void onSuccess(String verificationCode) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showStatusMessage("✅ Đã gửi mã xác thực qua Gmail thành công! Vui lòng kiểm tra hộp thư.", true);
                    Log.d(TAG, "✅ Code sent successfully via Gmail: " + verificationCode);

                    Toast.makeText(ForgotPasswordActivity.this,
                            "📧 Mã xác thực đã được gửi qua Gmail!",
                            Toast.LENGTH_LONG).show();

                    etEmail.postDelayed(() -> {
                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("verification_code", verificationCode);
                        intent.putExtra("timestamp", System.currentTimeMillis());
                        startActivity(intent);
                        finish();
                    }, 2000);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showStatusMessage("❌ Lỗi gửi Gmail: " + error, false);
                    Log.e(TAG, "❌ Failed to send Gmail: " + error);

                    new AlertDialog.Builder(ForgotPasswordActivity.this)
                            .setTitle("❌ Lỗi Gửi Email")
                            .setMessage("Không thể gửi email. Bạn có muốn cấu hình lại Gmail không?")
                            .setPositiveButton("Cấu Hình Lại", (dialog, which) -> showGmailConfigDialog())
                            .setNegativeButton("Hủy", null)
                            .show();
                });
            }
        });
    }

    private void showGmailConfigDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gmail_config, null);

        EditText etGmailEmail = dialogView.findViewById(R.id.etGmailEmail);
        EditText etGmailPassword = dialogView.findViewById(R.id.etGmailPassword);
        Button btnTestGmail = dialogView.findViewById(R.id.btnTestGmail);
        TextView tvGmailStatus = dialogView.findViewById(R.id.tvGmailStatus);

        EmailConfig.Config existingConfig = EmailConfig.getEmailConfig(this);
        etGmailEmail.setText(existingConfig.senderEmail);
        etGmailPassword.setText(existingConfig.senderPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("📧 Cấu Hình Gmail")
                .setMessage("Nhập Gmail và App Password để gửi mã xác thực:")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            btnTestGmail.setOnClickListener(v -> {
                String gmailEmail = etGmailEmail.getText().toString().trim();
                String gmailPassword = etGmailPassword.getText().toString().trim();

                if (TextUtils.isEmpty(gmailEmail) || TextUtils.isEmpty(gmailPassword)) {
                    tvGmailStatus.setText("❌ Vui lòng nhập đầy đủ thông tin");
                    tvGmailStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    tvGmailStatus.setVisibility(View.VISIBLE);
                    return;
                }

                tvGmailStatus.setText("🔄 Đang test Gmail...");
                tvGmailStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                tvGmailStatus.setVisibility(View.VISIBLE);
                btnTestGmail.setEnabled(false);

                EmailService.testEmailConfig(this, gmailEmail, gmailPassword, new EmailService.EmailCallback() {
                    @Override
                    public void onSuccess(String verificationCode) {
                        runOnUiThread(() -> {
                            tvGmailStatus.setText("✅ Gmail hoạt động tốt!");
                            tvGmailStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                            btnTestGmail.setEnabled(true);
                            saveButton.setEnabled(true);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            tvGmailStatus.setText("❌ Lỗi: " + error);
                            tvGmailStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            btnTestGmail.setEnabled(true);
                        });
                    }
                });
            });

            saveButton.setOnClickListener(v -> {
                String gmailEmail = etGmailEmail.getText().toString().trim();
                String gmailPassword = etGmailPassword.getText().toString().trim();

                if (TextUtils.isEmpty(gmailEmail) || TextUtils.isEmpty(gmailPassword)) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(gmailEmail).matches()) {
                    Toast.makeText(this, "Vui lòng nhập Gmail hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                EmailConfig.saveEmailConfig(this, gmailEmail, gmailPassword);
                Toast.makeText(this, "✅ Đã lưu cấu hình Gmail!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                String userEmail = etEmail.getText().toString().trim();
                if (!TextUtils.isEmpty(userEmail)) {
                    sendVerificationCode(userEmail);
                }
            });
        });

        dialog.show();
    }

    private void showLoading(boolean show) {
        if (show) {
            btnSendCode.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            btnSendCode.setEnabled(false);
        } else {
            btnSendCode.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            btnSendCode.setEnabled(true);
        }
    }

    private void showStatusMessage(String message, boolean isSuccess) {
        tvStatusMessage.setText(message);
        tvStatusMessage.setTextColor(isSuccess ?
                getResources().getColor(android.R.color.holo_green_light) :
                getResources().getColor(android.R.color.holo_red_light));
        tvStatusMessage.setVisibility(View.VISIBLE);
    }
}