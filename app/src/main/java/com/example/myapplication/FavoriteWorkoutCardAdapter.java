package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteWorkoutCardAdapter extends RecyclerView.Adapter<FavoriteWorkoutCardAdapter.FavoriteViewHolder> {

    private List<WorkoutCard> workoutList;
    private List<WorkoutCard> filteredList;
    private OnRemoveFromFavoritesListener onRemoveListener;

    public interface OnRemoveFromFavoritesListener {
        void onRemoveFromFavorites(WorkoutCard workoutCard);
    }

    public FavoriteWorkoutCardAdapter(List<WorkoutCard> workoutList, OnRemoveFromFavoritesListener listener) {
        this.workoutList = new ArrayList<>(workoutList);
        this.filteredList = new ArrayList<>(workoutList);
        this.onRemoveListener = listener;
        Log.d("FavoriteAdapter", "Created adapter with " + workoutList.size() + " items");
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_workout_card, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        WorkoutCard workoutCard = filteredList.get(position);

        Log.d("FavoriteAdapter", "Binding workout: " + workoutCard.getTitle());

        // Set data
        holder.title.setText(workoutCard.getTitle());
        holder.details.setText(workoutCard.getDetails());
        holder.calories.setText(String.valueOf(workoutCard.getCalories()));

        // Set image
        if (workoutCard.getImage() != null) {
            holder.image.setImageBitmap(workoutCard.getImage());
        } else {
            holder.image.setImageResource(R.drawable.pushup_card); // Default image
        }

        // Click to open workout details
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, UniversalWorkoutDetailsActivity.class);

            Log.d("FavoriteAdapter", "Opening workout: " + workoutCard.getTitle() + " (ID: " + workoutCard.getId() + ")");

            intent.putExtra("workout_id", workoutCard.getId());
            intent.putExtra("workout_title", workoutCard.getTitle());
            intent.putExtra("workout_details", workoutCard.getDetails());
            intent.putExtra("workout_calories", workoutCard.getCalories());
            intent.putExtra("workout_type", workoutCard.getType());
            intent.putExtra("workout_duration", workoutCard.getDuration());
            intent.putExtra("workout_level", workoutCard.getLevel());
            intent.putExtra("workout_description", workoutCard.getDescription());
            intent.putExtra("workout_image_name", workoutCard.getImageName());
            intent.putExtra("workout_activity_class", "UniversalWorkoutDetailsActivity");

            context.startActivity(intent);
        });

        // Remove from favorites button
        holder.btnRemove.setOnClickListener(v -> {
            Log.d("FavoriteAdapter", "Removing from favorites: " + workoutCard.getTitle());
            if (onRemoveListener != null) {
                onRemoveListener.onRemoveFromFavorites(workoutCard);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // ✅ Method để filter/search
    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(workoutList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (WorkoutCard card : workoutList) {
                if (card.getTitle().toLowerCase().contains(lowerQuery) ||
                        card.getType().toLowerCase().contains(lowerQuery) ||
                        card.getDetails().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(card);
                }
            }
        }
        notifyDataSetChanged();
        Log.d("FavoriteAdapter", "Filtered results: " + filteredList.size() + " items");
    }

    // ✅ Method để update data khi có thay đổi
    public void updateData(List<WorkoutCard> newWorkoutList) {
        this.workoutList.clear();
        this.workoutList.addAll(newWorkoutList);
        this.filteredList.clear();
        this.filteredList.addAll(newWorkoutList);
        notifyDataSetChanged();
        Log.d("FavoriteAdapter", "Data updated: " + newWorkoutList.size() + " items");
    }

    // ✅ Method để remove item khỏi adapter
    public void removeItem(WorkoutCard workoutCard) {
        int indexInWorkoutList = workoutList.indexOf(workoutCard);
        int indexInFilteredList = filteredList.indexOf(workoutCard);

        if (indexInWorkoutList != -1) {
            workoutList.remove(indexInWorkoutList);
        }

        if (indexInFilteredList != -1) {
            filteredList.remove(indexInFilteredList);
            notifyItemRemoved(indexInFilteredList);
        }

        Log.d("FavoriteAdapter", "Item removed: " + workoutCard.getTitle());
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView title, details, calories;
        ImageView image, btnRemove;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvWorkoutTitle);
            details = itemView.findViewById(R.id.tvWorkoutDetails);
            calories = itemView.findViewById(R.id.tvWorkoutCalories);
            image = itemView.findViewById(R.id.tvImagecard);
            btnRemove = itemView.findViewById(R.id.btnRemoveFavorite);

            // ✅ Đảm bảo tất cả views được tìm thấy
            if (title == null) Log.e("FavoriteAdapter", "tvWorkoutTitle not found");
            if (details == null) Log.e("FavoriteAdapter", "tvWorkoutDetails not found");
            if (calories == null) Log.e("FavoriteAdapter", "tvWorkoutCalories not found");
            if (image == null) Log.e("FavoriteAdapter", "tvImagecard not found");
            if (btnRemove == null) Log.e("FavoriteAdapter", "btnRemoveFavorite not found");
        }
    }
}