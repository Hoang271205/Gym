package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PushupDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_details_pushup);


        Button btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());


        Button start = findViewById(R.id.btnStartWorkout);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PushupDetailsActivity.this, StartWorkoutActivity.class);
                startActivity(intent);
            }
        });

    }
}
