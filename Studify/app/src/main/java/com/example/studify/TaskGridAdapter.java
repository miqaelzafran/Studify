package com.example.studify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Displays tasks as two-column colored cards, like the dashboard "OVERVIEW" grid mockup. */
public class TaskGridAdapter extends RecyclerView.Adapter<TaskGridAdapter.CardHolder> {

    public interface OnTaskClick {
        void onClick(Task task);
    }

    private final Context context;
    private final List<Task> tasks;
    private final OnTaskClick listener;

    public TaskGridAdapter(Context context, List<Task> tasks, OnTaskClick listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_task_card, parent, false);
        return new CardHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder holder, int position) {
        Task t = tasks.get(position);
        holder.title.setText(t.getTitle());
        holder.type.setText("[" + t.getType() + "]");
        holder.date.setText(t.getDueDate());
        holder.time.setText(t.getDueTime());

        int bg;
        if ("done".equalsIgnoreCase(t.getStatus())) {
            bg = R.drawable.bg_card_green;
        } else if (DateUtils.isOverdue(t.getDueDate(), t.getDueTime()) && !"done".equalsIgnoreCase(t.getStatus())) {
            bg = R.drawable.bg_card_red;
        } else if (position % 2 == 0) {
            bg = R.drawable.bg_card_blue;
        } else {
            bg = R.drawable.bg_card_gray;
        }
        holder.itemView.setBackgroundResource(bg);

        holder.itemView.setOnClickListener(v -> {
            SoundManager.playClick(context);
            if (listener != null) listener.onClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class CardHolder extends RecyclerView.ViewHolder {
        TextView title, type, date, time;
        CardHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            type = itemView.findViewById(R.id.card_type);
            date = itemView.findViewById(R.id.card_date);
            time = itemView.findViewById(R.id.card_time);
        }
    }
}
