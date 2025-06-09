package com.example.myapplication;

public class FavoriteWorkout {
    private int id;
    private int workoutId;
    private String userId;
    private String addedAt;

    // Constructors
    public FavoriteWorkout() {}

    public FavoriteWorkout(int workoutId, String userId) {
        this.workoutId = workoutId;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWorkoutId() { return workoutId; }
    public void setWorkoutId(int workoutId) { this.workoutId = workoutId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAddedAt() { return addedAt; }
    public void setAddedAt(String addedAt) { this.addedAt = addedAt; }
}