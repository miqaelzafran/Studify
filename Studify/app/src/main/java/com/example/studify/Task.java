package com.example.studify;

/** Simple POJO representing a row from the `tasks` table. */
public class Task {
    private int id;
    private String title;
    private String type;      // "ASSIGNMENT" or "TEST"
    private String dueDate;   // e.g. "23 JUNE 2026"
    private String dueTime;   // e.g. "4 PM"
    private String location;
    private String status;    // "assigned", "done", "missed"

    public Task(int id, String title, String type, String dueDate, String dueTime, String location, String status) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.location = location;
        this.status = status;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getDueDate() { return dueDate; }
    public String getDueTime() { return dueTime; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
