package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddInstructionDialog extends Dialog {

    private EditText etInstructionTitle, etInstructionDesc, etInstructionImage, etInstructionDuration;
    private Button btnSave, btnCancel;
    private OnInstructionAddedListener listener;

    public interface OnInstructionAddedListener {
        void onInstructionAdded(ExerciseInstruction instruction);
    }

    public AddInstructionDialog(Context context, OnInstructionAddedListener listener) {
        super(context);
        this.listener = listener;
        setContentView(R.layout.dialog_add_instruction);
        initViews();
        setupClickListeners();
        setTitle("Add Exercise Step");
    }

    private void initViews() {
        etInstructionTitle = findViewById(R.id.etInstructionTitle);
        etInstructionDesc = findViewById(R.id.etInstructionDesc);
        etInstructionImage = findViewById(R.id.etInstructionImage);
        etInstructionDuration = findViewById(R.id.etInstructionDuration);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etInstructionTitle.getText().toString().trim();
            String description = etInstructionDesc.getText().toString().trim();
            String imageName = etInstructionImage.getText().toString().trim();
            String durationStr = etInstructionDuration.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                Toast.makeText(getContext(), "Description is required", Toast.LENGTH_SHORT).show();
                return;
            }

            int duration = 0;
            if (!durationStr.isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr);
                    if (duration < 0) {
                        Toast.makeText(getContext(), "Duration must be positive", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid duration", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (imageName.isEmpty()) {
                imageName = "ic_fitness"; // Default image
            }

            ExerciseInstruction instruction = new ExerciseInstruction(title, description, imageName, duration);
            listener.onInstructionAdded(instruction);
            dismiss();
        });
    }
}