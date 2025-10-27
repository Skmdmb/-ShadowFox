package com.example.healthbuddy025;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ResetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DataManager dm = new DataManager(context);
        dm.resetDailyAndShift(0);  // assume 0 steps if app not open

        // NOTE: No UI/Toast here, BroadcastReceiver runs in background
        // Next daily reset will auto-happen tomorrow when triggered again.
    }
}
