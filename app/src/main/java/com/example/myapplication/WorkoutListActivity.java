package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WorkoutListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkoutCardAdapter adapter;
    private List<WorkoutCard> workoutCardList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workouts);

        databaseHelper = new DatabaseHelper(this);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewWorkouts);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Load workouts từ database
        loadWorkoutsFromDatabase();

        adapter = new WorkoutCardAdapter(workoutCardList);
        recyclerView.setAdapter(adapter);
    }

    private void loadWorkoutsFromDatabase() {
        List<Workout> workouts = databaseHelper.getAllWorkouts();

        workoutCardList = new ArrayList<>();

        for (Workout workout : workouts) {
            Bitmap bitmap = getBitmapFromDrawableName(workout.getImageName());
            Class<?> activityClass = getActivityClassFromString(workout.getActivityClass());

            WorkoutCard workoutCard = WorkoutCard.fromWorkout(workout, bitmap, activityClass);
            workoutCardList.add(workoutCard);
        }

        Log.d("WorkoutListActivity", "Loaded " + workoutCardList.size() + " workouts from database");
    }

    private Bitmap getBitmapFromDrawableName(String imageName) {
        try {
            int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            if (resourceId != 0) {
                return BitmapFactory.decodeResource(getResources(), resourceId);
            }
        } catch (Exception e) {
            Log.e("WorkoutListActivity", "Error loading image: " + imageName, e);
        }
        // Return default image if not found
        return BitmapFactory.decodeResource(getResources(), R.drawable.pushup_card);
    }

    private Class<?> getActivityClassFromString(String activityClassName) {
        try {
            return Class.forName("com.example.myapplication." + activityClassName);
        } catch (ClassNotFoundException e) {
            Log.e("WorkoutListActivity", "Activity class not found: " + activityClassName, e);
            // Return default activity
            return PushupDetailsActivity.class;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh workout data when returning to this activity
        loadWorkoutsFromDatabase();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}