package com.example.studify;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateTaskActivity extends AppCompatActivity {

    SessionManager session;
    EditText inputTitle, inputDate, inputTime, inputLocation;
    RadioGroup typeGroup;
    Button btnSave;

    String baseUrl = "http://10.0.2.2/studify_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        inputTitle = findViewById(R.id.input_title);
        inputDate = findViewById(R.id.input_date);
        inputTime = findViewById(R.id.input_time);
        inputLocation = findViewById(R.id.input_location);
        typeGroup = findViewById(R.id.type_group);
        btnSave = findViewById(R.id.btn_save_task);

        BottomNavHelper.setup(this, BottomNavHelper.Tab.CREATE);
        HeaderHelper.setup(this, session);

        inputDate.setOnClickListener(v -> {
            SoundManager.playClick(this);
            showDatePicker();
        });
        inputTime.setOnClickListener(v -> {
            SoundManager.playClick(this);
            showTimePicker();
        });

        btnSave.setOnClickListener(v -> {
            SoundManager.playClick(this);
            saveTask();
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
            inputDate.setText(sdf.format(picked.getTime()).toUpperCase(Locale.ENGLISH));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            int hour12 = hourOfDay % 12;
            if (hour12 == 0) hour12 = 12;
            String ampm = hourOfDay < 12 ? "AM" : "PM";
            String timeStr = minute == 0 ? (hour12 + " " + ampm) : String.format(Locale.ENGLISH, "%d:%02d %s", hour12, minute, ampm);
            inputTime.setText(timeStr);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        dialog.show();
    }

    private void saveTask() {
        String title = inputTitle.getText().toString().trim();
        String date = inputDate.getText().toString().trim();
        String time = inputTime.getText().toString().trim();
        String location = inputLocation.getText().toString().trim();
        String type = typeGroup.getCheckedRadioButtonId() == R.id.radio_test ? "TEST" : "ASSIGNMENT";

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill in title, date and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = baseUrl + "add_task.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to add task.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(session.getUserId()));
                params.put("title", title);
                params.put("type", type);
                params.put("due_date", date);
                params.put("due_time", time);
                params.put("location", location.isEmpty() ? "-" : location);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}
