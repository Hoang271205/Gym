package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WorkoutCardAdapter workoutAdapter;
    private List<WorkoutCard> workoutList;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.homepage);

            TextView seeAllTextView = findViewById(R.id.tvSeeAll);

            seeAllTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,WorkoutListActivity.class);
                    startActivity(intent);
                }
            });


            recyclerView = findViewById(R.id.recyclerViewWorkouts);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            Bitmap pushupBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pushup_card);
            Bitmap RunningBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.running_card);
            Bitmap PlankBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plank_card);

            // Sample data
            workoutList = new ArrayList<>();
            workoutList.add(new WorkoutCard("Push-ups", "4 sets of 10 reps", 50, pushupBitmap,PushupDetailsActivity.class));
            workoutList.add(new WorkoutCard("Running", "5 km run", 300, RunningBitmap,PushupDetailsActivity.class));
            workoutList.add(new WorkoutCard("Plank", "Hold for 1 minute", 30, PlankBitmap, PushupDetailsActivity.class));

            // Set adapter
            workoutAdapter = new WorkoutCardAdapter(workoutList);
            recyclerView.setAdapter(workoutAdapter);
        }


}
