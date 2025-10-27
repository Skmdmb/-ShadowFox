package com.example.healthbuddy025;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.healthbuddy025.databinding.ActivityMainBinding;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DataManager dataManager;
    private boolean isDarkTheme = false;

    private final BroadcastReceiver stepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long steps = intent.getLongExtra("steps", 0);
            binding.stepCountText.setText("Today's Steps: " + steps);

            // update inline goal achieved text
            updateGoalAchievedVisibility((int) steps);
        }
    };

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean activityAllowed = result.getOrDefault(Manifest.permission.ACTIVITY_RECOGNITION, false);
                Boolean bodyAllowed = result.getOrDefault(Manifest.permission.BODY_SENSORS, false);

                if (activityAllowed || bodyAllowed || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    startTrackingServiceIfNeeded();
                } else {
                    Toast.makeText(this, "Permission required for step tracking", Toast.LENGTH_LONG).show();
                }
            });

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = new DataManager(this);

        // populate UI from saved values
        binding.stepCountText.setText("Today's Steps: " + dataManager.getTodaySteps());
        binding.goalText.setText("Goal: " + dataManager.getGoal());
        updateGoalAchievedVisibility(dataManager.getTodaySteps());

        // hooks
        binding.setGoalBtn.setOnClickListener(v -> showGoalDialog());
        binding.viewStatsBtn.setOnClickListener(v -> {
            // open stats (StatsActivity will reload latest history)
            startActivity(new Intent(MainActivity.this, StatsActivity.class));
        });
        binding.switchThemeBtn.setOnClickListener(v -> switchTheme());

        // schedule daily reset
        scheduleDailyReset();

        // register receiver safely (API 33+ requires explicit exported flag)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stepReceiver, new IntentFilter("LIVE_STEP_UPDATE"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(stepReceiver, new IntentFilter("LIVE_STEP_UPDATE"));
        }

        // ensure permissions & start service
        ensurePermissionsAndStartService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh UI from storage (in case steps changed while paused)
        int today = dataManager.getTodaySteps();
        binding.stepCountText.setText("Today's Steps: " + today);
        binding.goalText.setText("Goal: " + dataManager.getGoal());
        updateGoalAchievedVisibility(today);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(stepReceiver); } catch (Exception ignored) {}
    }

    private void updateGoalAchievedVisibility(int todaySteps) {
        int goal = dataManager.getGoal();
        if (goal > 0 && todaySteps >= goal) {
            binding.goalAchievedText.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.goalAchievedText.setVisibility(android.view.View.GONE);
        }
    }

    private void showGoalDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter goal (e.g. 8000)");

        new AlertDialog.Builder(this)
                .setTitle("Set Step Goal")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (!value.isEmpty()) {
                        dataManager.setGoal(Integer.parseInt(value));
                        binding.goalText.setText("Goal: " + dataManager.getGoal());
                        // update inline result in case already achieved
                        updateGoalAchievedVisibility(dataManager.getTodaySteps());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void switchTheme() {
        if (isDarkTheme) {
            binding.getRoot().setBackgroundColor(0xFFFFFFFF); // Light
            binding.stepCountText.setTextColor(0xFF000000);
            binding.goalText.setTextColor(0xFF000000);
        } else {
            binding.getRoot().setBackgroundColor(0xFF000000); // Dark
            binding.stepCountText.setTextColor(0xFFFFFFFF);
            binding.goalText.setTextColor(0xFFFFFFFF);
        }
        isDarkTheme = !isDarkTheme;
    }

    private void scheduleDailyReset() {
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, ResetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    midnight.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    // ---------- Permissions & service control ----------

    private void ensurePermissionsAndStartService() {
        boolean needActivityPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        boolean hasActivity = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
        boolean hasBody = ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED;

        if (needActivityPermission && !(hasActivity || hasBody)) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.BODY_SENSORS
            });
            return;
        }

        startTrackingServiceIfNeeded();
    }

    private void startTrackingServiceIfNeeded() {
        Intent svc = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, svc);
        } else {
            startService(svc);
        }
        Toast.makeText(this, "Step tracking started", Toast.LENGTH_SHORT).show();
    }
}
