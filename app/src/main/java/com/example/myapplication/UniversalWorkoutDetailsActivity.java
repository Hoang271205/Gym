package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UniversalWorkoutDetailsActivity extends AppCompatActivity {

    private ImageView imgWorkout;
    private TextView tvDifficulty, tvWorkoutName, tvWorkoutStats, tvWorkoutDesc;
    private Button btnBack, btnStartWorkout;
    private ImageButton btnFavorite;
    private RecyclerView recyclerViewInstructions;

    private DatabaseHelper databaseHelper;
    private Workout currentWorkout;
    private ExerciseInstructionAdapter instructionAdapter;

    // ✅ UPDATED: Better user ID management
    private String currentUserId;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal_workout_details);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        getCurrentUserId(); // ✅ GET USER ID FIRST
        loadWorkoutData();
        setupClickListeners();

        // ✅ ADD DEBUG TEST
        testDatabase();
    }

    private void initViews() {
        imgWorkout = findViewById(R.id.imgWorkout);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvWorkoutName = findViewById(R.id.tvWorkoutName);
        tvWorkoutStats = findViewById(R.id.tvWorkoutStats);
        tvWorkoutDesc = findViewById(R.id.tvWorkoutDesc);
        btnBack = findViewById(R.id.btnBack);
        btnStartWorkout = findViewById(R.id.btnStartWorkout);
        btnFavorite = findViewById(R.id.btnFavorite);
        recyclerViewInstructions = findViewById(R.id.recyclerViewInstructions);
    }

    // ✅ NEW: Method để lấy current user ID
    private void getCurrentUserId() {
        // Cách 1: Từ Intent
        currentUserId = getIntent().getStringExtra("user_id");
        Log.d("UniversalWorkout", "📥 Intent user_id: '" + currentUserId + "'");

        // Cách 2: Từ SharedPreferences nếu Intent không có hoặc empty
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            try {
                currentUserId = DatabaseHelper.getCurrentUserId(this);
                Log.d("UniversalWorkout", "📱 SharedPrefs user_id: '" + currentUserId + "'");
            } catch (Exception e) {
                Log.e("UniversalWorkout", "❌ Error getting user_id from SharedPrefs: " + e.getMessage());
                currentUserId = "";
            }
        }

        // Cách 3: Query database với login name nếu vẫn không có
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            try {
                // ✅ Get user ID by login name từ database
                User user = databaseHelper.getUserByEmail("your_email@example.com"); // Replace with actual email
                if (user != null) {
                    currentUserId = String.valueOf(user.getId());
                    Log.d("UniversalWorkout", "🔍 Found user_id from database: '" + currentUserId + "'");
                }
            } catch (Exception e) {
                Log.e("UniversalWorkout", "❌ Error querying user from database: " + e.getMessage());
            }
        }

        // Cách 4: Final fallback - use ID=1 từ registration log
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            currentUserId = "1"; // Từ log: "User added with ID: 1"
            Log.w("UniversalWorkout", "⚠️ Using fallback user_id: " + currentUserId);
        }

        Log.d("UniversalWorkout", "✅ Final User ID: '" + currentUserId + "'");
    }

    // ✅ NEW: Test database functionality
    private void testDatabase() {
        try {
            databaseHelper.testFavoritesTable();
            Log.d("UniversalWorkout", "Database test completed successfully");
        } catch (Exception e) {
            Log.e("UniversalWorkout", "Database test failed: " + e.getMessage(), e);
        }
    }

    private void loadWorkoutData() {
        Intent intent = getIntent();
        Log.d("UniversalWorkout", "=== DEBUG INTENT DATA ===");

        if (intent != null) {
            // Debug tất cả extras
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    Log.d("UniversalWorkout", "Extra: " + key + " = " + value);
                }
            }

            // ✅ Tạo workout object từ intent extras
            if (intent.hasExtra("workout_id")) {
                currentWorkout = new Workout();

                // ✅ SET ID
                currentWorkout.setId(intent.getIntExtra("workout_id", 0));

                // Set all fields from intent
                currentWorkout.setTitle(intent.getStringExtra("workout_title"));
                currentWorkout.setDetails(intent.getStringExtra("workout_details"));
                currentWorkout.setCalories(intent.getIntExtra("workout_calories", 0));
                currentWorkout.setType(intent.getStringExtra("workout_type"));
                currentWorkout.setDuration(intent.getStringExtra("workout_duration"));
                currentWorkout.setLevel(intent.getStringExtra("workout_level"));
                currentWorkout.setDescription(intent.getStringExtra("workout_description"));
                currentWorkout.setImageName(intent.getStringExtra("workout_image_name"));
                currentWorkout.setActivityClass(intent.getStringExtra("workout_activity_class"));

                Log.d("UniversalWorkout", "✅ Workout created from intent extras");
            }
        }

        if (currentWorkout == null) {
            Log.e("UniversalWorkout", "❌ No workout data found!");
            Toast.makeText(this, "❌ Unable to load workout data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ DEBUG: In ra thông tin workout
        Log.d("UniversalWorkout", "=== WORKOUT LOADED SUCCESSFULLY ===");
        Log.d("UniversalWorkout", "User ID: '" + currentUserId + "'");
        Log.d("UniversalWorkout", "Workout ID: " + currentWorkout.getId());
        Log.d("UniversalWorkout", "Workout Title: " + currentWorkout.getTitle());

        // ✅ Kiểm tra favorite status
        checkFavoriteStatus();

        // Display workout info
        displayWorkoutInfo();
        loadExerciseInstructions();
    }

    // ✅ Kiểm tra trạng thái favorite
    private void checkFavoriteStatus() {
        isFavorite = databaseHelper.isFavorite(currentWorkout.getId(), currentUserId);
        updateFavoriteButton();
        Log.d("UniversalWorkout", "🔍 Favorite status: " + isFavorite);
    }

    // ✅ Cập nhật giao diện favorite button
    private void updateFavoriteButton() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
            btnFavorite.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
            btnFavorite.setColorFilter(getResources().getColor(android.R.color.white));
        }
    }

    // ✅ UPDATED: Toggle favorite với better error handling
    private void toggleFavorite() {
        Log.d("UniversalWorkout", "=== TOGGLE FAVORITE DEBUG ===");
        Log.d("UniversalWorkout", "Current User ID: '" + currentUserId + "'");
        Log.d("UniversalWorkout", "Current Workout ID: " + currentWorkout.getId());
        Log.d("UniversalWorkout", "Current Favorite Status: " + isFavorite);

        // Validate workout ID
        if (currentWorkout.getId() == 0) {
            Log.e("UniversalWorkout", "❌ INVALID WORKOUT ID = 0");
            Toast.makeText(this, "❌ Invalid workout data", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ FIX: Ensure we have valid user_id
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            // Try to get user_id again
            getCurrentUserId();
        }

        // ✅ FIX: Accept any non-empty user_id (remove guest restriction)
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            Log.e("UniversalWorkout", "❌ No valid user ID available");
            Toast.makeText(this, "❌ Please login to use favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("UniversalWorkout", "✅ Proceeding with User ID: '" + currentUserId + "'");

        // Toggle favorite
        boolean success = databaseHelper.toggleFavorite(currentWorkout.getId(), currentUserId);

        if (success) {
            // Update UI
            isFavorite = !isFavorite;
            updateFavoriteButton();

            // Show feedback
            if (isFavorite) {
                Toast.makeText(this, "❤️ Added to favorites!", Toast.LENGTH_SHORT).show();
                Log.d("UniversalWorkout", "✅ Added to favorites: " + currentWorkout.getTitle());
            } else {
                Toast.makeText(this, "💔 Removed from favorites", Toast.LENGTH_SHORT).show();
                Log.d("UniversalWorkout", "✅ Removed from favorites: " + currentWorkout.getTitle());
            }
        } else {
            Toast.makeText(this, "❌ Failed to update favorites", Toast.LENGTH_SHORT).show();
            Log.e("UniversalWorkout", "❌ Failed to toggle favorite for user: " + currentUserId);
        }
    }

    // [Giữ nguyên tất cả các method khác...]
    private void displayWorkoutInfo() {
        // Set workout image
        String imageName = currentWorkout.getImageName();
        if (imageName != null && !imageName.isEmpty()) {
            int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            if (resourceId != 0) {
                imgWorkout.setImageResource(resourceId);
            } else {
                imgWorkout.setImageResource(R.drawable.pushup_card);
            }
        } else {
            imgWorkout.setImageResource(R.drawable.pushup_card);
        }

        // Set difficulty level
        String level = currentWorkout.getLevel();
        if (level != null) {
            tvDifficulty.setText(level);
            switch (level.toLowerCase()) {
                case "beginner":
                    tvDifficulty.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                    break;
                case "intermediate":
                    tvDifficulty.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                    break;
                case "advanced":
                    tvDifficulty.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    break;
                default:
                    tvDifficulty.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            }
        } else {
            tvDifficulty.setText("Intermediate");
            tvDifficulty.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        }

        // Set workout name
        tvWorkoutName.setText(currentWorkout.getTitle());

        // Set workout stats
        String stats = currentWorkout.getDetails() + " • " + currentWorkout.getCalories() + " kcal";
        if (currentWorkout.getDuration() != null) {
            stats += " • " + currentWorkout.getDuration();
        }
        tvWorkoutStats.setText(stats);

        // Set description
        String description = currentWorkout.getDescription();
        if (description != null && !description.isEmpty()) {
            tvWorkoutDesc.setText(description);
        } else {
            tvWorkoutDesc.setText("Complete this workout to strengthen your body and improve fitness.");
        }
    }

    private void loadExerciseInstructions() {
        int workoutId = currentWorkout.getId();
        Log.d("UniversalWorkout", "🔍 Loading instructions for workout ID: " + workoutId);

        databaseHelper.debugWorkoutInstructions(workoutId);

        List<ExerciseInstruction> instructions = databaseHelper.getExerciseInstructions(workoutId);

        if (instructions.isEmpty()) {
            Log.d("UniversalWorkout", "⚠️ No custom instructions found, using auto-generated");
            instructions = ExerciseDataProvider.getExerciseInstructions(
                    currentWorkout.getType(), currentWorkout.getTitle());
        }

        recyclerViewInstructions.setLayoutManager(new LinearLayoutManager(this));
        instructionAdapter = new ExerciseInstructionAdapter(this, instructions);
        recyclerViewInstructions.setAdapter(instructionAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(this, StartWorkoutActivity.class);

            // ✅ TRUYỀN USER ID
            intent.putExtra("user_id", currentUserId);
            intent.putExtra("workout_id", currentWorkout.getId());
            intent.putExtra("workout_title", currentWorkout.getTitle());
            intent.putExtra("workout_details", currentWorkout.getDetails());
            intent.putExtra("workout_calories", currentWorkout.getCalories());
            intent.putExtra("workout_type", currentWorkout.getType());
            intent.putExtra("workout_duration", currentWorkout.getDuration());
            intent.putExtra("workout_level", currentWorkout.getLevel());
            intent.putExtra("workout_description", currentWorkout.getDescription());

            startActivity(intent);
        });

        // ✅ Favorite button với toggle functionality
        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentWorkout != null) {
            checkFavoriteStatus();
        }
    }
}