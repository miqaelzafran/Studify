package com.example.studify;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    SessionManager session;
    EditText searchInput;
    TextView tvEmpty;
    RecyclerView rvResults;
    List<Task> allTasks = new ArrayList<>();
    List<Task> filtered = new ArrayList<>();
    TaskRowAdapter adapter;

    String baseUrl = "http://10.0.2.2/studify_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        searchInput = findViewById(R.id.search_input);
        tvEmpty = findViewById(R.id.tv_search_empty);
        rvResults = findViewById(R.id.rv_search_results);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        BottomNavHelper.setup(this, BottomNavHelper.Tab.SEARCH);
        HeaderHelper.setup(this, session);

        adapter = new TaskRowAdapter(this, filtered, this::toggleStatus);
        adapter.setOnTaskClickListener(this::showTaskDetails);
        rvResults.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadTasks();
    }

    private void loadTasks() {
        String url = baseUrl + "get_tasks.php?user_id=" + session.getUserId();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    allTasks.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            allTasks.add(new Task(
                                    o.getInt("id"), o.getString("title"), o.getString("type"),
                                    o.getString("due_date"), o.getString("due_time"),
                                    o.getString("location"), o.optString("status", "assigned")));
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    filter(searchInput.getText().toString());
                },
                error -> Toast.makeText(this, "Could not load tasks.", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(this).add(request);
    }

    private void filter(String query) {
        filtered.clear();
        String q = query.trim().toLowerCase(Locale.ENGLISH);
        if (q.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            tvEmpty.setText("Type to search your tasks");
            rvResults.setVisibility(android.view.View.GONE);
        } else {
            for (Task t : allTasks) {
                if (t.getTitle().toLowerCase(Locale.ENGLISH).contains(q)
                        || t.getType().toLowerCase(Locale.ENGLISH).contains(q)
                        || t.getLocation().toLowerCase(Locale.ENGLISH).contains(q)) {
                    filtered.add(t);
                }
            }
            if (filtered.isEmpty()) {
                tvEmpty.setVisibility(android.view.View.VISIBLE);
                tvEmpty.setText("No matching tasks found");
                rvResults.setVisibility(android.view.View.GONE);
            } else {
                tvEmpty.setVisibility(android.view.View.GONE);
                rvResults.setVisibility(android.view.View.VISIBLE);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void toggleStatus(Task task) {
        boolean nowDone = "done".equalsIgnoreCase(task.getStatus());
        String newStatus = nowDone ? "assigned" : "done";
        String url = baseUrl + "update_task_status.php";
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                Request.Method.POST, url,
                response -> loadTasks(),
                error -> Toast.makeText(this, "Failed to update task.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
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
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                Request.Method.POST, url,
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
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("task_id", String.valueOf(task.getId()));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}
