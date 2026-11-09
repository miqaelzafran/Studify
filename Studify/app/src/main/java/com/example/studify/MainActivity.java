package com.example.studify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button loginButton;
    Button signupButton;
    SessionManager session;

    // CRITICAL: We use 10.0.2.2 instead of localhost because the Android Emulator
    // treats 'localhost' as the phone itself, not your computer running XAMPP!
    String url = "http://10.0.2.2/studify_api/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.playClick(MainActivity.this);
                String user = usernameInput.getText().toString().trim();
                String pass = passwordInput.getText().toString().trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(user, pass);
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.playClick(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(final String username, final String password) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                rawResponse -> {
                    try {
                        JSONObject response = new JSONObject(rawResponse);
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            JSONObject u = response.getJSONObject("user");
                            session.saveUserSession(
                                    u.getInt("id"),
                                    u.getString("name"),
                                    u.getString("username"),
                                    u.getString("email"),
                                    u.getInt("semester_weeks"),
                                    u.getString("start_date"),
                                    u.optString("photo", "")
                            );
                            Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Unexpected server response.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Error connecting to database: " + error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
