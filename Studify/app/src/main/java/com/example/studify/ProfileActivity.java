package com.example.studify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    SessionManager session;
    EditText inputUsername, inputName, inputEmail, inputNewPassword;
    Button btnEditSave, btnChangePassword, btnChangePhoto;
    ImageView ivProfilePhoto;
    Switch switchSound;
    LinearLayout btnLogout, btnDeleteAccount;
    boolean isEditing = false;

    ActivityResultLauncher<String> photoPickerLauncher;

    String baseUrl = "http://10.0.2.2/studify_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        inputUsername = findViewById(R.id.input_username);
        inputName = findViewById(R.id.input_name);
        inputEmail = findViewById(R.id.input_email);
        inputNewPassword = findViewById(R.id.input_new_password);
        btnEditSave = findViewById(R.id.btn_edit_save);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        switchSound = findViewById(R.id.switch_sound);
        btnLogout = findViewById(R.id.btn_logout);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        BottomNavHelper.setup(this, BottomNavHelper.Tab.ME);
        HeaderHelper.setup(this, session);

        inputUsername.setText(session.getUsername());
        inputName.setText(session.getName());
        inputEmail.setText(session.getEmail());
        switchSound.setChecked(session.isSoundEnabled());

        HeaderHelper.applyAvatar(this, ivProfilePhoto, session);

        photoPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) uploadPhoto(uri);
        });

        btnChangePhoto.setOnClickListener(v -> {
            SoundManager.playClick(this);
            photoPickerLauncher.launch("image/*");
        });

        btnEditSave.setOnClickListener(v -> {
            SoundManager.playClick(this);
            if (!isEditing) {
                enterEditMode();
            } else {
                saveProfile();
            }
        });

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            session.setSoundEnabled(isChecked);
            // play toggle sound only if turning ON (or was on before turning off)
            SoundManager.playToggle(this, buttonView);
        });

        btnChangePassword.setOnClickListener(v -> {
            SoundManager.playClick(this);
            changePassword();
        });

        btnLogout.setOnClickListener(v -> {
            SoundManager.playClick(this);
            session.logout();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            SoundManager.playClick(this);
            confirmDeleteAccount();
        });
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all of your tasks. This action cannot be undone. Are you sure?")
                .setPositiveButton("DELETE", (dialog, which) -> deleteAccount())
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void deleteAccount() {
        String url = baseUrl + "delete_account.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(this, "Account deleted.", Toast.LENGTH_SHORT).show();
                        session.logout();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(session.getUserId()));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void enterEditMode() {
        isEditing = true;
        inputName.setEnabled(true);
        inputUsername.setEnabled(true);
        btnEditSave.setText("SAVE CHANGES");
    }

    private void saveProfile() {
        final String newName = inputName.getText().toString().trim();
        final String newUsername = inputUsername.getText().toString().trim();

        if (newName.isEmpty() || newUsername.isEmpty()) {
            Toast.makeText(this, "Name and username cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = baseUrl + "update_profile.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        session.setName(newName);
                        session.setUsername(newUsername);
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        isEditing = false;
                        inputName.setEnabled(false);
                        inputUsername.setEnabled(false);
                        btnEditSave.setText("EDIT PROFILE");
                    } else if (response.trim().equals("username_taken")) {
                        Toast.makeText(this, "That username is already taken.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(session.getUserId()));
                params.put("name", newName);
                params.put("username", newUsername);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    /** Reads the picked image, downsizes/compresses it, and uploads it as base64. */
    private void uploadPhoto(Uri uri) {
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
            String base64Photo = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);

            session.setPhoto(base64Photo); // preview instantly without waiting for the server
            HeaderHelper.applyAvatar(this, ivProfilePhoto, session);
            HeaderHelper.setup(this, session); // also refresh the header avatar at the top of this page
            savePhoto(base64Photo);
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

    private void savePhoto(final String base64Photo) {
        String url = baseUrl + "update_profile.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save photo.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(session.getUserId()));
                params.put("name", session.getName());
                params.put("username", session.getUsername());
                params.put("photo", base64Photo);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void changePassword() {
        final String newPassword = inputNewPassword.getText().toString().trim();
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Enter a new password first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = baseUrl + "change_password.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(this, "Password updated!", Toast.LENGTH_SHORT).show();
                        inputNewPassword.setText("");
                    } else {
                        Toast.makeText(this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(session.getUserId()));
                params.put("password", newPassword);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}
