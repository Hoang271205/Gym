package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class EmailConfig {
    private static final String TAG = "EmailConfig";
    private static final String PREFS_NAME = "email_config";

    private static final String KEY_SENDER_EMAIL = "sender_email";
    private static final String KEY_SENDER_PASSWORD = "sender_password";
    private static final String KEY_SMTP_HOST = "smtp_host";
    private static final String KEY_SMTP_PORT = "smtp_port";
    private static final String KEY_IS_CONFIGURED = "is_configured";

    private static final String DEFAULT_SMTP_HOST = "smtp.gmail.com";
    private static final String DEFAULT_SMTP_PORT = "587";

    public static class Config {
        public String senderEmail;
        public String senderPassword;
        public String smtpHost;
        public String smtpPort;
        public boolean isConfigured;

        public Config() {
            this.smtpHost = DEFAULT_SMTP_HOST;
            this.smtpPort = DEFAULT_SMTP_PORT;
            this.isConfigured = false;
        }

        public boolean isValid() {
            return senderEmail != null && !senderEmail.isEmpty() &&
                    senderPassword != null && !senderPassword.isEmpty() &&
                    smtpHost != null && !smtpHost.isEmpty() &&
                    smtpPort != null && !smtpPort.isEmpty();
        }
    }

    public static void saveEmailConfig(Context context, String email, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_SENDER_EMAIL, email);
        editor.putString(KEY_SENDER_PASSWORD, password);
        editor.putString(KEY_SMTP_HOST, DEFAULT_SMTP_HOST);
        editor.putString(KEY_SMTP_PORT, DEFAULT_SMTP_PORT);
        editor.putBoolean(KEY_IS_CONFIGURED, true);

        editor.apply();
        Log.d(TAG, "✅ Email config saved for: " + email);
    }

    public static Config getEmailConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Config config = new Config();
        config.senderEmail = prefs.getString(KEY_SENDER_EMAIL, "");
        config.senderPassword = prefs.getString(KEY_SENDER_PASSWORD, "");
        config.smtpHost = prefs.getString(KEY_SMTP_HOST, DEFAULT_SMTP_HOST);
        config.smtpPort = prefs.getString(KEY_SMTP_PORT, DEFAULT_SMTP_PORT);
        config.isConfigured = prefs.getBoolean(KEY_IS_CONFIGURED, false);

        return config;
    }

    public static boolean isEmailConfigured(Context context) {
        Config config = getEmailConfig(context);
        return config.isConfigured && config.isValid();
    }

    public static void clearEmailConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Log.d(TAG, "✅ Email config cleared");
    }
}