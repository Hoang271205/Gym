package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AdminWorkoutActivity extends AppCompatActivity {

    private ListView lvWorkouts;
    private Button btnAddWorkout, btnBack;
    private TextView tvWorkoutCount;
    private DatabaseHelper databaseHelper;
    private List<Workout> workoutList;
    private AdminWorkoutAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_workout);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
        loadWorkoutData();
    }

    private void initViews() {
        lvWorkouts = findViewById(R.id.lvWorkouts);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        btnBack = findViewById(R.id.btnBack);
        tvWorkoutCount = findViewById(R.id.tvWorkoutCount);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminWorkoutActivity.this, AddWorkoutActivity.class);
            startActivity(intent);
        });

        lvWorkouts.setOnItemClickListener((parent, view, position, id) -> {
            Workout workout = workoutList.get(position);
            showWorkoutOptionsDialog(workout);
        });
    }

    private void showWorkoutOptionsDialog(Workout workout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Workout Options");
        builder.setMessage("Choose an action for: " + workout.getTitle());

        builder.setPositiveButton("Delete", (dialog, which) -> {
            confirmDeleteWorkout(workout);
        });

        builder.setNegativeButton("View Details", (dialog, which) -> {
            showWorkoutDetails(workout);
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showWorkoutDetails(Workout workout) {
        String details = "🏋️ " + workout.getTitle() + "\n\n" +
                "📝 Details: " + workout.getDetails() + "\n" +
                "🔥 Calories: " + workout.getCalories() + " kcal\n" +
                "💪 Type: " + workout.getType() + "\n" +
                "⏱️ Duration: " + workout.getDuration() + "\n" +
                "📊 Level: " + workout.getLevel() + "\n" +
                "🖼️ Image: " + workout.getImageName() + "\n" +
                "📱 Activity: " + workout.getActivityClass() + "\n" +
                "📄 Description: " + workout.getDescription() + "\n" +
                "📅 Created: " + workout.getCreatedAt();

        new AlertDialog.Builder(this)
                .setTitle("Workout Details")
                .setMessage(details)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void confirmDeleteWorkout(Workout workout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Workout");
        builder.setMessage("Are you sure you want to delete '" + workout.getTitle() + "'?\n\nThis action cannot be undone.");

        builder.setPositiveButton("Yes, Delete", (dialog, which) -> {
            if (databaseHelper.deleteWorkout(workout.getId())) {
                Toast.makeText(this, "✅ Workout deleted successfully!", Toast.LENGTH_SHORT).show();
                loadWorkoutData(); // Refresh list
            } else {
                Toast.makeText(this, "❌ Failed to delete workout", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void loadWorkoutData() {
        workoutList = databaseHelper.getAllWorkouts();

        // Update count
        tvWorkoutCount.setText(String.valueOf(workoutList.size()));

        // Setup adapter
        ArrayList<String> workoutDisplayList = new ArrayList<>();
        for (Workout workout : workoutList) {
            String workoutInfo = "🏋️ " + workout.getTitle() +
                    "\n📝 " + workout.getDetails() +
                    "\n🔥 " + workout.getCalories() + " kcal" +
                    "\n💪 " + workout.getType() + " • " + workout.getLevel() +
                    "\n⏱️ " + workout.getDuration() + "\n";
            workoutDisplayList.add(workoutInfo);
        }

        adapter = new AdminWorkoutAdapter(this, workoutDisplayList);
        lvWorkouts.setAdapter(adapter);

        if (workoutList.isEmpty()) {
            workoutDisplayList.add("📝 No workouts available.\n\nClick 'Add Workout' to create new exercises.");
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkoutData(); // Refresh data when returning
    }
}