package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutCardAdapter extends RecyclerView.Adapter<WorkoutCardAdapter.WorkoutViewHolder>  {
    private List<WorkoutCard> workoutList;

    public WorkoutCardAdapter(List<WorkoutCard> workoutList) {
        this.workoutList = workoutList;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_card,parent,false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutCard workoutcard = workoutList.get(position);
        holder.title.setText(workoutcard.getTitle());
        holder.details.setText(workoutcard.getDetails());
        holder.calories.setText(String.valueOf(workoutcard.getCalories()));
        holder.image.setImageBitmap(workoutcard.getImage());
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }
    static class WorkoutViewHolder extends RecyclerView.ViewHolder{
        TextView title,details,calories;
        ImageView image;
        public WorkoutViewHolder(@NonNull View itemview){
            super(itemview);
            title = itemView.findViewById(R.id.tvWorkoutTitle);
            details = itemView.findViewById(R.id.tvWorkoutDetails);
            calories = itemView.findViewById(R.id.tvWorkoutCalories);
            image = itemView.findViewById(R.id.tvImagecard);
        }
    }


}
