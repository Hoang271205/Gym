package com.example.myapplication;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WorkoutCardAdapter extends RecyclerView.Adapter<WorkoutCardAdapter.WorkoutViewHolder> {
    private List<WorkoutCard> workoutList;
    private List<WorkoutCard> filteredList;

    public WorkoutCardAdapter(List<WorkoutCard> workoutList) {
        this.filteredList = new ArrayList<>(workoutList);
        this.workoutList = workoutList;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_card, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutCard workoutcard = filteredList.get(position);
        holder.title.setText(workoutcard.getTitle());
        holder.details.setText(workoutcard.getDetails());
        holder.calories.setText(String.valueOf(workoutcard.getCalories()));
        holder.image.setImageBitmap(workoutcard.getImage());

        // ✅ DYNAMIC FIX: Truyền user data từ MainActivity
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();

            try {
                Intent intent = new Intent(context, UniversalWorkoutDetailsActivity.class);

                // ✅ DYNAMIC: Get user data from MainActivity if possible
                String currentUserId = "";
                String currentUsername = "User";

                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    currentUserId = mainActivity.getCurrentUserId();
                    currentUsername = mainActivity.getCurrentUsername();
                    Log.d("WorkoutCardAdapter", "✅ Got user data from MainActivity: ID=" + currentUserId + ", Username=" + currentUsername);
                } else {
                    // Fallback: Get from DatabaseHelper
                    currentUserId = DatabaseHelper.getCurrentUserId(context);
                    Log.d("WorkoutCardAdapter", "✅ Got user data from DatabaseHelper: ID=" + currentUserId);
                }

                Log.d("WorkoutCardAdapter", "🚀 Opening workout ID: " + workoutcard.getId() + " for user: " + currentUserId);

                // ✅ PASS USER DATA DYNAMICALLY
                try {
                    int userIdInt = Integer.parseInt(currentUserId);
                    intent.putExtra("user_id", userIdInt);
                } catch (NumberFormatException e) {
                    Log.w("WorkoutCardAdapter", "Invalid user_id format: " + currentUserId);
                    intent.putExtra("user_id", 1); // Fallback
                }

                intent.putExtra("username", currentUsername);

                // Truyền tất cả workout data
                intent.putExtra("workout_id", workoutcard.getId());
                intent.putExtra("workout_title", workoutcard.getTitle());
                intent.putExtra("workout_details", workoutcard.getDetails());
                intent.putExtra("workout_calories", workoutcard.getCalories());
                intent.putExtra("workout_type", workoutcard.getType());
                intent.putExtra("workout_duration", workoutcard.getDuration());
                intent.putExtra("workout_level", workoutcard.getLevel());
                intent.putExtra("workout_description", workoutcard.getDescription());
                intent.putExtra("workout_image_name", workoutcard.getImageName());
                intent.putExtra("workout_activity_class", "UniversalWorkoutDetailsActivity");

                context.startActivity(intent);

            } catch (Exception e) {
                Log.e("WorkoutCardAdapter", "❌ Error starting workout activity: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(workoutList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (WorkoutCard c : workoutList) {
                if (c.getTitle().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView title, details, calories, activity;
        ImageView image;

        public WorkoutViewHolder(@NonNull View itemview) {
            super(itemview);
            title = itemView.findViewById(R.id.tvWorkoutTitle);
            details = itemView.findViewById(R.id.tvWorkoutDetails);
            calories = itemView.findViewById(R.id.tvWorkoutCalories);
            image = itemView.findViewById(R.id.tvImagecard);
        }
    }
}