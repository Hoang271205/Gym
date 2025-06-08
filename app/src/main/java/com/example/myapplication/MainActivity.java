package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvName, tvWelcome;
    private ImageView imgAvatar;
    private RecyclerView recyclerView;
    private WorkoutCardAdapter workoutAdapter;
    private List<WorkoutCard> workoutList;

    // User info
    private String currentUsername = "Guest";
    private String currentEmail = "";
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        initViews();
        loadUserInfo();
        setupClickListeners();
        setupRecyclerView();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvWelcome = findViewById(R.id.tvWelcome);
        imgAvatar = findViewById(R.id.imgAvatar);
        recyclerView = findViewById(R.id.recyclerViewWorkouts);
    }

    private void loadUserInfo() {
        // Lấy thông tin user từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            currentUserId = intent.getIntExtra("user_id", -1);
            currentUsername = intent.getStringExtra("username");
            currentEmail = intent.getStringExtra("email");
        }

        // Nếu không có thông tin từ Intent, kiểm tra SharedPreferences
        if (currentUsername == null || currentUsername.isEmpty()) {
            loadUserFromPreferences();
        } else {
            // Lưu thông tin user vào SharedPreferences để sử dụng sau
            saveUserToPreferences();
        }

        // Hiển thị thông tin user
        updateUserDisplay();
    }

    private void loadUserFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        currentUsername = prefs.getString("username", "Guest");
        currentEmail = prefs.getString("email", "");
    }

    private void saveUserToPreferences() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("user_id", currentUserId);
        editor.putString("username", currentUsername != null ? currentUsername : "Guest");
        editor.putString("email", currentEmail != null ? currentEmail : "");
        editor.apply();
    }

    private void updateUserDisplay() {
        if (currentUsername != null && !currentUsername.isEmpty()) {
            tvName.setText(currentUsername);
            tvWelcome.setText("welcome to the gym");
        } else {
            tvName.setText("Guest");
            tvWelcome.setText("welcome to the gym");
        }
    }

    private void setupClickListeners() {
        // Click vào avatar để xem profile hoặc logout
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserMenu();
            }
        });


        TextView tvSeeAll = findViewById(R.id.tvSeeAll);
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, WorkoutListActivity.class);
                    startActivity(intent);
                }
            });
        }

        View featuredCard = findViewById(R.id.cardTwist);
        featuredCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TwistDetailsActivity.class);
            startActivity(intent);
        });

        ImageView profile = findViewById(R.id.profile_image);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                // Add user info if needed
                intent.putExtra("user_id", currentUserId);
                intent.putExtra("username", currentUsername);
                intent.putExtra("email", currentEmail);
                startActivity(intent);
            }
        });


    }

    private void showUserMenu() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("User Menu");

        String userInfo = "Logged in as: " + currentUsername;
        if (!currentEmail.isEmpty()) {
            userInfo += "\nEmail: " + currentEmail;
        }

        builder.setMessage(userInfo);

        builder.setPositiveButton("Logout", (dialog, which) -> {
            logout();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void logout() {
        // Xóa thông tin user khỏi SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Chuyển về trang Login
        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupRecyclerView() {
        // Setup RecyclerView cho workout list với layout horizontal
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Tạo bitmap từ resources
        Bitmap pushupBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pushup_card);
        Bitmap RunningBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.running_card);
        Bitmap PlankBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plank_card);

        // Sample data với WorkoutCard
        workoutList = new ArrayList<>();
        workoutList.add(new WorkoutCard("Push-ups", "3 sets of 15 reps", 50, pushupBitmap,PushupDetailsActivity.class,"chest"));
        workoutList.add(new WorkoutCard("Running", "5 km run", 300, RunningBitmap,RunningDetailsActivity.class,"Stamina"));
        workoutList.add(new WorkoutCard("Plank", "Hold for 1 minute", 30, PlankBitmap,RunningDetailsActivity.class,"core"));

        // Set adapter
        workoutAdapter = new WorkoutCardAdapter(workoutList);
        recyclerView.setAdapter(workoutAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user info khi quay lại activity
        loadUserInfo();
    }
}