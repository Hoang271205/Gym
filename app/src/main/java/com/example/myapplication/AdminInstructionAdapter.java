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

public class AdminInstructionAdapter extends RecyclerView.Adapter<AdminInstructionAdapter.ViewHolder> {

    private Context context;
    private List<ExerciseInstruction> instructions;
    private OnRemoveClickListener removeListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public AdminInstructionAdapter(Context context, List<ExerciseInstruction> instructions,
                                   OnRemoveClickListener removeListener) {
        this.context = context;
        this.instructions = instructions;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_instruction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseInstruction instruction = instructions.get(position);

        holder.tvStepNumber.setText(String.valueOf(position + 1));
        holder.tvTitle.setText(instruction.getTitle());
        holder.tvDescription.setText(instruction.getDescription());
        holder.tvImageName.setText("📷 " + instruction.getImageName());

        if (instruction.getDuration() > 0) {
            holder.tvDuration.setText(instruction.getDuration() + "s");
            holder.tvDuration.setVisibility(View.VISIBLE);
        } else {
            holder.tvDuration.setVisibility(View.GONE);
        }

        holder.btnRemove.setOnClickListener(v -> removeListener.onRemoveClick(position));
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvTitle, tvDescription, tvImageName, tvDuration;
        ImageView btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvImageName = itemView.findViewById(R.id.tvImageName);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}