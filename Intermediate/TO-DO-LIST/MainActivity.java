package com.example.todolistapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskDeleteListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private Button addTaskBtn, sortBtn;
    private SharedPreferences prefs;
    private static final String PREF_KEY = "tasks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        addTaskBtn = findViewById(R.id.addTaskBtn);
        sortBtn = findViewById(R.id.sortBtn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        prefs = getSharedPreferences("ToDoList", MODE_PRIVATE);

        loadTasks();

        adapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(adapter);

        addTaskBtn.setOnClickListener(v -> showAddTaskDialog());
        sortBtn.setOnClickListener(v -> sortTasks());
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText taskInput = dialogView.findViewById(R.id.editTask);
        Spinner prioritySpinner = dialogView.findViewById(R.id.prioritySpinner);

        String[] priorities = {"High", "Medium", "Low"};
        prioritySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities));

        new AlertDialog.Builder(this)
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String taskText = taskInput.getText().toString().trim();
                    String priority = prioritySpinner.getSelectedItem().toString();
                    if (!taskText.isEmpty()) {
                        taskList.add(new Task(taskText, priority, false));
                        adapter.notifyItemInserted(taskList.size() - 1);
                        saveTasks();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sortTasks() {
        Collections.sort(taskList, Comparator.comparing(Task::getPriority));
        adapter.notifyDataSetChanged();
        saveTasks();
    }

    private void saveTasks() {
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(PREF_KEY, gson.toJson(taskList));
        editor.apply();
    }

    private void loadTasks() {
        Gson gson = new Gson();
        String json = prefs.getString(PREF_KEY, null);
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        List<Task> savedList = gson.fromJson(json, type);
        if (savedList != null) {
            taskList = savedList;
        }
    }

    @Override
    public void onDelete(int position) {
        taskList.remove(position);
        adapter.notifyItemRemoved(position);
        saveTasks();
    }
}
