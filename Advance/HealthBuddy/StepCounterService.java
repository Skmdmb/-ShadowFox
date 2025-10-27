package com.example.healthbuddy025;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor, accelSensor;
    private DataManager dataManager;
    private static final String CHANNEL_ID = "step_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        dataManager = new DataManager(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        startForegroundServiceSafe();

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (accelSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void startForegroundServiceSafe() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Counter",
                    NotificationManager.IMPORTANCE_LOW
            );
            nm.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("HealthBuddy Running")
                .setContentText("Tracking your steps silently...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long steps = dataManager.addStep(); // ✅ WORKS NOW

        Intent i = new Intent("LIVE_STEP_UPDATE");
        i.putExtra("steps", steps);
        sendBroadcast(i);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
