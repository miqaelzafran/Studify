package com.example.studify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Displays tasks as full-width rows with a status toggle button (used in Search & Task List). */
public class TaskRowAdapter extends RecyclerView.Adapter<TaskRowAdapter.RowHolder> {

    public interface OnStatusToggle {
        void onToggle(Task task);
    }

    public interface OnTaskClick {
        void onClick(Task task);
    }

    private final Context context;
    private final List<Task> tasks;
    private final OnStatusToggle listener;
    private OnTaskClick taskClickListener;

    public TaskRowAdapter(Context context, List<Task> tasks, OnStatusToggle listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
    }

    public void setOnTaskClickListener(OnTaskClick taskClickListener) {
        this.taskClickListener = taskClickListener;
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_task_row, parent, false);
        return new RowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        Task t = tasks.get(position);
        holder.title.setText(t.getTitle());
        holder.subtitle.setText("[" + t.getType() + "]  " + t.getDueDate() + "  " + t.getDueTime());
        holder.location.setText(t.getLocation());

        boolean done = "done".equalsIgnoreCase(t.getStatus());
        boolean missed = !done && DateUtils.isOverdue(t.getDueDate(), t.getDueTime());

        int bg = done ? R.drawable.bg_card_green : (missed ? R.drawable.bg_card_red : R.drawable.bg_card_blue);
        holder.container.setBackgroundResource(bg);
        holder.statusBtn.setText(done ? "DONE \u2713" : "MARK DONE");

        holder.statusBtn.setOnClickListener(v -> {
            SoundManager.playClick(context);
            if (listener != null) listener.onToggle(t);
        });

        holder.container.setOnClickListener(v -> {
            SoundManager.playClick(context);
            if (taskClickListener != null) taskClickListener.onClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class RowHolder extends RecyclerView.ViewHolder {
        View container;
        TextView title, subtitle, location, statusBtn;
        RowHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.row_container);
            title = itemView.findViewById(R.id.row_title);
            subtitle = itemView.findViewById(R.id.row_subtitle);
            location = itemView.findViewById(R.id.row_location);
            statusBtn = itemView.findViewById(R.id.row_status_btn);
        }
    }
}
