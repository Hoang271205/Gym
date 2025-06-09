package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvEmail;
    private Button btnSignOut;

    // User info
    private String currentUsername = "Guest";
    private String currentEmail = "";
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        // ✅ Load user info from intent
        loadUserInfo();

        initViews();
        setupClickListeners();
        setupBottomNavigation(); // ✅ ADD navigation setup
    }

    // ✅ NEW: Load user info from intent
    private void loadUserInfo() {
        Intent intent = getIntent();
        if (intent != null) {
            currentUserId = intent.getIntExtra("user_id", -1);
            currentUsername = intent.getStringExtra("username");
            currentEmail = intent.getStringExtra("email");

            Log.d("ProfileActivity", "User loaded: " + currentUsername + " (ID: " + currentUserId + ")");
        }

        // Fallback to SharedPreferences if intent data is missing
        if (currentUsername == null || currentUsername.isEmpty()) {
            loadUserFromPreferences();
        }
    }

    private void loadUserFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        currentUsername = prefs.getString("username", "Guest");
        currentEmail = prefs.getString("email", "");
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        btnSignOut = findViewById(R.id.btnSignOut);

        // ✅ Update UI with user info
        updateUserDisplay();
    }

    private void updateUserDisplay() {
        if (currentUsername != null && !currentUsername.isEmpty()) {
            tvUserName.setText(currentUsername);
        } else {
            tvUserName.setText("Guest User");
        }

        if (currentEmail != null && !currentEmail.isEmpty()) {
            tvEmail.setText(currentEmail);
        } else {
            tvEmail.setText("No email provided");
        }
    }

    private void setupClickListeners() {
        btnSignOut.setOnClickListener(v -> {
            signOut();
        });
    }

    // ✅ NEW: Setup bottom navigation
    private void setupBottomNavigation() {
        Log.d("ProfileActivity", "Setting up bottom navigation...");

        // Home navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Log.d("ProfileActivity", "Navigating to Home");
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("user_id", currentUserId);
                intent.putExtra("username", currentUsername);
                intent.putExtra("email", currentEmail);
                startActivity(intent);
                finish();
            });
            Log.d("ProfileActivity", "✅ navHome set");
        }

        // Favorite navigation
        LinearLayout navFavorite = findViewById(R.id.navFavorite);
        if (navFavorite != null) {
            navFavorite.setOnClickListener(v -> {
                Log.d("ProfileActivity", "Navigating to Favorites");
                Intent intent = new Intent(this, FavoriteActivity.class);
                intent.putExtra("user_id", currentUserId);
                intent.putExtra("username", currentUsername);
                intent.putExtra("email", currentEmail);
                startActivity(intent);
            });
            Log.d("ProfileActivity", "✅ navFavorite set");
        }

        // Profile navigation (current page)
        LinearLayout navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Log.d("ProfileActivity", "Already on Profile page");
                // Already on profile page
            });
            Log.d("ProfileActivity", "✅ navProfile set");
        }
    }

    private void signOut() {
        // Clear SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Navigate to Login
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Log.d("ProfileActivity", "User signed out");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserDisplay();
        Log.d("ProfileActivity", "Profile activity resumed");
    }
}