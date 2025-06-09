package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class ExerciseDataProvider {

    public static List<ExerciseInstruction> getExerciseInstructions(String workoutType, String workoutTitle) {
        List<ExerciseInstruction> instructions = new ArrayList<>();

        switch (workoutType.toLowerCase()) {
            case "chest":
                if (workoutTitle.toLowerCase().contains("push")) {
                    instructions.add(new ExerciseInstruction(
                            "STANDARD PUSH-UP",
                            "Classic push-up for entire upper body",
                            "standard_pushup", 30));
                    instructions.add(new ExerciseInstruction(
                            "WIDE PUSH-UP",
                            "Targets chest muscles specifically",
                            "wide_pushup", 30));
                    instructions.add(new ExerciseInstruction(
                            "NARROW PUSH-UP",
                            "Core and arm stability focus",
                            "narrow_pushup", 30));
                } else if (workoutTitle.toLowerCase().contains("dumbell")) {
                    instructions.add(new ExerciseInstruction(
                            "CHEST PRESS SETUP",
                            "Position dumbbells at chest level",
                            "dumbell_setup", 0));
                    instructions.add(new ExerciseInstruction(
                            "PRESS MOVEMENT",
                            "Push weights up, squeeze chest",
                            "dumbell_press", 45));
                    instructions.add(new ExerciseInstruction(
                            "CONTROLLED DESCENT",
                            "Lower weights slowly to chest",
                            "dumbell_lower", 30));
                }
                break;

            case "stamina":
            case "cardio":
                if (workoutTitle.toLowerCase().contains("running")) {
                    instructions.add(new ExerciseInstruction(
                            "WARM-UP JOG",
                            "Start with light jogging pace",
                            "running_warmup", 300));
                    instructions.add(new ExerciseInstruction(
                            "STEADY PACE",
                            "Maintain consistent running speed",
                            "running_steady", 1200));
                    instructions.add(new ExerciseInstruction(
                            "COOL DOWN",
                            "Gradually reduce pace to walk",
                            "running_cooldown", 180));
                }
                break;

            case "core":
                if (workoutTitle.toLowerCase().contains("plank")) {
                    instructions.add(new ExerciseInstruction(
                            "PLANK POSITION",
                            "Hold steady plank form",
                            "plank_hold", 60));
                    instructions.add(new ExerciseInstruction(
                            "SIDE PLANK LEFT",
                            "Turn to left side plank",
                            "side_plank_left", 30));
                    instructions.add(new ExerciseInstruction(
                            "SIDE PLANK RIGHT",
                            "Turn to right side plank",
                            "side_plank_right", 30));
                }
                break;

            case "bicep":
                if (workoutTitle.toLowerCase().contains("twist")) {
                    instructions.add(new ExerciseInstruction(
                            "BICEP CURL",
                            "Standard bicep curling motion",
                            "bicep_curl", 0));
                    instructions.add(new ExerciseInstruction(
                            "HAMMER CURL",
                            "Neutral grip hammer curls",
                            "hammer_curl", 0));
                    instructions.add(new ExerciseInstruction(
                            "TWIST CURL",
                            "Rotate wrists while curling",
                            "twist_curl", 0));
                }
                break;

            case "legs":
                instructions.add(new ExerciseInstruction(
                        "SQUAT POSITION",
                        "Feet shoulder-width apart",
                        "squat_start", 0));
                instructions.add(new ExerciseInstruction(
                        "SQUAT DOWN",
                        "Lower body, keep back straight",
                        "squat_down", 30));
                instructions.add(new ExerciseInstruction(
                        "SQUAT UP",
                        "Push through heels to stand",
                        "squat_up", 30));
                break;

            default:
                // Generic workout instructions
                instructions.add(new ExerciseInstruction(
                        "WARM UP",
                        "Prepare your body for exercise",
                        "warmup_generic", 120));
                instructions.add(new ExerciseInstruction(
                        "MAIN EXERCISE",
                        "Perform the primary workout movement",
                        "exercise_generic", 0));
                instructions.add(new ExerciseInstruction(
                        "COOL DOWN",
                        "Stretch and relax muscles",
                        "cooldown_generic", 120));
                break;
        }

        return instructions;
    }
}