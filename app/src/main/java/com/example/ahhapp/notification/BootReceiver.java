package com.example.ahhapp.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;

import com.example.ahhapp.utils.ReminderRescheduler;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "裝置已重啟，開始重新排程提醒");

            // 從 SharedPreferences 取得 token
            SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("token", null);

            if (token != null) {
                ReminderRescheduler.rescheduleAllReminders(context, token);
            } else {
                Log.w("BootReceiver", "無法重新排程提醒，token 為 null");
            }
        }
    }
}