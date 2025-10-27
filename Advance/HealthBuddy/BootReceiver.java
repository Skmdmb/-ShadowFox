package com.example.healthbuddy025;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Restart step counting service after reboot
            Intent serviceIntent = new Intent(context, StepCounterService.class);
            context.startService(serviceIntent);
        }
    }
}
