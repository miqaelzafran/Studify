package com.example.studify;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.util.Calendar;

/**
 * Wires up the shared header_common.xml include (day, date, week, greeting,
 * avatar, progress bar) so every screen shows the same, live user info.
 * Include <include layout="@layout/header_common"/> in an activity's layout,
 * then call HeaderHelper.setup(this, session) after setContentView().
 */
public class HeaderHelper {

    public static void setup(final Activity activity, final SessionManager session) {
        TextView tvDay = activity.findViewById(R.id.tv_day);
        TextView tvDate = activity.findViewById(R.id.tv_today_date);
        TextView tvWeek = activity.findViewById(R.id.tv_week);
        TextView tvGreeting = activity.findViewById(R.id.tv_greeting);
        TextView tvProgressLabel = activity.findViewById(R.id.tv_progress_label);
        final View progressFill = activity.findViewById(R.id.progress_fill);
        ImageView ivAvatar = activity.findViewById(R.id.iv_avatar);

        if (tvDay == null || tvDate == null || tvWeek == null) return; // header not on this screen

        tvDay.setText(DateUtils.todayDayFormatted());
        tvDate.setText(DateUtils.todayFormatted());

        int week = DateUtils.getCurrentWeek(session.getStartDate(), session.getSemesterWeeks());
        int totalWeeks = session.getSemesterWeeks();
        tvWeek.setText("WEEK " + week);
        if (tvProgressLabel != null) {
            tvProgressLabel.setText("Week " + week + " of " + totalWeeks);
        }

        int percent = totalWeeks > 0 ? Math.min(100, (int) ((week * 100.0) / totalWeeks)) : 0;
        if (progressFill != null) {
            progressFill.post(() -> {
                ViewGroup parent = (ViewGroup) progressFill.getParent();
                int maxWidth = parent.getWidth();
                ViewGroup.LayoutParams lp = progressFill.getLayoutParams();
                lp.width = Math.max(20, (int) (maxWidth * (percent / 100.0)));
                progressFill.setLayoutParams(lp);
            });
        }

        if (tvGreeting != null) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            String greetingWord = hour < 12 ? "Good Morning" : (hour < 18 ? "Good Afternoon" : "Good Evening");
            tvGreeting.setText(greetingWord + ",\n" + session.getName() + "!");
        }

        if (ivAvatar != null) {
            applyAvatar(activity, ivAvatar, session);
            ivAvatar.setOnClickListener(v -> {
                SoundManager.playClick(activity);
                if (activity.getClass().equals(ProfileActivity.class)) return; // already on profile
                activity.startActivity(new Intent(activity, ProfileActivity.class));
            });
        }
    }

    /** Decodes the user's stored profile photo (base64) onto any avatar ImageView,
     *  falling back to the default person icon if no photo has been set yet. */
    public static void applyAvatar(Activity activity, ImageView imageView, SessionManager session) {
        String photoBase64 = session.getPhoto();
        float density = activity.getResources().getDisplayMetrics().density;

        if (photoBase64 != null && !photoBase64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(photoBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(activity.getResources(), bitmap);
                    drawable.setCircular(true);
                    imageView.setPadding(0, 0, 0, 0);
                    imageView.setImageDrawable(drawable);
                    return;
                }
            } catch (Exception ignored) { }
        }

        int paddingPx = (int) (10 * density);
        imageView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        imageView.setImageResource(R.drawable.ic_person);
    }
}
