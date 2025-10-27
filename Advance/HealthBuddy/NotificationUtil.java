package com.example.healthbuddy025;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationUtil {
    public static void createNotificationChannel(Context ctx, String channelId, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("HealthBuddy notifications");
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
}
