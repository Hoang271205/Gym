package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

    // ✅ NEW: Favorite functionality
    private String currentUserId = "user123"; // TODO: Get from session/preferences/login
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal_workout_details);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadWorkoutData();
        setupClickListeners();
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

            // ✅ THÊM ĐOẠN NÀY: Load user info từ intent
            int userIdInt = intent.getIntExtra("user_id", -1);
            String username = intent.getStringExtra("username");
            String email = intent.getStringExtra("email");

            if (userIdInt != -1) {
                currentUserId = String.valueOf(userIdInt);
                Log.d("UniversalWorkout", "✅ User ID loaded from intent: " + currentUserId);
            } else {
                Log.w("UniversalWorkout", "⚠️ No user_id in intent, using default: " + currentUserId);
            }

            Log.d("UniversalWorkout", "🔍 Final currentUserId: " + currentUserId);
            // ✅ KẾT THÚC ĐOẠN THÊM

            // ✅ Tạo workout object từ intent extras
            if (intent.hasExtra("workout_id")) {
                currentWorkout = new Workout();

                // ✅ FIX: THÊM DÒNG NÀY ĐỂ SET ID
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

        // ✅ DEBUG: In ra thông tin workout SAU KHI SET ID
        Log.d("UniversalWorkout", "=== WORKOUT LOADED SUCCESSFULLY ===");
        Log.d("UniversalWorkout", "User ID: " + currentUserId); // ✅ THÊM DÒNG NÀY
        Log.d("UniversalWorkout", "Workout ID: " + currentWorkout.getId());
        Log.d("UniversalWorkout", "Workout Title: " + currentWorkout.getTitle());
        Log.d("UniversalWorkout", "Workout Type: " + currentWorkout.getType());

        // ✅ NEW: Kiểm tra favorite status
        checkFavoriteStatus();

        // Display workout info
        displayWorkoutInfo();
        loadExerciseInstructions();
    }

    // ✅ NEW: Kiểm tra trạng thái favorite
    private void checkFavoriteStatus() {
        isFavorite = databaseHelper.isFavorite(currentWorkout.getId(), currentUserId);
        updateFavoriteButton();
        Log.d("UniversalWorkout", "🔍 Favorite status: " + isFavorite);
    }

    // ✅ NEW: Cập nhật giao diện favorite button
    private void updateFavoriteButton() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
            btnFavorite.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
            btnFavorite.setColorFilter(getResources().getColor(android.R.color.white));
        }
    }

    // ✅ NEW: Toggle favorite functionality
    private void toggleFavorite() {
        if (isFavorite) {
            // Remove from favorites
            boolean removed = databaseHelper.removeFromFavorites(currentWorkout.getId(), currentUserId);
            if (removed) {
                isFavorite = false;
                updateFavoriteButton();
                Toast.makeText(this, "💔 Removed from favorites", Toast.LENGTH_SHORT).show();
                Log.d("UniversalWorkout", "✅ Removed from favorites: " + currentWorkout.getTitle());
            } else {
                Toast.makeText(this, "❌ Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                Log.e("UniversalWorkout", "❌ Failed to remove from favorites");
            }
        } else {
            // Add to favorites
            boolean added = databaseHelper.addToFavorites(currentWorkout.getId(), currentUserId);
            if (added) {
                isFavorite = true;
                updateFavoriteButton();
                Toast.makeText(this, "❤️ Added to favorites!", Toast.LENGTH_SHORT).show();
                Log.d("UniversalWorkout", "✅ Added to favorites: " + currentWorkout.getTitle());
            } else {
                Toast.makeText(this, "❌ Failed to add to favorites", Toast.LENGTH_SHORT).show();
                Log.e("UniversalWorkout", "❌ Failed to add to favorites");
            }
        }
    }

    private void displayWorkoutInfo() {
        // Set workout image
        String imageName = currentWorkout.getImageName();
        if (imageName != null && !imageName.isEmpty()) {
            int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            if (resourceId != 0) {
                imgWorkout.setImageResource(resourceId);
            } else {
                imgWorkout.setImageResource(R.drawable.pushup_card); // Default image
            }
        } else {
            imgWorkout.setImageResource(R.drawable.pushup_card); // Default image
        }

        // Set difficulty level
        String level = currentWorkout.getLevel();
        if (level != null) {
            tvDifficulty.setText(level);
            // Set difficulty color
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
        Log.d("UniversalWorkout", "🔍 DEBUG: Loading instructions for workout ID: " + workoutId);
        Log.d("UniversalWorkout", "🔍 DEBUG: Workout title: " + currentWorkout.getTitle());
        Log.d("UniversalWorkout", "🔍 DEBUG: Workout type: " + currentWorkout.getType());

        // ✅ THÊM DEBUG: Kiểm tra database trực tiếp
        databaseHelper.debugWorkoutInstructions(workoutId);

        // Load từ database trước
        List<ExerciseInstruction> instructions = databaseHelper.getExerciseInstructions(workoutId);

        Log.d("UniversalWorkout", "📊 Found " + instructions.size() + " custom instructions in database");

        // In chi tiết từng instruction
        for (int i = 0; i < instructions.size(); i++) {
            ExerciseInstruction inst = instructions.get(i);
            Log.d("UniversalWorkout", "✅ Custom Instruction " + (i+1) + ": " + inst.getTitle() + " - " + inst.getDescription());
        }

        // Nếu không có custom instructions, dùng auto-generated
        if (instructions.isEmpty()) {
            Log.d("UniversalWorkout", "⚠️ No custom instructions found, using auto-generated");
            Log.d("UniversalWorkout", "🔍 Will use auto-generated for type: '" + currentWorkout.getType() + "' and title: '" + currentWorkout.getTitle() + "'");

            instructions = ExerciseDataProvider.getExerciseInstructions(
                    currentWorkout.getType(), currentWorkout.getTitle());
            Log.d("UniversalWorkout", "📋 Auto-generated " + instructions.size() + " instructions");

            // Debug auto-generated instructions
            for (int i = 0; i < instructions.size(); i++) {
                ExerciseInstruction inst = instructions.get(i);
                Log.d("UniversalWorkout", "🤖 Auto Instruction " + (i+1) + ": " + inst.getTitle());
            }
        } else {
            Log.d("UniversalWorkout", "🎯 Using " + instructions.size() + " custom instructions from database");
        }

        // Setup RecyclerView
        recyclerViewInstructions.setLayoutManager(new LinearLayoutManager(this));
        instructionAdapter = new ExerciseInstructionAdapter(this, instructions);
        recyclerViewInstructions.setAdapter(instructionAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(this, StartWorkoutActivity.class);

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

        // ✅ NEW: Favorite button với toggle functionality
        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    // ✅ NEW: Refresh favorite status when returning to this activity
    @Override
    protected void onResume() {
        super.onResume();
        if (currentWorkout != null) {
            checkFavoriteStatus();
        }
    }

    // ✅ NEW: Method để set user ID từ bên ngoài (nếu cần)
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        if (currentWorkout != null) {
            checkFavoriteStatus();
        }
    }

    // ✅ NEW: Method để get current user ID (cho testing hoặc debugging)
    public String getCurrentUserId() {
        return currentUserId;
    }
}