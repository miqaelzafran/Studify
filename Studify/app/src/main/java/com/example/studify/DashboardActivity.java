package com.example.studify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    SessionManager session;
    TextView tvEmptyTasks;
    android.widget.LinearLayout priorityCard;
    TextView priorityLabel, priorityTitle, priorityDatetime, priorityLocation, priorityDoneBtn;
    android.widget.LinearLayout sectionTest, sectionAssignment;
    RecyclerView rvTest, rvAssignment;
    SwipeRefreshLayout swipeRefresh;

    List<Task> allTasks = new ArrayList<>();
    List<Task> testTasks = new ArrayList<>();
    List<Task> assignmentTasks = new ArrayList<>();
    Task currentPriorityTask = null;

    String baseUrl = "http://10.0.2.2/studify_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        tvEmptyTasks = findViewById(R.id.tv_empty_tasks);
        priorityCard = findViewById(R.id.priority_card);
        priorityLabel = findViewById(R.id.priority_label);
        priorityTitle = findViewById(R.id.priority_title);
        priorityDatetime = findViewById(R.id.priority_datetime);
        priorityLocation = findViewById(R.id.priority_location);
        priorityDoneBtn = findViewById(R.id.priority_done_btn);
        sectionTest = findViewById(R.id.section_test);
        sectionAssignment = findViewById(R.id.section_assignment);
        rvTest = findViewById(R.id.rv_tasks_test);
        rvAssignment = findViewById(R.id.rv_tasks_assignment);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        rvTest.setLayoutManager(new GridLayoutManager(this, 2));
        rvAssignment.setLayoutManager(new GridLayoutManager(this, 2));

        BottomNavHelper.setup(this, BottomNavHelper.Tab.HOME);

        swipeRefresh.setOnRefreshListener(this::loadTasks);

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
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    swipeRefresh.setRefreshing(false);
                    allTasks.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            allTasks.add(new Task(
                                    o.getInt("id"),
                                    o.getString("title"),
                                    o.getString("type"),
                                    o.getString("due_date"),
                                    o.getString("due_time"),
                                    o.getString("location"),
                                    o.optString("status", "assigned")
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    renderTasks();
                },
                error -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, "Could not load tasks. Check server connection.", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }

    private void renderTasks() {
        tvEmptyTasks.setVisibility(allTasks.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

        currentPriorityTask = null;
        boolean priorityIsMissed = false;
        Task earliestOverdue = null;
        java.util.Date earliestOverdueDate = null;
        Task earliestUpcoming = null;
        java.util.Date earliestUpcomingDate = null;

        for (Task t : allTasks) {
            if ("done".equalsIgnoreCase(t.getStatus())) continue;
            java.util.Date due = DateUtils.parseDueDateTime(t.getDueDate(), t.getDueTime());
            if (due == null) continue;

            if (DateUtils.isOverdue(t.getDueDate(), t.getDueTime())) {
                // Missed tasks always outrank upcoming ones; among missed tasks,
                // the one that was due earliest (longest overdue) wins.
                if (earliestOverdue == null || due.before(earliestOverdueDate)) {
                    earliestOverdue = t;
                    earliestOverdueDate = due;
                }
            } else {
                if (earliestUpcoming == null || due.before(earliestUpcomingDate)) {
                    earliestUpcoming = t;
                    earliestUpcomingDate = due;
                }
            }
        }

        if (earliestOverdue != null) {
            currentPriorityTask = earliestOverdue;
            priorityIsMissed = true;
        } else if (earliestUpcoming != null) {
            currentPriorityTask = earliestUpcoming;
        }

        if (currentPriorityTask != null) {
            priorityCard.setVisibility(android.view.View.VISIBLE);
            priorityLabel.setText(priorityIsMissed ? "MISSED ASSIGNMENT !!!" : "HIGH PRIORITY !!!");
            priorityTitle.setText(currentPriorityTask.getTitle());
            priorityDatetime.setText(currentPriorityTask.getDueDate() + " \u00b7 " + currentPriorityTask.getDueTime());
            priorityLocation.setText(currentPriorityTask.getLocation());
            final Task pTask = currentPriorityTask;
            priorityDoneBtn.setOnClickListener(v -> {
                SoundManager.playClick(this);
                VibrationHelper.vibrateTaskDone(this);
                updateStatus(pTask, "done");
            });
        } else {
            priorityCard.setVisibility(android.view.View.GONE);
        }

        testTasks.clear();
        assignmentTasks.clear();
        for (Task t : allTasks) {
            if ("TEST".equalsIgnoreCase(t.getType())) {
                testTasks.add(t);
            } else {
                assignmentTasks.add(t);
            }
        }

        sectionTest.setVisibility(testTasks.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
        sectionAssignment.setVisibility(assignmentTasks.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);

        TaskGridAdapter.OnTaskClick openTaskList = task ->
                startActivity(new Intent(this, TaskListActivity.class));

        rvTest.setAdapter(new TaskGridAdapter(this, testTasks, openTaskList));
        rvAssignment.setAdapter(new TaskGridAdapter(this, assignmentTasks, openTaskList));
    }

    private void updateStatus(Task task, String newStatus) {
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
}
