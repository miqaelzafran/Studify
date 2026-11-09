package com.example.studify;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    EditText nameInput, emailInput, usernameInput, passwordInput, confirmPasswordInput, startDateInput;
    Button btnMinus, btnPlus, registerButton;
    TextView tvWeekCount, btnAddPhoto;
    ImageView ivPhoto;

    ActivityResultLauncher<String> photoPickerLauncher;
    String photoBase64 = ""; // required: filled in once the user picks a profile photo

    int weekCount = 14; // Default starting number for the weeks counter
    String url = "http://10.0.2.2/studify_api/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 1. Link variables to XML
        nameInput = findViewById(R.id.register_name);
        emailInput = findViewById(R.id.register_email);
        usernameInput = findViewById(R.id.register_username);
        passwordInput = findViewById(R.id.register_password);
        confirmPasswordInput = findViewById(R.id.register_confirm_password);
        startDateInput = findViewById(R.id.register_start_date);

        btnMinus = findViewById(R.id.btn_minus_week);
        btnPlus = findViewById(R.id.btn_plus_week);
        tvWeekCount = findViewById(R.id.tv_week_count);
        registerButton = findViewById(R.id.register_submit_button);
        ivPhoto = findViewById(R.id.iv_signup_photo);
        btnAddPhoto = findViewById(R.id.btn_signup_add_photo);

        // 1b. Profile Photo Picker (required at sign up)
        photoPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) loadPickedPhoto(uri);
        });

        View.OnClickListener openPicker = v -> {
            SoundManager.playClick(SignUpActivity.this);
            photoPickerLauncher.launch("image/*");
        };
        ivPhoto.setOnClickListener(openPicker);
        btnAddPhoto.setOnClickListener(openPicker);

        // 2. Interactive Counter Logic
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.playClick(SignUpActivity.this);
                if (weekCount > 1) {
                    weekCount--;
                    tvWeekCount.setText(String.valueOf(weekCount));
                }
            }
        });

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.playClick(SignUpActivity.this);
                if (weekCount < 52) {
                    weekCount++;
                    tvWeekCount.setText(String.valueOf(weekCount));
                }
            }
        });

        // 3. Native Calendar Popup Logic
        startDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        SignUpActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Format properly for MySQL (YYYY-MM-DD)
                                String formattedDate = year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth);
                                startDateInput.setText(formattedDate);
                            }
                        },
                        year, month, day);
                datePickerDialog.show();
            }
        });

        // 4. Submit Button Logic
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.playClick(SignUpActivity.this);
                final String name = nameInput.getText().toString().trim();
                final String email = emailInput.getText().toString().trim();
                final String user = usernameInput.getText().toString().trim();
                final String pass = passwordInput.getText().toString().trim();
                final String confirmPass = confirmPasswordInput.getText().toString().trim();
                final String startDate = startDateInput.getText().toString().trim();
                final String weeks = String.valueOf(weekCount);

                // Check for empty fields
                if (name.isEmpty() || email.isEmpty() || user.isEmpty() || pass.isEmpty() || startDate.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Profile photo is required at sign up
                if (photoBase64.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please add a profile photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if passwords match
                if (!pass.equals(confirmPass)) {
                    Toast.makeText(SignUpActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Send to XAMPP
                registerUser(name, email, user, pass, weeks, startDate, photoBase64);
            }
        });
    }

    /** Reads the picked image, downsizes/compresses it, and previews it as base64
     *  (same approach as ProfileActivity's photo picker). */
    private void loadPickedPhoto(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(input);
            if (input != null) input.close();
            if (original == null) {
                Toast.makeText(this, "Could not read that image.", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap resized = resizeForAvatar(original, 300);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            photoBase64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);

            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resized);
            drawable.setCircular(true);
            ivPhoto.setPadding(0, 0, 0, 0);
            ivPhoto.setImageDrawable(drawable);
            btnAddPhoto.setText("Change Profile Photo");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to process image.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeForAvatar(Bitmap source, int maxSize) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= maxSize && height <= maxSize) return source;
        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }

    // 5. Volley Network Request
    private void registerUser(final String name, final String email, final String username, final String password, final String weeks, final String startDate, final String photo) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("success")) {
                            Toast.makeText(SignUpActivity.this, "Account Created! You can now log in.", Toast.LENGTH_LONG).show();
                            finish(); // Closes the signup screen and returns to the Login Screen
                        } else if (response.trim().equals("username_taken")) {
                            Toast.makeText(SignUpActivity.this, "That username is already taken.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration failed.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SignUpActivity.this, "Network Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("username", username);
                params.put("password", password);
                params.put("semester_weeks", weeks);
                params.put("start_date", startDate);
                params.put("photo", photo);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}