package com.example.todolistapp;

public class Task {
    private String title;
    private String priority;
    private boolean completed;

    public Task(String title, String priority, boolean completed) {
        this.title = title;
        this.priority = priority;
        this.completed = completed;
    }

    public String getTitle() { return title; }
    public String getPriority() { return priority; }
    public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) { this.completed = completed; }
}
