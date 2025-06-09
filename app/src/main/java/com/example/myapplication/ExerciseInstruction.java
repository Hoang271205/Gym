package com.example.myapplication;

public class ExerciseInstruction {
    private String title;
    private String description;
    private String imageName;
    private int duration; // seconds

    public ExerciseInstruction(String title, String description, String imageName, int duration) {
        this.title = title;
        this.description = description;
        this.imageName = imageName;
        this.duration = duration;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageName() { return imageName; }
    public int getDuration() { return duration; }
}