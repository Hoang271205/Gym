package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class AdminWorkoutAdapter extends BaseAdapter {
    private Context context;
    private List<String> workoutDisplayList;
    private LayoutInflater inflater;

    public AdminWorkoutAdapter(Context context, List<String> workoutDisplayList) {
        this.context = context;
        this.workoutDisplayList = workoutDisplayList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return workoutDisplayList.size();
    }

    @Override
    public Object getItem(int position) {
        return workoutDisplayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(workoutDisplayList.get(position));
        holder.textView.setTextSize(14);
        holder.textView.setPadding(16, 16, 16, 16);
        holder.textView.setLineSpacing(4, 1.2f); // Tăng line spacing cho dễ đọc

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
    }
}