package com.example.myapplication;

import android.graphics.Bitmap;

public class WorkoutCard {
    private String title;
    private String details;
    private int calories;

    private Bitmap image;

    public WorkoutCard(String title, String details, int calories, Bitmap image) {
        this.title = title;
        this.details = details;
        this.calories = calories;
        this.image = image;
    }

    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public int getCalories() { return calories; }

    public Bitmap getImage() { return image;}


}
