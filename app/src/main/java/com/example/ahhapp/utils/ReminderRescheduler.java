package com.example.ahhapp.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.ahhapp.data.modle.MedicationReminder;
import com.example.ahhapp.data.modle.MedicationReminderResponse;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.notification.ReminderReceiver;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReminderRescheduler {

    public static void rescheduleAllReminders(Context context, String token) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<MedicationReminderResponse> call = apiService.getAllMedication("Bearer " + token);

        call.enqueue(new Callback<MedicationReminderResponse>() {
            @Override
            public void onResponse(Call<MedicationReminderResponse> call, Response<MedicationReminderResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (MedicationReminder reminder : response.body().data) {
                        scheduleReminder(context, reminder);
                    }
                } else {
                    Log.e("ReminderRescheduler", "重新排程失敗: API 回傳異常");
                }
            }

            @Override
            public void onFailure(Call<MedicationReminderResponse> call, Throwable t) {
                Log.e("ReminderRescheduler", "重新排程錯誤：" + t.getMessage());
            }
        });
    }

    private static void scheduleReminder(Context context, MedicationReminder reminder) {
        String reminderTime = reminder.getReminder_time(); // 格式應為 HH:mm:ss
        String medicineName = reminder.getMedication_name();

        if (reminderTime == null || !reminderTime.matches("^\\d{2}:\\d{2}:\\d{2}$")) {
            Log.w("ReminderRescheduler", "時間格式錯誤：" + reminderTime);
            return;
        }

        try {
            String[] parts = reminderTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            long triggerTime = calendar.getTimeInMillis();
            if (triggerTime <= System.currentTimeMillis()) {
                triggerTime += 24 * 60 * 60 * 1000; // 延後一天
            }

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("medicine_name", medicineName);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) System.currentTimeMillis(),  // 或使用 reminder.getId() 若有唯一 ID
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(context, "請手動開啟精確鬧鐘權限", Toast.LENGTH_SHORT).show();
                    Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(settingsIntent);
                    return;
                }

                try {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "設置提醒失敗：缺少權限", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            Log.e("ReminderRescheduler", "排程錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }
}