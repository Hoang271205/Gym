package com.example.myapplication;

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

public class WorkoutCardAdapter extends RecyclerView.Adapter<WorkoutCardAdapter.WorkoutViewHolder>  {
    private List<WorkoutCard> workoutList;
    private List<WorkoutCard> filteredList;

    public WorkoutCardAdapter(List<WorkoutCard> workoutList) {
        this.filteredList = new ArrayList<>(workoutList);
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
        WorkoutCard workoutcard = filteredList.get(position);
        holder.title.setText(workoutcard.getTitle());
        holder.details.setText(workoutcard.getDetails());
        holder.calories.setText(String.valueOf(workoutcard.getCalories()));
        holder.image.setImageBitmap(workoutcard.getImage());

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, workoutcard.getActivity());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query){
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(workoutList);
        }else {
            String lowerQuery = query.toLowerCase();
            for (WorkoutCard c : workoutList){
                if (c.getTitle().toLowerCase().contains(lowerQuery)){
                    filteredList.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }



    static class WorkoutViewHolder extends RecyclerView.ViewHolder{
        TextView title,details,calories,activity;
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
