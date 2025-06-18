package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvName, tvWelcome;
    private ImageView imgAvatar;
    private RecyclerView recyclerView;
    private WorkoutCardAdapter workoutAdapter;
    private List<WorkoutCard> workoutList;
    private List<WorkoutCard> filteredWorkoutList;
    private DatabaseHelper databaseHelper;
    private EditText etSearch;

    // ✅ NEW: Stats TextViews
    private TextView tvDailyCalories, tvDailyTime, tvTotalWorkout;

    // User info
    private String currentUsername = "Guest";
    private String currentEmail = "";
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadUserInfo();
        setupClickListeners();
        setupRecyclerView();
        setupBottomNavigation();
        setupSearchFunctionality();
        loadDailyStats(); // ✅ Load stats on startup
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvWelcome = findViewById(R.id.tvWelcome);
        imgAvatar = findViewById(R.id.imgAvatar);
        recyclerView = findViewById(R.id.recyclerViewWorkouts);
        etSearch = findViewById(R.id.etSearch);

        // ✅ NEW: Initialize stats TextViews with debugging
        tvDailyCalories = findViewById(R.id.tvDailyCalories);
        tvDailyTime = findViewById(R.id.tvDailyTime);
        tvTotalWorkout = findViewById(R.id.tvTotalWorkout);

        // ✅ DEBUG: Check if views are found
        Log.d("MainActivity", "🔧 View initialization:");
        Log.d("MainActivity", "tvDailyCalories: " + (tvDailyCalories != null ? "✅ Found" : "❌ NULL"));
        Log.d("MainActivity", "tvDailyTime: " + (tvDailyTime != null ? "✅ Found" : "❌ NULL"));
        Log.d("MainActivity", "tvTotalWorkout: " + (tvTotalWorkout != null ? "✅ Found" : "❌ NULL"));

        if (tvTotalWorkout == null) {
            Log.e("MainActivity", "❌ CRITICAL: tvTotalWorkout not found in layout! Check R.id.tvTotalWorkout");
        }
    }

    // ✅ FIXED: Priority fix for login issue
    private void loadUserInfo() {
        try {
            // ✅ FIX: Ưu tiên Intent data từ Login
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("user_id")) {
                currentUserId = intent.getIntExtra("user_id", -1);
                currentUsername = intent.getStringExtra("username");
                currentEmail = intent.getStringExtra("email");

                Log.d("MainActivity", "📥 From Intent - ID: " + currentUserId + ", Username: " + currentUsername);

                // ✅ Nếu có data từ Intent, lưu và RETURN ngay
                if (currentUserId != -1 && currentUsername != null && !currentUsername.isEmpty()) {
                    saveUserToPreferences();
                    updateUserDisplay();
                    Log.d("MainActivity", "✅ Using Intent data: " + currentUsername + " (ID: " + currentUserId + ")");
                    return; // ← QUAN TRỌNG: Không tiếp tục xử lý
                }
            }

            // ✅ CHỈ check SharedPreferences nếu KHÔNG có Intent data
            Log.d("MainActivity", "🔍 Loading from SharedPreferences...");
            loadUserFromPreferences();

            // ✅ VALIDATE: Nếu có valid data từ prefs thì dùng
            if (currentUserId != -1 && !"Guest".equals(currentUsername)) {
                updateUserDisplay();
                Log.d("MainActivity", "✅ Using SharedPreferences data: " + currentUsername + " (ID: " + currentUserId + ")");
                return;
            }

            // ✅ CHỈ fallback database nếu cả Intent và SharedPreferences đều trống
            Log.w("MainActivity", "⚠️ No valid user session found, redirecting to login...");
            redirectToLogin();

        } catch (Exception e) {
            Log.e("MainActivity", "❌ Error in loadUserInfo: " + e.getMessage(), e);
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void clearCorruptedPreferences() {
        try {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Log.d("MainActivity", "✅ Cleared corrupted preferences");
        } catch (Exception e) {
            Log.e("MainActivity", "❌ Error clearing preferences: " + e.getMessage());
        }
    }

    private void loadUserFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        try {
            // ✅ FIX: Try to get as int first, fallback to string conversion
            currentUserId = prefs.getInt("user_id", -1);

            // If currentUserId is still -1, try to get as string and convert
            if (currentUserId == -1) {
                String userIdStr = prefs.getString("user_id", "-1");
                try {
                    currentUserId = Integer.parseInt(userIdStr);
                } catch (NumberFormatException e) {
                    Log.w("MainActivity", "Cannot parse user_id: " + userIdStr + ", using -1");
                    currentUserId = -1;
                }
            }

            currentUsername = prefs.getString("username", "Guest");
            currentEmail = prefs.getString("email", "");

            Log.d("MainActivity", "✅ Loaded from prefs - ID: " + currentUserId + ", Username: " + currentUsername);

        } catch (ClassCastException e) {
            Log.e("MainActivity", "❌ ClassCastException loading preferences: " + e.getMessage());

            // ✅ FALLBACK: Clear corrupted preferences and set defaults
            clearCorruptedPreferences();
            currentUserId = -1;
            currentUsername = "Guest";
            currentEmail = "";
        }
    }

    private void saveUserToPreferences() {
        try {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // ✅ ALWAYS save as INTEGER, not string
            editor.putInt("user_id", currentUserId);
            editor.putString("username", currentUsername != null ? currentUsername : "Guest");
            editor.putString("email", currentEmail != null ? currentEmail : "");

            editor.apply();

            Log.d("MainActivity", "✅ Saved to prefs - ID: " + currentUserId + ", Username: " + currentUsername);

        } catch (Exception e) {
            Log.e("MainActivity", "❌ Error saving preferences: " + e.getMessage());
        }
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

    // ✅ NEW: Load daily stats from database
    private void loadDailyStats() {
        if (currentUserId == -1) {
            Log.w("MainActivity", "⚠️ No user ID, skipping stats");
            return;
        }

        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String userId = String.valueOf(currentUserId);

            Log.d("MainActivity", "📊 Loading stats for user: " + userId + " on " + today);

            // Get today's stats
            DatabaseHelper.DailyStats dailyStats = databaseHelper.getDailyStats(userId, today);

            // Get total workout time
            String totalWorkoutTime = databaseHelper.getTotalWorkoutTime(userId);

            // Update UI
            updateStatsDisplay(dailyStats, totalWorkoutTime);

            Log.d("MainActivity", "📊 Stats loaded - Calories: " + dailyStats.totalCalories +
                    ", Daily Time: " + formatDuration(dailyStats.totalDurationSeconds) +
                    ", Total: " + totalWorkoutTime);

        } catch (Exception e) {
            Log.e("MainActivity", "❌ Error loading daily stats: " + e.getMessage(), e);
            // Set default values on error
            updateStatsDisplay(new DatabaseHelper.DailyStats(), "0min");
        }
    }

    // ✅ FIXED: Update stats display with debugging
    private void updateStatsDisplay(DatabaseHelper.DailyStats dailyStats, String totalWorkoutTime) {
        Log.d("MainActivity", "🔧 updateStatsDisplay called:");
        Log.d("MainActivity", "📊 Daily calories: " + dailyStats.totalCalories);
        Log.d("MainActivity", "⏱️ Daily time seconds: " + dailyStats.totalDurationSeconds);
        Log.d("MainActivity", "🏋️ Total workout time: '" + totalWorkoutTime + "'");

        if (tvDailyCalories != null) {
            tvDailyCalories.setText(String.valueOf(dailyStats.totalCalories));
            Log.d("MainActivity", "✅ Updated tvDailyCalories to: " + dailyStats.totalCalories);
        } else {
            Log.e("MainActivity", "❌ tvDailyCalories is NULL!");
        }

        // Update daily workout time
        if (tvDailyTime != null) {
            String dailyTime = dailyStats.totalDurationSeconds > 0 ?
                    formatDuration(dailyStats.totalDurationSeconds) : "0min";
            tvDailyTime.setText(dailyTime);
            Log.d("MainActivity", "✅ Updated tvDailyTime to: " + dailyTime);
        } else {
            Log.e("MainActivity", "❌ tvDailyTime is NULL!");
        }

        // ✅ FIX: Update total workout time with extra debugging
        if (tvTotalWorkout != null) {
            Log.d("MainActivity", "🔧 Setting tvTotalWorkout to: '" + totalWorkoutTime + "'");
            tvTotalWorkout.setText(totalWorkoutTime);

            // ✅ VERIFY: Check if value was actually set
            String actualText = tvTotalWorkout.getText().toString();
            Log.d("MainActivity", "✅ tvTotalWorkout actual text after update: '" + actualText + "'");

            if (!actualText.equals(totalWorkoutTime)) {
                Log.e("MainActivity", "❌ tvTotalWorkout text mismatch! Expected: '" + totalWorkoutTime + "', Actual: '" + actualText + "'");
                // Try force update
                tvTotalWorkout.post(() -> {
                    tvTotalWorkout.setText(totalWorkoutTime);
                    Log.d("MainActivity", "🔄 Force updated tvTotalWorkout via post()");
                });
            }
        } else {
            Log.e("MainActivity", "❌ tvTotalWorkout is NULL! Check layout XML.");
        }

        Log.d("MainActivity", "✅ Stats UI updated - " +
                "Daily: " + dailyStats.totalCalories + " cal, " +
                formatDuration(dailyStats.totalDurationSeconds) +
                " | Total: " + totalWorkoutTime);
    }

    // ✅ NEW: Helper method to format duration
    private String formatDuration(int seconds) {
        if (seconds <= 0) return "0min";

        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            return minutes + "min";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            if (minutes > 0) {
                return hours + "h " + minutes + "min";
            } else {
                return hours + "h";
            }
        }
    }

    // ✅ NEW: Method để start workout với user data
    private void startWorkoutWithUserData(int workoutId, String workoutTitle, Class<?> activityClass) {
        try {
            Intent intent = new Intent(this, activityClass);
            intent.putExtra("workout_id", workoutId);
            intent.putExtra("workout_title", workoutTitle);

            // ✅ IMPORTANT: Pass current user data
            intent.putExtra("user_id", currentUserId);
            intent.putExtra("username", currentUsername);
            intent.putExtra("email", currentEmail);

            Log.d("MainActivity", "🚀 Starting workout with user data - ID: " + currentUserId + ", Username: " + currentUsername);

            startActivity(intent);
        } catch (Exception e) {
            Log.e("MainActivity", "❌ Error starting workout: " + e.getMessage(), e);
            Toast.makeText(this, "Error starting workout", Toast.LENGTH_SHORT).show();
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
                    // ✅ Pass user data to WorkoutListActivity too
                    intent.putExtra("user_id", currentUserId);
                    intent.putExtra("username", currentUsername);
                    startActivity(intent);
                }
            });
        }

        View featuredCard = findViewById(R.id.cardTwist);
        if (featuredCard != null) {
            featuredCard.setOnClickListener(v -> {
                // ✅ DYNAMIC: Start workout with user data
                startWorkoutWithUserData(4, "Twist Exercise", TwistDetailsActivity.class);
            });
        }
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

        workoutList = new ArrayList<>();
        filteredWorkoutList = new ArrayList<>();

        // Load workouts từ database thay vì hardcode
        loadWorkoutsFromDatabase();

        // Set adapter với filtered list
        workoutAdapter = new WorkoutCardAdapter(filteredWorkoutList);
        recyclerView.setAdapter(workoutAdapter);
    }

    private void loadWorkoutsFromDatabase() {
        List<Workout> workouts = databaseHelper.getAllWorkouts();

        workoutList.clear();
        filteredWorkoutList.clear();

        for (Workout workout : workouts) {
            // Load bitmap từ drawable
            Bitmap bitmap = getBitmapFromDrawableName(workout.getImageName());

            // Get activity class từ string
            Class<?> activityClass = getActivityClassFromString(workout.getActivityClass());

            // Tạo WorkoutCard từ Workout
            WorkoutCard workoutCard = WorkoutCard.fromWorkout(workout, bitmap, activityClass);
            workoutList.add(workoutCard);
        }

        // Copy to filtered list
        filteredWorkoutList.addAll(workoutList);

        Log.d("MainActivity", "Loaded " + workoutList.size() + " workouts from database");
    }

    // Setup search functionality
    private void setupSearchFunctionality() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterWorkouts(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // Filter workouts based on search query
    private void filterWorkouts(String query) {
        filteredWorkoutList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredWorkoutList.addAll(workoutList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (WorkoutCard workout : workoutList) {
                if (workout.getTitle().toLowerCase().contains(lowerQuery) ||
                        workout.getType().toLowerCase().contains(lowerQuery) ||
                        workout.getDetails().toLowerCase().contains(lowerQuery)) {
                    filteredWorkoutList.add(workout);
                }
            }
        }

        if (workoutAdapter != null) {
            workoutAdapter.notifyDataSetChanged();
        }

        Log.d("MainActivity", "Filtered results: " + filteredWorkoutList.size() + " workouts");
    }

    private Bitmap getBitmapFromDrawableName(String imageName) {
        try {
            int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            if (resourceId != 0) {
                return BitmapFactory.decodeResource(getResources(), resourceId);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading image: " + imageName, e);
        }
        // Return default image if not found
        return BitmapFactory.decodeResource(getResources(), R.drawable.pushup_card);
    }

    private Class<?> getActivityClassFromString(String activityClassName) {
        try {
            return Class.forName("com.example.myapplication." + activityClassName);
        } catch (ClassNotFoundException e) {
            Log.e("MainActivity", "Activity class not found: " + activityClassName, e);
            // Return default activity
            return UniversalWorkoutDetailsActivity.class;
        }
    }

    private void setupBottomNavigation() {
        Log.d("MainActivity", "Setting up bottom navigation...");

        try {
            // FAVORITE NAVIGATION - with better error handling
            LinearLayout navFavorite = findViewById(R.id.navFavorite);
            if (navFavorite != null) {
                navFavorite.setOnClickListener(v -> {
                    try {
                        Log.d("MainActivity", "🎯 FAVORITE CLICKED! Starting FavoriteActivity");

                        // Validate user data before navigation
                        if (currentUserId == -1) {
                            Log.w("MainActivity", "⚠️ User ID is -1, using fallback");
                            currentUserId = 1; // Fallback ID
                        }

                        if (currentUsername == null || currentUsername.trim().isEmpty()) {
                            Log.w("MainActivity", "⚠️ Username is empty, using Guest");
                            currentUsername = "Guest";
                        }

                        Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
                        intent.putExtra("user_id", currentUserId);
                        intent.putExtra("username", currentUsername);
                        intent.putExtra("email", currentEmail != null ? currentEmail : "");

                        Log.d("MainActivity", "📤 Passing data - ID: " + currentUserId + ", Username: " + currentUsername);

                        startActivity(intent);
                        Log.d("MainActivity", "✅ FavoriteActivity started successfully");

                    } catch (Exception e) {
                        Log.e("MainActivity", "❌ ERROR starting FavoriteActivity: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this, "Cannot open favorites: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                Log.d("MainActivity", "✅ navFavorite found and set");
            } else {
                Log.e("MainActivity", "❌ navFavorite NOT found in layout!");
            }

            // HOME NAVIGATION
            LinearLayout navHome = findViewById(R.id.navHome);
            if (navHome != null) {
                navHome.setOnClickListener(v -> {
                    Log.d("MainActivity", "Home clicked - Already on home page");
                });
                Log.d("MainActivity", "✅ navHome found and set");
            } else {
                Log.e("MainActivity", "❌ navHome NOT found!");
            }

            // PROFILE NAVIGATION
            LinearLayout navProfile = findViewById(R.id.navProfile);
            if (navProfile != null) {
                navProfile.setOnClickListener(v -> {
                    try {
                        Log.d("MainActivity", "Profile clicked");
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.putExtra("user_id", currentUserId);
                        intent.putExtra("username", currentUsername);
                        intent.putExtra("email", currentEmail);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error navigating to profile: " + e.getMessage(), e);
                    }
                });
                Log.d("MainActivity", "✅ navProfile found and set");
            } else {
                Log.e("MainActivity", "❌ navProfile NOT found!");
            }

            Log.d("MainActivity", "✅ Bottom navigation setup completed");

        } catch (Exception e) {
            Log.e("MainActivity", "❌ FATAL ERROR in setupBottomNavigation: " + e.getMessage(), e);
        }
    }

    // Get current user ID for favorites
    public String getCurrentUserId() {
        return String.valueOf(currentUserId);
    }

    // Get current username
    public String getCurrentUsername() {
        return currentUsername;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ FIXED: Don't reload user info on resume - keep current session
        // loadUserInfo(); // ← Removed to prevent override

        // Refresh workout data
        loadWorkoutsFromDatabase();
        // ✅ NEW: Refresh daily stats
        loadDailyStats();

        if (workoutAdapter != null) {
            workoutAdapter.notifyDataSetChanged();
        }
        Log.d("MainActivity", "MainActivity resumed and refreshed");
    }
}