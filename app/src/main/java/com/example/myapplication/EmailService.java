package com.example.myapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    private static final String TAG = "EmailService";

    public interface EmailCallback {
        void onSuccess(String verificationCode);
        void onFailure(String error);
    }

    // ✅ DYNAMIC: Gửi verification code với Gmail thật
    public static void sendVerificationCode(Context context, String recipientEmail, EmailCallback callback) {
        EmailConfig.Config config = EmailConfig.getEmailConfig(context);

        if (!config.isValid()) {
            callback.onFailure("Email chưa được cấu hình. Vui lòng cấu hình Gmail trước.");
            return;
        }

        String verificationCode = generateVerificationCode();
        new SendEmailTask(config, recipientEmail, verificationCode, callback, false).execute();
    }

    // ✅ Test email configuration
    public static void testEmailConfig(Context context, String email, String password, EmailCallback callback) {
        EmailConfig.Config config = new EmailConfig.Config();
        config.senderEmail = email;
        config.senderPassword = password;
        config.smtpHost = "smtp.gmail.com";
        config.smtpPort = "587";
        config.isConfigured = true;

        String testCode = "123456";
        new SendEmailTask(config, email, testCode, callback, true).execute();
    }

    private static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private static class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private EmailConfig.Config config;
        private String recipientEmail;
        private String verificationCode;
        private EmailCallback callback;
        private boolean isTest;
        private String errorMessage;

        public SendEmailTask(EmailConfig.Config config, String recipientEmail,
                             String verificationCode, EmailCallback callback, boolean isTest) {
            this.config = config;
            this.recipientEmail = recipientEmail;
            this.verificationCode = verificationCode;
            this.callback = callback;
            this.isTest = isTest;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Log.d(TAG, "🚀 Starting real email send process...");

                // ✅ Gmail SMTP Configuration - Updated
                Properties props = new Properties();
                props.put("mail.smtp.host", config.smtpHost);
                props.put("mail.smtp.port", config.smtpPort);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");

                // ✅ Additional security settings
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
                props.put("mail.smtp.connectiontimeout", "30000"); // 30 seconds
                props.put("mail.smtp.timeout", "30000");
                props.put("mail.smtp.writetimeout", "30000");

                // ✅ Debug mode
                props.put("mail.debug", "true");

                Log.d(TAG, "📧 SMTP Config: " + config.smtpHost + ":" + config.smtpPort);
                Log.d(TAG, "👤 Sender: " + config.senderEmail);
                Log.d(TAG, "📨 Recipient: " + recipientEmail);

                // ✅ Create authenticated session
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        Log.d(TAG, "🔐 Authenticating with Gmail...");
                        return new PasswordAuthentication(config.senderEmail, config.senderPassword);
                    }
                });

                session.setDebug(true);

                // ✅ Create message
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(config.senderEmail, "Gym App"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));

                if (isTest) {
                    message.setSubject("🏋️ Gym App - Test Email Configuration");
                    message.setContent(createTestEmailContent(), "text/html; charset=utf-8");
                    Log.d(TAG, "📝 Test email created");
                } else {
                    message.setSubject("🏋️ Gym App - Mã Xác Thực Đặt Lại Mật Khẩu");
                    message.setContent(createEmailContent(verificationCode), "text/html; charset=utf-8");
                    Log.d(TAG, "📝 Verification email created with code: " + verificationCode);
                }

                // ✅ Send email
                Log.d(TAG, "📤 Sending email via Gmail SMTP...");
                Transport.send(message);
                Log.d(TAG, "✅ Email sent successfully via Gmail!");

                return true;

            } catch (MessagingException e) {
                Log.e(TAG, "❌ MessagingException: " + e.getMessage(), e);

                // ✅ Detailed error analysis
                String errorMsg = e.getMessage();
                if (errorMsg != null) {
                    if (errorMsg.contains("authentication failed") || errorMsg.contains("535")) {
                        errorMessage = "❌ Lỗi xác thực Gmail.\n• Kiểm tra email đúng chưa\n• Dùng App Password thay vì mật khẩu thường\n• Bật 2-Step Verification";
                    } else if (errorMsg.contains("connection") || errorMsg.contains("timeout")) {
                        errorMessage = "❌ Lỗi kết nối.\n• Kiểm tra internet\n• Thử lại sau";
                    } else if (errorMsg.contains("554") || errorMsg.contains("recipient")) {
                        errorMessage = "❌ Email nhận không hợp lệ.\n• Kiểm tra email đích";
                    } else {
                        errorMessage = "❌ Lỗi Gmail: " + errorMsg;
                    }
                } else {
                    errorMessage = "❌ Lỗi gửi email không xác định";
                }
                return false;

            } catch (Exception e) {
                Log.e(TAG, "❌ General Exception: " + e.getMessage(), e);
                errorMessage = "❌ Lỗi hệ thống: " + e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) {
                if (success) {
                    Log.d(TAG, "🎉 Email callback - SUCCESS");
                    callback.onSuccess(verificationCode);
                } else {
                    Log.e(TAG, "💥 Email callback - FAILURE: " + errorMessage);
                    callback.onFailure(errorMessage != null ? errorMessage : "Lỗi không xác định");
                }
            }
        }
    }

    // ✅ HTML Templates giữ nguyên như cũ
    private static String createTestEmailContent() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: white; border-radius: 15px; padding: 30px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);'>" +
                "<div style='text-align: center; background: linear-gradient(135deg, #4CAF50, #45a049); color: white; padding: 30px; border-radius: 10px; margin-bottom: 30px;'>" +
                "<h1 style='margin: 0; font-size: 28px;'>🏋️ GYM APP</h1>" +
                "<p style='margin: 10px 0 0 0; font-size: 16px;'>Email Configuration Test</p>" +
                "</div>" +
                "<h2 style='color: #4CAF50; text-align: center;'>✅ Cấu Hình Gmail Thành Công!</h2>" +
                "<p style='font-size: 16px; line-height: 1.6; color: #555; text-align: center;'>Gmail của bạn đã được cấu hình thành công!</p>" +
                "<p style='font-size: 16px; line-height: 1.6; color: #555; text-align: center;'>Bây giờ bạn có thể sử dụng chức năng Quên Mật Khẩu với email thật.</p>" +
                "<div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666;'>" +
                "<p style='margin: 0;'><strong>📧 Test email gửi thành công lúc: " + new java.util.Date().toString() + "</strong></p>" +
                "<p style='margin: 10px 0; font-size: 12px;'>Developed by HienTruongTHMH 🚀</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private static String createEmailContent(String verificationCode) {
        // ✅ Giữ nguyên template email như code cũ của bạn
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: white; border-radius: 15px; padding: 30px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);'>" +

                // Header
                "<div style='text-align: center; background: linear-gradient(135deg, #FF7300, #FF5722); color: white; padding: 40px 30px; border-radius: 15px; margin-bottom: 30px;'>" +
                "<h1 style='margin: 0; font-size: 32px; font-weight: bold;'>🏋️ GYM APP</h1>" +
                "<p style='margin: 15px 0 0 0; font-size: 18px; opacity: 0.9;'>Đặt Lại Mật Khẩu</p>" +
                "</div>" +

                // Content
                "<div style='padding: 0 20px;'>" +
                "<h2 style='color: #333; margin-bottom: 20px; font-size: 24px;'>Xin chào! 👋</h2>" +
                "<p style='font-size: 16px; line-height: 1.8; color: #555; margin-bottom: 30px;'>" +
                "Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản Gym App của bạn. " +
                "Vui lòng sử dụng mã xác thực bên dưới để tiếp tục:</p>" +

                // Verification Code Box
                "<div style='background: linear-gradient(135deg, #f8f9fa, #e9ecef); border: 3px dashed #FF7300; border-radius: 20px; padding: 40px; text-align: center; margin: 40px 0;'>" +
                "<p style='margin: 0 0 15px 0; font-size: 14px; color: #666; text-transform: uppercase; letter-spacing: 2px; font-weight: bold;'>MÃ XÁC THỰC</p>" +
                "<div style='font-size: 48px; font-weight: bold; color: #FF7300; letter-spacing: 12px; font-family: \"Courier New\", monospace; margin: 20px 0;'>" + verificationCode + "</div>" +
                "<p style='margin: 15px 0 0 0; font-size: 12px; color: #999; font-style: italic;'>Nhập mã này vào ứng dụng Gym App</p>" +
                "</div>" +

                // Important Notice
                "<div style='background: #fff3cd; border: 1px solid #ffeaa7; border-left: 5px solid #fdcb6e; border-radius: 10px; padding: 20px; margin: 30px 0;'>" +
                "<h3 style='margin: 0 0 15px 0; color: #856404; font-size: 16px;'>⚠️ Lưu Ý Quan Trọng</h3>" +
                "<ul style='margin: 0; padding-left: 20px; color: #856404; line-height: 1.6;'>" +
                "<li><strong>Mã này có hiệu lực trong 10 phút</strong></li>" +
                "<li>Không chia sẻ mã này với bất kỳ ai</li>" +
                "<li>Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này</li>" +
                "<li>Đảm bảo bạn đang truy cập ứng dụng Gym App chính thức</li>" +
                "</ul>" +
                "</div>" +

                // Footer
                "<div style='text-align: center; margin-top: 40px; padding-top: 30px; border-top: 2px solid #f0f0f0; color: #666;'>" +
                "<h3 style='margin: 0 0 10px 0; color: #FF7300; font-size: 18px;'>Cảm ơn bạn đã sử dụng Gym App! 🏋️‍♀️</h3>" +
                "<p style='margin: 5px 0; font-size: 14px;'>Đây là email tự động, vui lòng không phản hồi.</p>" +
                "<p style='margin: 5px 0; font-size: 12px; color: #999;'>© 2025 Gym App. Developed by HienTruongTHMH</p>" +
                "<p style='margin: 15px 0 0 0; font-size: 11px; color: #ccc;'>📧 Gửi qua Gmail SMTP: " + new java.util.Date().toString() + "</p>" +
                "</div>" +

                "</div>" +
                "</body>" +
                "</html>";
    }
}