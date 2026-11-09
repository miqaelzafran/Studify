package com.example.studify;

import android.content.Context;
import android.content.SharedPreferences;

/** Wraps SharedPreferences to persist the logged-in user's session and app preferences. */
public class SessionManager {

    private static final String PREF_NAME = "studify_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_SEMESTER_WEEKS = "semester_weeks";
    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_PHOTO = "photo";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(int userId, String name, String username, String email,
                                 int semesterWeeks, String startDate, String photo) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putInt(KEY_SEMESTER_WEEKS, semesterWeeks);
        editor.putString(KEY_START_DATE, startDate);
        editor.putString(KEY_PHOTO, photo == null ? "" : photo);
        editor.apply();
    }

    public int getUserId() { return prefs.getInt(KEY_USER_ID, -1); }
    public String getName() { return prefs.getString(KEY_NAME, "Student"); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public int getSemesterWeeks() { return prefs.getInt(KEY_SEMESTER_WEEKS, 14); }
    public String getStartDate() { return prefs.getString(KEY_START_DATE, ""); }
    public String getPhoto() { return prefs.getString(KEY_PHOTO, ""); }

    public void setName(String name) { prefs.edit().putString(KEY_NAME, name).apply(); }
    public void setUsername(String username) { prefs.edit().putString(KEY_USERNAME, username).apply(); }
    public void setPhoto(String photoBase64) { prefs.edit().putString(KEY_PHOTO, photoBase64 == null ? "" : photoBase64).apply(); }

    public boolean isSoundEnabled() { return prefs.getBoolean(KEY_SOUND_ENABLED, true); }
    public void setSoundEnabled(boolean enabled) { prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply(); }

    public boolean isLoggedIn() { return getUserId() != -1; }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
