package com.example.myapplication;

public class Workout {
    private int id;
    private String title;
    private String details;
    private int calories;
    private String type;
    private String imageName;
    private String activityClass;
    private String duration;
    private String level;
    private String description;
    private String createdAt;

    // Constructor rỗng
    public Workout() {
    }

    // Constructor với tham số
    public Workout(String title, String details, int calories, String type,
                   String imageName, String activityClass, String duration, String level) {
        this.title = title;
        this.details = details;
        this.calories = calories;
        this.type = type;
        this.imageName = imageName;
        this.activityClass = activityClass;
        this.duration = duration;
        this.level = level;
    }

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getActivityClass() { return activityClass; }
    public void setActivityClass(String activityClass) { this.activityClass = activityClass; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", calories=" + calories +
                ", duration='" + duration + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}