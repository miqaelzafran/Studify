package com.example.studify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskListActivity extends AppCompatActivity {

    SessionManager session;
    RecyclerView rvDone, rvMissed, rvAssigned;
    List<Task> doneList = new ArrayList<>();
    List<Task> missedList = new ArrayList<>();
    List<Task> assignedList = new ArrayList<>();
    TaskRowAdapter doneAdapter, missedAdapter, assignedAdapter;

    String baseUrl = "http://10.0.2.2/studify_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        rvDone = findViewById(R.id.rv_done);
        rvMissed = findViewById(R.id.rv_missed);
        rvAssigned = findViewById(R.id.rv_assigned);

        rvDone.setLayoutManager(new LinearLayoutManager(this));
        rvMissed.setLayoutManager(new LinearLayoutManager(this));
        rvAssigned.setLayoutManager(new LinearLayoutManager(this));

        doneAdapter = new TaskRowAdapter(this, doneList, this::toggleStatus);
        missedAdapter = new TaskRowAdapter(this, missedList, this::toggleStatus);
        assignedAdapter = new TaskRowAdapter(this, assignedList, this::toggleStatus);

        doneAdapter.setOnTaskClickListener(this::showTaskDetails);
        missedAdapter.setOnTaskClickListener(this::showTaskDetails);
        assignedAdapter.setOnTaskClickListener(this::showTaskDetails);

        rvDone.setAdapter(doneAdapter);
        rvMissed.setAdapter(missedAdapter);
        rvAssigned.setAdapter(assignedAdapter);

        BottomNavHelper.setup(this, BottomNavHelper.Tab.TASK);
        HeaderHelper.setup(this, session);

        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HeaderHelper.setup(this, session);
        loadTasks();
    }

    private void loadTasks() {
        String url = baseUrl + "get_tasks.php?user_id=" + session.getUserId();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    doneList.clear();
                    missedList.clear();
                    assignedList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            Task t = new Task(
                                    o.getInt("id"), o.getString("title"), o.getString("type"),
                                    o.getString("due_date"), o.getString("due_time"),
                                    o.getString("location"), o.optString("status", "assigned"));

                            if ("done".equalsIgnoreCase(t.getStatus())) {
                                doneList.add(t);
                            } else if (DateUtils.isOverdue(t.getDueDate(), t.getDueTime())) {
                                missedList.add(t);
                            } else {
                                assignedList.add(t);
                            }
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    doneAdapter.notifyDataSetChanged();
                    missedAdapter.notifyDataSetChanged();
                    assignedAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Could not load tasks.", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(this).add(request);
    }

    private void toggleStatus(Task task) {
        boolean nowDone = "done".equalsIgnoreCase(task.getStatus());
        String newStatus = nowDone ? "assigned" : "done";
        if ("done".equals(newStatus)) {
            VibrationHelper.vibrateTaskDone(this);
        }
        String url = baseUrl + "update_task_status.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> loadTasks(),
                error -> Toast.makeText(this, "Failed to update task.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("task_id", String.valueOf(task.getId()));
                params.put("status", newStatus);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void showTaskDetails(Task task) {
        View view = getLayoutInflater().inflate(R.layout.dialog_task_details, null);
        TextView title = view.findViewById(R.id.detail_title);
        TextView type = view.findViewById(R.id.detail_type);
        TextView date = view.findViewById(R.id.detail_date);
        TextView time = view.findViewById(R.id.detail_time);
        TextView location = view.findViewById(R.id.detail_location);
        TextView status = view.findViewById(R.id.detail_status);
        Button deleteBtn = view.findViewById(R.id.detail_delete_btn);
        Button closeBtn = view.findViewById(R.id.detail_close_btn);

        title.setText(task.getTitle());
        type.setText("Type: " + task.getType());
        date.setText("Due Date: " + task.getDueDate());
        time.setText("Due Time: " + task.getDueTime());
        location.setText("Location: " + task.getLocation());
        status.setText("Status: " + task.getStatus());

        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        deleteBtn.setOnClickListener(v -> {
            SoundManager.playClick(this);
            dialog.dismiss();
            deleteTask(task);
        });

        dialog.show();
    }

    private void deleteTask(Task task) {
        String url = baseUrl + "delete_task.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(this, "Task deleted.", Toast.LENGTH_SHORT).show();
                        loadTasks();
                    } else {
                        Toast.makeText(this, "Failed to delete task.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("task_id", String.valueOf(task.getId()));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}
