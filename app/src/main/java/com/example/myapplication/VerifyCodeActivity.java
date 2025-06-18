package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyCodeActivity extends AppCompatActivity {
    private static final String TAG = "VerifyCodeActivity";
    private static final long CODE_EXPIRY_TIME = 10 * 60 * 1000; // 10 phút

    private EditText[] codeInputs = new EditText[6];
    private Button btnVerify;
    private TextView tvEmailDisplay, tvTimer, tvResendCode, tvResendText;
    private ImageButton btnBack;

    private String email;
    private String correctCode;
    private long codeTimestamp;
    private CountDownTimer timer;
    private boolean canResend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        getIntentData();
        initViews();
        setupCodeInputs();
        setupClickListeners();
        startTimer();

        Log.d(TAG, "VerifyCodeActivity created for email: " + email);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        correctCode = intent.getStringExtra("verification_code");
        codeTimestamp = intent.getLongExtra("timestamp", System.currentTimeMillis());

        Log.d(TAG, "Received data - Email: " + email + ", Code: " + correctCode);
    }

    private void initViews() {
        codeInputs[0] = findViewById(R.id.etCode1);
        codeInputs[1] = findViewById(R.id.etCode2);
        codeInputs[2] = findViewById(R.id.etCode3);
        codeInputs[3] = findViewById(R.id.etCode4);
        codeInputs[4] = findViewById(R.id.etCode5);
        codeInputs[5] = findViewById(R.id.etCode6);

        btnVerify = findViewById(R.id.btnVerify);
        tvEmailDisplay = findViewById(R.id.tvEmailDisplay);
        tvTimer = findViewById(R.id.tvTimer);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvResendText = findViewById(R.id.tvResendText);
        btnBack = findViewById(R.id.btnBack);

        if (email != null) {
            tvEmailDisplay.setText(maskEmail(email));
        }
    }

    private void setupCodeInputs() {
        for (int i = 0; i < codeInputs.length; i++) {
            final int index = i;

            codeInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < codeInputs.length - 1) {
                        codeInputs[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        codeInputs[index - 1].requestFocus();
                    }
                    checkCodeComplete();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            codeInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                        event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                    if (codeInputs[index].getText().length() == 0 && index > 0) {
                        codeInputs[index - 1].requestFocus();
                        codeInputs[index - 1].setText("");
                    }
                }
                return false;
            });
        }

        codeInputs[0].requestFocus();
    }

    private void setupClickListeners() {
        btnVerify.setOnClickListener(v -> verifyCode());

        tvResendCode.setOnClickListener(v -> {
            if (canResend) {
                resendCode();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void startTimer() {
        long timeRemaining = CODE_EXPIRY_TIME - (System.currentTimeMillis() - codeTimestamp);

        if (timeRemaining <= 0) {
            showExpiredState();
            return;
        }

        timer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(seconds + "s");
            }

            @Override
            public void onFinish() {
                showExpiredState();
            }
        };

        timer.start();
    }

    private void showExpiredState() {
        tvTimer.setVisibility(View.GONE);
        tvResendText.setVisibility(View.GONE);
        tvResendCode.setVisibility(View.VISIBLE);
        canResend = true;

        Toast.makeText(this, "⏰ Mã đã hết hạn. Vui lòng yêu cầu mã mới.", Toast.LENGTH_LONG).show();
    }

    private void checkCodeComplete() {
        StringBuilder code = new StringBuilder();
        for (EditText input : codeInputs) {
            code.append(input.getText().toString());
        }

        if (code.length() == 6) {
            btnVerify.setEnabled(true);
            btnVerify.setAlpha(1.0f);
        } else {
            btnVerify.setEnabled(false);
            btnVerify.setAlpha(0.5f);
        }
    }

    private void verifyCode() {
        StringBuilder enteredCode = new StringBuilder();
        for (EditText input : codeInputs) {
            enteredCode.append(input.getText().toString());
        }

        String code = enteredCode.toString();
        Log.d(TAG, "Verifying code: " + code + " against: " + correctCode);

        long timeElapsed = System.currentTimeMillis() - codeTimestamp;
        if (timeElapsed > CODE_EXPIRY_TIME) {
            Toast.makeText(this, "⏰ Mã đã hết hạn. Vui lòng yêu cầu mã mới.", Toast.LENGTH_LONG).show();
            showExpiredState();
            return;
        }

        if (code.equals(correctCode)) {
            Log.d(TAG, "✅ Code verified successfully");
            Toast.makeText(this, "✅ Xác thực thành công!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(VerifyCodeActivity.this, ResetPasswordActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("verified", true);
            startActivity(intent);
            finish();
        } else {
            Log.w(TAG, "❌ Invalid code entered");
            Toast.makeText(this, "❌ Mã không đúng. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            clearCodeInputs();
            shakeCodeInputs();
        }
    }

    private void resendCode() {
        Log.d(TAG, "Resending code to: " + email);
        canResend = false;
        tvResendCode.setVisibility(View.GONE);
        tvResendText.setVisibility(View.VISIBLE);
        tvTimer.setVisibility(View.VISIBLE);

        EmailService.sendVerificationCode(this, email, new EmailService.EmailCallback() {
            @Override
            public void onSuccess(String verificationCode) {
                runOnUiThread(() -> {
                    correctCode = verificationCode;
                    codeTimestamp = System.currentTimeMillis();
                    clearCodeInputs();
                    startTimer();

                    Toast.makeText(VerifyCodeActivity.this,
                            "📧 Đã gửi mã mới qua Gmail!",
                            Toast.LENGTH_LONG).show();
                    Log.d(TAG, "✅ New code sent: " + verificationCode);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(VerifyCodeActivity.this, "❌ Lỗi gửi lại mã: " + error, Toast.LENGTH_LONG).show();
                    showExpiredState();
                });
            }
        });
    }

    private void clearCodeInputs() {
        for (EditText input : codeInputs) {
            input.setText("");
        }
        codeInputs[0].requestFocus();
    }

    private void shakeCodeInputs() {
        for (EditText input : codeInputs) {
            input.animate()
                    .translationX(-10f)
                    .setDuration(50)
                    .withEndAction(() -> input.animate()
                            .translationX(10f)
                            .setDuration(50)
                            .withEndAction(() -> input.animate()
                                    .translationX(0f)
                                    .setDuration(50)));
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return email;
        }

        String maskedUsername = username.charAt(0) + "***" + username.charAt(username.length() - 1);
        return maskedUsername + "@" + domain;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}