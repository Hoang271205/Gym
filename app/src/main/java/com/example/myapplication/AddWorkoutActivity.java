package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AddWorkoutActivity extends AppCompatActivity {

    private EditText etTitle, etDetails, etCalories, etDuration, etDescription, etImageName;
    private Spinner spinnerType, spinnerLevel;
    private Button btnSave, btnCancel, btnAddInstruction;
    private RecyclerView recyclerViewInstructions;
    private TextView tvEmptyInstructions;

    private DatabaseHelper databaseHelper;
    private List<ExerciseInstruction> tempInstructions;
    private AdminInstructionAdapter instructionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        databaseHelper = new DatabaseHelper(this);
        tempInstructions = new ArrayList<>();

        initViews();
        setupSpinners();
        setupInstructionsRecyclerView();
        setupClickListeners();
        updateEmptyState();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDetails = findViewById(R.id.etDetails);
        etCalories = findViewById(R.id.etCalories);
        etDuration = findViewById(R.id.etDuration);
        etDescription = findViewById(R.id.etDescription);
        etImageName = findViewById(R.id.etImageName);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnAddInstruction = findViewById(R.id.btnAddInstruction);
        recyclerViewInstructions = findViewById(R.id.recyclerViewInstructions);
        tvEmptyInstructions = findViewById(R.id.tvEmptyInstructions);
    }

    private void setupSpinners() {
        // Workout types
        String[] types = {"Chest", "Back", "Legs", "Core", "Bicep", "Tricep", "Shoulders", "Stamina", "Full Body", "Cardio"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Workout levels
        String[] levels = {"Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);
    }

    private void setupInstructionsRecyclerView() {
        recyclerViewInstructions.setLayoutManager(new LinearLayoutManager(this));
        instructionAdapter = new AdminInstructionAdapter(this, tempInstructions, this::removeInstruction);
        recyclerViewInstructions.setAdapter(instructionAdapter);
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveWorkout());
        btnAddInstruction.setOnClickListener(v -> showAddInstructionDialog());
    }

    private void showAddInstructionDialog() {
        AddInstructionDialog dialog = new AddInstructionDialog(this, instruction -> {
            tempInstructions.add(instruction);
            instructionAdapter.notifyDataSetChanged();
            updateEmptyState();
        });
        dialog.show();
    }

    private void removeInstruction(int position) {
        tempInstructions.remove(position);
        instructionAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (tempInstructions.isEmpty()) {
            tvEmptyInstructions.setVisibility(View.VISIBLE);
            recyclerViewInstructions.setVisibility(View.GONE);
        } else {
            tvEmptyInstructions.setVisibility(View.GONE);
            recyclerViewInstructions.setVisibility(View.VISIBLE);
        }
    }

    private void saveWorkout() {
        String title = etTitle.getText().toString().trim();
        String details = etDetails.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String imageName = etImageName.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String level = spinnerLevel.getSelectedItem().toString();

        // Validation
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(details)) {
            etDetails.setError("Details are required");
            etDetails.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(caloriesStr)) {
            etCalories.setError("Calories are required");
            etCalories.requestFocus();
            return;
        }

        int calories;
        try {
            calories = Integer.parseInt(caloriesStr);
            if (calories < 0) {
                etCalories.setError("Calories must be positive");
                etCalories.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etCalories.setError("Invalid calories number");
            etCalories.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(duration)) {
            etDuration.setError("Duration is required");
            etDuration.requestFocus();
            return;
        }

        // Set default values
        if (TextUtils.isEmpty(imageName)) {
            imageName = "pushup_card";
        }

        if (TextUtils.isEmpty(description)) {
            description = "New workout exercise";
        }

        // Save workout
        long workoutId = databaseHelper.addWorkout(title, details, calories, type, imageName,
                "UniversalWorkoutDetailsActivity", duration, level, description);

        if (workoutId != -1) {
            // Save exercise instructions
            for (int i = 0; i < tempInstructions.size(); i++) {
                ExerciseInstruction instruction = tempInstructions.get(i);
                databaseHelper.addExerciseInstruction((int)workoutId, i + 1,
                        instruction.getTitle(), instruction.getDescription(),
                        instruction.getImageName(), instruction.getDuration());
            }

            Toast.makeText(this, "✅ Workout and instructions added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "❌ Failed to add workout", Toast.LENGTH_SHORT).show();
        }
    }
}