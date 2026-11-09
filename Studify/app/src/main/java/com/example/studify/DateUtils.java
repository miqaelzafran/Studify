package com.example.studify;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Helpers for parsing Studify's "23 JUNE 2026" / "4 PM" date-time strings. */
public class DateUtils {

    /** Combines a "23 JUNE 2026" date string with a "4 PM" time string into a Date. */
    public static Date parseDueDateTime(String dueDate, String dueTime) {
        if (dueDate == null || dueDate.isEmpty()) return null;
        String combined = dueDate.trim() + " " + (dueTime == null ? "12 AM" : dueTime.trim());
        String[] patterns = {"dd MMMM yyyy h a", "dd MMM yyyy h a", "dd MMMM yyyy hh:mm a"};
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.ENGLISH);
                sdf.setLenient(true);
                return sdf.parse(combined);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    /** Returns true if the given due date/time is before now (i.e. the deadline passed). */
    public static boolean isOverdue(String dueDate, String dueTime) {
        Date due = parseDueDateTime(dueDate, dueTime);
        if (due == null) return false;
        return due.before(new Date());
    }

    /** Returns true if the deadline is within the next 48 hours (used for "high priority"). */
    public static boolean isHighPriority(String dueDate, String dueTime) {
        Date due = parseDueDateTime(dueDate, dueTime);
        if (due == null) return false;
        long diff = due.getTime() - System.currentTimeMillis();
        long hours = diff / (1000 * 60 * 60);
        return hours >= 0 && hours <= 48;
    }

    /**
     * Calculates which week of the semester "today" falls in.
     *
     * The chosen start date is Day 1 of Week 1 of the semester, and weeks are
     * counted in rolling 7-day blocks from that exact calendar date (e.g. if the
     * semester start date is a Wednesday, Week 2 begins on the following Wednesday
     * - it is not re-aligned to a Monday). Both dates are normalized to noon
     * before comparing so a difference in "what time it currently is" (or a
     * daylight-saving shift between the two dates) can never shift the calendar-day
     * count by one and misplace the boundary between two weeks.
     */
    public static int getCurrentWeek(String startDateStr, int totalWeeks) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            sdf.setLenient(true);
            Date start = sdf.parse(startDateStr);
            if (start == null) return 1;

            long startDayMillis = toNoonMillis(start);
            long todayDayMillis = toNoonMillis(new Date());

            long diffDays = (todayDayMillis - startDayMillis) / (1000L * 60 * 60 * 24);
            int week = (int) (diffDays / 7) + 1;
            if (week < 1) week = 1;
            if (week > totalWeeks) week = totalWeeks;
            return week;
        } catch (ParseException e) {
            return 1;
        }
    }

    /** Returns the given date's calendar day pinned to 12:00 noon, so day-based
     *  comparisons are unaffected by daylight-saving shifts or the exact time of day. */
    private static long toNoonMillis(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 12);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static String todayFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        return sdf.format(new Date()).toUpperCase(Locale.ENGLISH);
    }

    /** Returns today's day of the week, e.g. "MONDAY", shown above the date in the header. */
    public static String todayDayFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        return sdf.format(new Date()).toUpperCase(Locale.ENGLISH);
    }
}
