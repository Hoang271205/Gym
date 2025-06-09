package com.example.myapplication;

import android.graphics.Bitmap;

public class WorkoutCard {
    private int id;
    private String title;
    private String details;
    private String type;
    private int calories;
    private Bitmap image;
    private Class<?> activity;
    private String duration;
    private String level;
    private String description;  // ✅ Thêm mới
    private String imageName;    // ✅ Thêm mới

    // ✅ Constructor đầy đủ từ database (updated)
    public WorkoutCard(int id, String title, String details, int calories, Bitmap image,
                       Class<?> activity, String type, String duration, String level,
                       String description, String imageName) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.calories = calories;
        this.image = image;
        this.activity = activity;
        this.type = type;
        this.duration = duration;
        this.level = level;
        this.description = description;
        this.imageName = imageName;
    }

    // Constructor từ database (backward compatibility)
    public WorkoutCard(int id, String title, String details, int calories, Bitmap image,
                       Class<?> activity, String type, String duration, String level) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.calories = calories;
        this.image = image;
        this.activity = activity;
        this.type = type;
        this.duration = duration;
        this.level = level;
        this.description = ""; // Default value
        this.imageName = "";   // Default value
    }

    // Constructor cũ (để backward compatibility)
    public WorkoutCard(String title, String details, int calories, Bitmap image, Class<?> activity, String type) {
        this.title = title;
        this.details = details;
        this.calories = calories;
        this.image = image;
        this.activity = activity;
        this.type = type;
        this.description = ""; // Default value
        this.imageName = "";   // Default value
    }

    // ✅ Getters đầy đủ
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public Class<?> getActivity() { return activity; }
    public int getCalories() { return calories; }
    public Bitmap getImage() { return image; }
    public String getType() { return type; }
    public String getDuration() { return duration; }
    public String getLevel() { return level; }
    public String getDescription() { return description; }  // ✅ Thêm mới
    public String getImageName() { return imageName; }      // ✅ Thêm mới

    // ✅ Method để tạo WorkoutCard từ Workout object (updated)
    public static WorkoutCard fromWorkout(Workout workout, Bitmap image, Class<?> activityClass) {
        return new WorkoutCard(
                workout.getId(),
                workout.getTitle(),
                workout.getDetails(),
                workout.getCalories(),
                image,
                activityClass,
                workout.getType(),
                workout.getDuration(),
                workout.getLevel(),
                workout.getDescription() != null ? workout.getDescription() : "",
                workout.getImageName() != null ? workout.getImageName() : ""
        );
    }
}