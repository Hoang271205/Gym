package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class StartWorkoutActivity extends AppCompatActivity {

    // UI Components
    private Chronometer chronometerWorkoutTimer;
    private String currentUserId = null;
    private int totalCaloriesBurned = 100;
    private TextView tvWorkoutTitle, tvProgressText, tvCurrentExercise;
    private TextView tvCurrentExerciseDetails, tvPhaseStatus, tvExerciseTimer, tvTimerStatus;
    private ProgressBar progressBarWorkout, progressCircular;
    private LinearLayout llExerciseCards;
    private Button btnPauseResumeWorkout, btnSkipExercise, btnEndWorkout;

    // ✅ ADD: Database helper
    private DatabaseHelper databaseHelper;

    // Workout data
    private List<ExerciseItem> exercises;
    private int currentExerciseIndex = 0;
    private boolean isWorkoutPaused = false;
    private boolean isRestTime = false;
    private long workoutStartTime;

    // ✅ ADD: Workout info from intent
    private int workoutId = -1;
    private String workoutTitle = "";

    // Timers
    private CountDownTimer exerciseTimer;
    private CountDownTimer restTimer;

    // Constants
    private static final int WORK_TIME = 30; // 30 seconds
    private static final int REST_TIME = 30; // 30 seconds rest

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_workout);

        // ✅ ADD: Initialize database
        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadWorkoutData();
        setupExerciseCards();
        setupClickListeners();
        startWorkout();
    }

    private void initViews() {
        chronometerWorkoutTimer = findViewById(R.id.chronometerWorkoutTimer);
        tvWorkoutTitle = findViewById(R.id.tvWorkoutTitle);
        tvProgressText = findViewById(R.id.tvProgressText);
        tvCurrentExercise = findViewById(R.id.tvCurrentExercise);
        tvCurrentExerciseDetails = findViewById(R.id.tvCurrentExerciseDetails);
        tvPhaseStatus = findViewById(R.id.tvPhaseStatus);
        tvExerciseTimer = findViewById(R.id.tvExerciseTimer);
        tvTimerStatus = findViewById(R.id.tvTimerStatus);
        progressBarWorkout = findViewById(R.id.progressBarWorkout);
        progressCircular = findViewById(R.id.progressCircular);
        llExerciseCards = findViewById(R.id.llExerciseCards);
        btnPauseResumeWorkout = findViewById(R.id.btnPauseResumeWorkout);
        btnSkipExercise = findViewById(R.id.btnSkipExercise);
        btnEndWorkout = findViewById(R.id.btnEndWorkout);
    }

    // ✅ DYNAMIC FIX: Load exercises từ database + user data
    private void loadWorkoutData() {
        exercises = new ArrayList<>();

        Intent intent = getIntent();

        // ✅ DYNAMIC: Get workout + user info từ intent
        workoutId = intent.getIntExtra("workout_id", -1);
        workoutTitle = intent.getStringExtra("workout_title");

        // ✅ NEW: Get user info từ Intent (từ MainActivity/WorkoutCardAdapter)
        int userIdFromIntent = intent.getIntExtra("user_id", -1);
        if (userIdFromIntent != -1) {
            currentUserId = String.valueOf(userIdFromIntent);
            Log.d("StartWorkout", "✅ Got user_id from Intent: " + currentUserId);
        }

        Log.d("StartWorkout", "🚀 Loading workout - ID: " + workoutId + ", Title: " + workoutTitle + ", User: " + currentUserId);

        if (workoutTitle != null) {
            tvWorkoutTitle.setText(workoutTitle);
        }

        // ✅ Load exercise instructions từ database
        if (workoutId != -1) {
            try {
                List<ExerciseInstruction> instructions = databaseHelper.getExerciseInstructions(workoutId);
                Log.d("StartWorkout", "📋 Found " + instructions.size() + " instructions from database");

                if (!instructions.isEmpty()) {
                    // Convert database instructions to exercise items
                    for (ExerciseInstruction instruction : instructions) {
                        ExerciseItem exercise = new ExerciseItem(
                                instruction.getTitle(),
                                instruction.getDescription(),
                                instruction.getDuration() > 0 ? instruction.getDuration() : WORK_TIME
                        );
                        exercises.add(exercise);
                        Log.d("StartWorkout", "✅ Added: " + exercise.name + " (" + exercise.duration + "s)");
                    }
                } else {
                    Log.w("StartWorkout", "⚠️ No instructions found in database, generating default");
                    generateDefaultExercises();
                }
            } catch (Exception e) {
                Log.e("StartWorkout", "❌ Error loading from database: " + e.getMessage());
                generateDefaultExercises();
            }
        } else {
            Log.w("StartWorkout", "⚠️ No workout ID provided");
            generateDefaultExercises();
        }

        Log.d("StartWorkout", "🎯 Total exercises loaded: " + exercises.size());
        updateProgressDisplay();
    }

    // ✅ NEW: Generate default exercises based on workout title
    private void generateDefaultExercises() {
        if (workoutTitle != null && workoutTitle.toLowerCase().contains("push")) {
            // Tạo exercises cho Push-up workout
            exercises.add(new ExerciseItem("Push your hands up", "Raise your hands up slowly", 30));
            exercises.add(new ExerciseItem("Push slowly", "Lower down with control", 30));
            Log.d("StartWorkout", "✅ Generated push-up specific exercises");
        } else {
            // Default fallback exercises
            exercises.add(new ExerciseItem("Warm up", "Prepare your body", 30));
            exercises.add(new ExerciseItem("Main exercise", "Perform the movement", 30));
            exercises.add(new ExerciseItem("Cool down", "Relax and breathe", 30));
            Log.d("StartWorkout", "✅ Generated default exercises");
        }
    }

    private void setupExerciseCards() {
        llExerciseCards.removeAllViews();

        Log.d("StartWorkout", "🔧 Setting up " + exercises.size() + " exercise cards");

        for (int i = 0; i < exercises.size(); i++) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.exercise_card_item, llExerciseCards, false);

            TextView tvNumber = cardView.findViewById(R.id.tvExerciseNumber);
            TextView tvName = cardView.findViewById(R.id.tvExerciseName);
            TextView tvDuration = cardView.findViewById(R.id.tvExerciseDuration);
            ImageView ivStatus = cardView.findViewById(R.id.ivExerciseStatus);

            ExerciseItem exercise = exercises.get(i);
            tvNumber.setText(String.valueOf(i + 1));
            tvName.setText(exercise.name);
            tvDuration.setText(exercise.duration + " seconds");

            // Set status
            if (i == currentExerciseIndex) {
                ivStatus.setImageResource(R.drawable.ic_current);
                cardView.setBackgroundResource(R.drawable.exercise_card_current_bg);
            } else if (i < currentExerciseIndex) {
                ivStatus.setImageResource(R.drawable.ic_completed);
            } else {
                ivStatus.setImageResource(R.drawable.ic_pending);
            }

            llExerciseCards.addView(cardView);
        }

        Log.d("StartWorkout", "✅ Exercise cards setup completed");
    }

    private void setupClickListeners() {
        btnPauseResumeWorkout.setOnClickListener(v -> {
            if (isWorkoutPaused) {
                resumeWorkout();
            } else {
                pauseWorkout();
            }
        });

        btnSkipExercise.setOnClickListener(v -> skipCurrentExercise());

        btnEndWorkout.setOnClickListener(v -> showEndWorkoutDialog());
    }

    private void startWorkout() {
        workoutStartTime = SystemClock.elapsedRealtime();
        chronometerWorkoutTimer.setBase(workoutStartTime);
        chronometerWorkoutTimer.start();

        if (exercises.isEmpty()) {
            Toast.makeText(this, "❌ No exercises found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        startCurrentExercise();
    }

    private void startCurrentExercise() {
        if (currentExerciseIndex >= exercises.size()) {
            completeWorkout();
            return;
        }

        ExerciseItem currentExercise = exercises.get(currentExerciseIndex);

        Log.d("StartWorkout", "🎯 Starting exercise " + (currentExerciseIndex + 1) + ": " + currentExercise.name);

        // Update UI
        tvCurrentExercise.setText(currentExercise.name);
        tvCurrentExerciseDetails.setText(currentExercise.description);
        tvPhaseStatus.setText("WORK TIME");
        tvPhaseStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        tvTimerStatus.setText("Keep going! You got this!");

        isRestTime = false;
        startExerciseTimer(currentExercise.duration);
        updateProgressDisplay();
        setupExerciseCards();
    }

    private void startExerciseTimer(int seconds) {
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }

        Log.d("StartWorkout", "⏱️ Starting timer for " + seconds + " seconds");

        exerciseTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) millisUntilFinished / 1000;
                tvExerciseTimer.setText(formatTime(secondsLeft));

                // Update circular progress
                int progress = ((seconds - secondsLeft) * 100) / seconds;
                progressCircular.setProgress(progress);

                // Warning when time is low
                if (secondsLeft <= 5) {
                    tvTimerStatus.setText("Almost done!");
                    tvTimerStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                }
            }

            @Override
            public void onFinish() {
                tvExerciseTimer.setText("00:00");
                progressCircular.setProgress(100);

                if (!isRestTime) {
                    // Exercise completed, start rest
                    startRestPeriod();
                } else {
                    // Rest completed, next exercise
                    nextExercise();
                }
            }
        }.start();
    }

    private void startRestPeriod() {
        if (currentExerciseIndex >= exercises.size() - 1) {
            // Last exercise, no rest needed
            nextExercise();
            return;
        }

        isRestTime = true;
        tvPhaseStatus.setText("REST TIME");
        tvPhaseStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        tvTimerStatus.setText("Take a break, next exercise coming up!");
        tvCurrentExercise.setText("Rest Period");
        tvCurrentExerciseDetails.setText("Get ready for next exercise");

        Log.d("StartWorkout", "😴 Starting rest period");
        startExerciseTimer(REST_TIME);
    }

    private void nextExercise() {
        currentExerciseIndex++;
        Log.d("StartWorkout", "➡️ Moving to exercise " + (currentExerciseIndex + 1));
        startCurrentExercise();
    }

    private void skipCurrentExercise() {
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }

        if (!isRestTime) {
            currentExerciseIndex++;
        }

        if (currentExerciseIndex >= exercises.size()) {
            completeWorkout();
        } else {
            startCurrentExercise();
        }

        Toast.makeText(this, "Exercise skipped", Toast.LENGTH_SHORT).show();
        Log.d("StartWorkout", "⏭️ Exercise skipped");
    }

    private void pauseWorkout() {
        chronometerWorkoutTimer.stop();
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }

        isWorkoutPaused = true;
        btnPauseResumeWorkout.setText("RESUME");
        tvTimerStatus.setText("Workout paused");

        Log.d("StartWorkout", "⏸️ Workout paused");
    }

    private void resumeWorkout() {
        chronometerWorkoutTimer.start();
        isWorkoutPaused = false;
        btnPauseResumeWorkout.setText("PAUSE");

        // Resume current timer
        int currentSeconds = parseTimeToSeconds(tvExerciseTimer.getText().toString());
        startExerciseTimer(currentSeconds);

        Log.d("StartWorkout", "▶️ Workout resumed");
    }

    private void updateProgressDisplay() {
        int progress = ((currentExerciseIndex) * 100) / Math.max(exercises.size(), 1);
        progressBarWorkout.setProgress(progress);
        tvProgressText.setText("Exercise " + (currentExerciseIndex + 1) + " of " + exercises.size());

        Log.d("StartWorkout", "📊 Progress: " + (currentExerciseIndex + 1) + "/" + exercises.size());
    }

    private void completeWorkout() {
        chronometerWorkoutTimer.stop();
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }

        Log.d("StartWorkout", "🎉 Workout completed!");

        // Show completion dialog
        new AlertDialog.Builder(this)
                .setTitle("🎉 Workout Completed!")
                .setMessage("Great job! You've completed all " + exercises.size() + " exercises.\n\nTotal time: " +
                        chronometerWorkoutTimer.getText())
                .setPositiveButton("Finish", (dialog, which) -> {
                    saveWorkoutResult();
                    navigateToMainActivity();
                })
                .setCancelable(false)
                .show();
    }

    private void showEndWorkoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("End Workout")
                .setMessage("Are you sure you want to end this workout?\n\nProgress: " +
                        (currentExerciseIndex + 1) + "/" + exercises.size() + " exercises")
                .setPositiveButton("Yes", (dialog, which) -> {
                    saveWorkoutResult();
                    navigateToMainActivity();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // ✅ DYNAMIC FIX: Save workout results to database
    private void saveWorkoutResult() {
        try {
            // Calculate workout stats
            long workoutDurationMs = SystemClock.elapsedRealtime() - workoutStartTime;
            int durationSeconds = (int) (workoutDurationMs / 1000);
            int completedExercises = currentExerciseIndex;
            int totalExercises = exercises.size();
            int actualCalories = totalExercises > 0 ?
                    (totalCaloriesBurned * completedExercises) / totalExercises : 0;

            // ✅ DYNAMIC FIX: Ensure we have valid user_id
            if (currentUserId == null || currentUserId.trim().isEmpty()) {
                Log.w("StartWorkout", "⚠️ No user_id provided, getting from current session");

                // Method 1: Try DatabaseHelper.getCurrentUserId()
                try {
                    currentUserId = DatabaseHelper.getCurrentUserId(this);
                    if (currentUserId != null && !currentUserId.trim().isEmpty()) {
                        Log.d("StartWorkout", "✅ Got user_id from DatabaseHelper: " + currentUserId);
                    }
                } catch (Exception e) {
                    Log.e("StartWorkout", "❌ DatabaseHelper.getCurrentUserId() failed: " + e.getMessage());
                }

                // Method 2: If still empty, try SharedPreferences directly
                if (currentUserId == null || currentUserId.trim().isEmpty()) {
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    try {
                        // Try as int first
                        int userIdInt = prefs.getInt("user_id", -1);
                        if (userIdInt != -1) {
                            currentUserId = String.valueOf(userIdInt);
                            Log.d("StartWorkout", "✅ Got user_id from SharedPrefs (int): " + currentUserId);
                        } else {
                            // Try as string
                            String userIdStr = prefs.getString("user_id", "");
                            if (!userIdStr.isEmpty()) {
                                currentUserId = userIdStr;
                                Log.d("StartWorkout", "✅ Got user_id from SharedPrefs (string): " + currentUserId);
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("StartWorkout", "❌ Error reading SharedPrefs: " + ex.getMessage());
                    }
                }

                // Method 3: Find current logged in user from database
                if (currentUserId == null || currentUserId.trim().isEmpty()) {
                    try {
                        User currentUser = databaseHelper.getLastLoggedInUser();
                        if (currentUser != null) {
                            currentUserId = String.valueOf(currentUser.getId());
                            Log.d("StartWorkout", "✅ Got user_id from database: " + currentUserId + " (" + currentUser.getUsername() + ")");
                        }
                    } catch (Exception e) {
                        Log.e("StartWorkout", "❌ Error finding user from database: " + e.getMessage());
                    }
                }

                // Final check
                if (currentUserId == null || currentUserId.trim().isEmpty()) {
                    Log.e("StartWorkout", "❌ Could not determine user_id! Cannot save workout.");
                    return;
                }
            }

            if (workoutId == -1) {
                workoutId = 1; // Default workout ID
            }

            Log.d("StartWorkout", "💾 Saving workout result:");
            Log.d("StartWorkout", "👤 User ID: '" + currentUserId + "'");
            Log.d("StartWorkout", "🏋️ Workout ID: " + workoutId + " - " + workoutTitle);
            Log.d("StartWorkout", "📊 Duration: " + durationSeconds + "s");
            Log.d("StartWorkout", "🔥 Calories: " + actualCalories);
            Log.d("StartWorkout", "✅ Exercises: " + completedExercises + "/" + totalExercises);

            // Save to database
            boolean success = databaseHelper.saveWorkoutSession(
                    currentUserId,
                    workoutId,
                    workoutTitle != null ? workoutTitle : "Workout",
                    actualCalories,
                    durationSeconds,
                    completedExercises,
                    totalExercises
            );

            if (success) {
                Log.d("StartWorkout", "✅ Workout session saved successfully!");
            } else {
                Log.e("StartWorkout", "❌ Failed to save workout session");
            }

        } catch (Exception e) {
            Log.e("StartWorkout", "❌ Error saving workout result: " + e.getMessage(), e);
        }
    }

    // ✅ DYNAMIC FIX: Navigate back with user data
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // ✅ DYNAMIC: Pass current user data back to MainActivity
        if (currentUserId != null && !currentUserId.trim().isEmpty()) {
            try {
                int userIdInt = Integer.parseInt(currentUserId);

                // Get current username from database dynamically
                String currentUsername = "User";
                try {
                    User user = databaseHelper.getUserById(userIdInt);
                    if (user != null) {
                        currentUsername = user.getUsername();
                    }
                } catch (Exception e) {
                    Log.e("StartWorkout", "Error getting username: " + e.getMessage());
                }

                intent.putExtra("user_id", userIdInt);
                intent.putExtra("username", currentUsername);
                Log.d("StartWorkout", "📤 Passing user data back to MainActivity: ID=" + currentUserId + ", Username=" + currentUsername);
            } catch (NumberFormatException e) {
                Log.e("StartWorkout", "❌ Invalid user_id format: " + currentUserId);
            }
        }

        startActivity(intent);
        finish();
    }

    // Helper methods
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private int parseTimeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        if (restTimer != null) {
            restTimer.cancel();
        }
    }

    // Inner class for exercise data
    private static class ExerciseItem {
        String name;
        String description;
        int duration; // in seconds

        public ExerciseItem(String name, String description, int duration) {
            this.name = name;
            this.description = description;
            this.duration = duration;
        }
    }
}