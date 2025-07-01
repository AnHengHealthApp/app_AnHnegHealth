package com.example.ahhapp.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.ahhapp.R;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // 從 SharedPreferences 檢查是否啟用通知
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true); // 預設為 true

        if (!notificationsEnabled) {
            // 若使用者已關閉通知，則不做任何事
            return;
        }

        // 取得藥品名稱
        String medicineName = intent.getStringExtra("medicine_name");

        // 建立通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "health_channel")
                .setSmallIcon(R.drawable.ic_pills)
                .setContentTitle("用藥提醒")
                .setContentText("該吃藥囉：" + medicineName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        // 發送通知前權限檢查
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}