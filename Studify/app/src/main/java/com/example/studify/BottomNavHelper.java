package com.example.studify;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Wires up the shared bottom_nav.xml include for every screen and highlights the active tab. */
public class BottomNavHelper {

    public enum Tab { HOME, SEARCH, CREATE, TASK, ME }

    public static void setup(final Activity activity, Tab active) {
        LinearLayout navHome = activity.findViewById(R.id.nav_home);
        LinearLayout navSearch = activity.findViewById(R.id.nav_search);
        LinearLayout navCreate = activity.findViewById(R.id.nav_create);
        LinearLayout navTask = activity.findViewById(R.id.nav_task);
        LinearLayout navMe = activity.findViewById(R.id.nav_me);

        navHome.setOnClickListener(v -> {
            SoundManager.playClick(activity);
            navigate(activity, DashboardActivity.class);
        });
        navSearch.setOnClickListener(v -> {
            SoundManager.playClick(activity);
            navigate(activity, SearchActivity.class);
        });
        navCreate.setOnClickListener(v -> {
            SoundManager.playClick(activity);
            navigate(activity, CreateTaskActivity.class);
        });
        navTask.setOnClickListener(v -> {
            SoundManager.playClick(activity);
            navigate(activity, TaskListActivity.class);
        });
        navMe.setOnClickListener(v -> {
            SoundManager.playClick(activity);
            navigate(activity, ProfileActivity.class);
        });

        highlight(activity, R.id.nav_home_text, active == Tab.HOME);
        highlight(activity, R.id.nav_search_text, active == Tab.SEARCH);
        highlight(activity, R.id.nav_create_text, active == Tab.CREATE);
        highlight(activity, R.id.nav_task_text, active == Tab.TASK);
        highlight(activity, R.id.nav_me_text, active == Tab.ME);
    }

    private static void highlight(Activity activity, int textId, boolean isActive) {
        TextView tv = activity.findViewById(textId);
        if (tv != null) {
            tv.setTextColor(activity.getResources().getColor(
                    isActive ? R.color.spotify_green : R.color.nav_inactive));
        }
    }

    private static void navigate(Activity activity, Class<?> target) {
        if (activity.getClass().equals(target)) return; // already on this screen
        Intent intent = new Intent(activity, target);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }
}
