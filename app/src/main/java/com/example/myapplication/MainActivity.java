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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvName, tvWelcome;
    private ImageView imgAvatar;
    private RecyclerView recyclerView;
    private WorkoutCardAdapter workoutAdapter;
    private List<WorkoutCard> workoutList;
    private List<WorkoutCard> filteredWorkoutList;
    private DatabaseHelper databaseHelper;
    private EditText etSearch;

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
        setupBottomNavigation(); // ✅ Bottom navigation setup
        setupSearchFunctionality(); // ✅ Search functionality
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvWelcome = findViewById(R.id.tvWelcome);
        imgAvatar = findViewById(R.id.imgAvatar);
        recyclerView = findViewById(R.id.recyclerViewWorkouts);
        etSearch = findViewById(R.id.etSearch);
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
        if (featuredCard != null) {
            featuredCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, TwistDetailsActivity.class);
                startActivity(intent);
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

    // ✅ FIXED: Setup bottom navigation với proper error handling
    // Thay thế setupBottomNavigation() method trong MainActivity
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
        // Refresh user info khi quay lại activity
        loadUserInfo();
        // Refresh workout data
        loadWorkoutsFromDatabase();
        if (workoutAdapter != null) {
            workoutAdapter.notifyDataSetChanged();
        }
        Log.d("MainActivity", "MainActivity resumed and refreshed");
    }
}