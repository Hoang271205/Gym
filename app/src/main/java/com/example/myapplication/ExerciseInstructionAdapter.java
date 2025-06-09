package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExerciseInstructionAdapter extends RecyclerView.Adapter<ExerciseInstructionAdapter.ViewHolder> {

    private Context context;
    private List<ExerciseInstruction> instructions;

    public ExerciseInstructionAdapter(Context context, List<ExerciseInstruction> instructions) {
        this.context = context;
        this.instructions = instructions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise_instruction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseInstruction instruction = instructions.get(position);

        holder.tvTitle.setText(instruction.getTitle());
        holder.tvDescription.setText(instruction.getDescription());

        // Load image
        try {
            int resourceId = context.getResources().getIdentifier(
                    instruction.getImageName(), "drawable", context.getPackageName());

            if (resourceId != 0) {
                holder.ivExercise.setImageResource(resourceId);
            } else {
                holder.ivExercise.setImageResource(R.drawable.pushup_card);
            }
        } catch (Exception e) {
            holder.ivExercise.setImageResource(R.drawable.pushup_card);
        }

        // Show duration if available
        if (instruction.getDuration() > 0) {
            holder.tvDuration.setVisibility(View.VISIBLE);
            holder.tvDuration.setText(instruction.getDuration() + "s");
        } else {
            holder.tvDuration.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivExercise;
        TextView tvTitle, tvDescription, tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExercise = itemView.findViewById(R.id.ivExercise);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}