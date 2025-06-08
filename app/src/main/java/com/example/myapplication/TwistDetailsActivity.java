package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TwistDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_details_twist_exercise);

        Button btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());


    }
}
