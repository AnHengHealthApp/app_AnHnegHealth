package com.example.ahhapp.ui.medication;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.provider.Settings;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ahhapp.R;
import com.example.ahhapp.adapter.MedicineReminderAdapter;
import com.example.ahhapp.data.modle.MedicationReminder;
import com.example.ahhapp.data.modle.MedicationReminderRequest;
import com.example.ahhapp.data.modle.MedicationReminderResponse;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.notification.ReminderReceiver;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.example.ahhapp.utils.UserProfileManager;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MedicationReminderFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {
    private RecyclerView recyclerView;
    private MedicineReminderAdapter adapter;
    private List<MedicationReminder> medicineList = new ArrayList<>();
    private EditText etMedicineName, etMedicineTime, etMedicineNote, etReminderTime;

    private TextView tvUsername;
    private ImageView ivUserPhoto;

    // 暫存
    private Bitmap cachedAvatar = null;
    private String cachedUsername = null;

    // 空建構子
    public MedicationReminderFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_medication_reminder, container, false);

        //初始化頭像列 UI 元件
        tvUsername = view.findViewById(R.id.tvUsername);
        ivUserPhoto = view.findViewById(R.id.ivUserPhoto);

        // 如果有快取資料先顯示
        if (cachedUsername != null) tvUsername.setText(cachedUsername);
        if (cachedAvatar != null) {
            ivUserPhoto.setImageBitmap(cachedAvatar);
        } else {
            ivUserPhoto.setImageResource(R.drawable.ic_user_photo);
        }

        // 載入使用者資料
        loadUserProfile();

        //先處理權限問題
        checkExactAlarmPermission();

        //頭像按鈕邏輯
        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this); // 傳入當前 Fragment 作為 listener
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });

        // 綁定輸入欄位
        etMedicineName = view.findViewById(R.id.etMedicineName);
        etMedicineTime = view.findViewById(R.id.etMedicineTime);
        etMedicineNote = view.findViewById(R.id.etMedicineNote);
        etReminderTime = view.findViewById(R.id.etReminderTime);

        // 設定 RecyclerView 顯示用藥提醒列表
        recyclerView = view.findViewById(R.id.rvReminderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicineReminderAdapter(medicineList);
        recyclerView.setAdapter(adapter);

        // 設定刪除 callback
        adapter.setOnDeleteClickListener(reminderId -> deleteMedicationReminder(reminderId));

        // 初始載入提醒資料
        fetchMedicationReminders();

        // 綁定返回鍵
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // 綁定新增按鈕點擊事件
        Button btnAddMedicine = view.findViewById(R.id.btnAddReminder);
        btnAddMedicine.setOnClickListener(v -> addMedicationReminder());

        return view;
    }

    //取得用藥提醒
    private void fetchMedicationReminders() {
        String token = getToken();
        if (token == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<MedicationReminderResponse> call = apiService.getAllMedication("Bearer " + token);
        call.enqueue(new Callback<MedicationReminderResponse>() {
            @Override
            public void onResponse(Call<MedicationReminderResponse> call, Response<MedicationReminderResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    medicineList = response.body().data;
                    adapter.setData(medicineList);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "取得提醒失敗", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MedicationReminderResponse> call, Throwable t) {
                Toast.makeText(getContext(), "連線錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 新增提醒
    private void addMedicationReminder() {
        String name = etMedicineName.getText().toString().trim();
        String time = etMedicineTime.getText().toString().trim();
        String note = etMedicineNote.getText().toString().trim();
        String inputReminderTime = etReminderTime.getText().toString().trim();

        if (name.isEmpty() || time.isEmpty() || note.isEmpty() || inputReminderTime.isEmpty()) {
            Toast.makeText(getContext(), "請完整填寫所有欄位", Toast.LENGTH_SHORT).show();
            return;
        }

        // 檢查格式是否為 HH:mm，若不是則提示錯誤
        if (!inputReminderTime.matches("^\\d{2}:\\d{2}$")) {
            Toast.makeText(getContext(), "提醒時間格式錯誤，請使用 HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }
        // 補上秒數，轉為 HH:mm:ss 傳給 API
        String formattedReminderTime = inputReminderTime + ":00";

        String token = getToken();
        if (token == null) return;

        MedicationReminderRequest request = new MedicationReminderRequest(name, time, note, formattedReminderTime);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<JsonObject> call = apiService.addMedication("Bearer " + token, request);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    Toast.makeText(getContext(), "新增成功", Toast.LENGTH_SHORT).show();

                    clearInputFields();
                    fetchMedicationReminders();

                    // 從回傳資料中取得 reminder_id
                    JsonObject data = response.body().getAsJsonObject("data");
                    int reminderId = data.get("reminder_id").getAsInt();

                    // 設定鬧鐘，使用 reminderId 當作 requestCode
                    setReminderAlarm(reminderId, name, inputReminderTime);

                } else {
                    Toast.makeText(getContext(), "新增失敗：" + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "新增錯誤: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 設定本地鬧鐘提醒
    private void setReminderAlarm(int reminderId, String medicineName, String reminderTime) {
        try {
            if (!reminderTime.matches("\\d{2}:\\d{2}")) {
                Toast.makeText(getContext(), "提醒時間格式錯誤，請輸入 HH:mm", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] parts = reminderTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            long triggerTime = calendar.getTimeInMillis();
            if (triggerTime <= System.currentTimeMillis()) {
                triggerTime += 24 * 60 * 60 * 1000;
            }

//            Log.d("AlarmDebug", "Trigger time: " + triggerTime + " | " + calendar.getTime().toString());

            Context context = requireContext();  // 確保不是 null
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("medicine_name", medicineName);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    reminderId, // 用提醒的 ID 當作唯一識別碼
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "設置提醒時發生錯誤：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // 刪除提醒
    private void deleteMedicationReminder(int reminderId) {
        String token = getToken();
        if (token == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<JsonObject> call = apiService.deleteMedication(reminderId, "Bearer " + token);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String msg = response.body().get("message").getAsString();
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    // 從本地 medicineList 找到被刪除的那一筆資料
                    boolean found = false;
                    for (MedicationReminder reminder : medicineList) {
                        if (reminder.getId() == reminderId) {
                            cancelReminderAlarm(reminderId, reminder.getMedication_name());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Log.w("AlarmCancel", "找不到 reminderId = " + reminderId + "，鬧鐘可能未取消");
                    }

                    fetchMedicationReminders();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(getContext(), "刪除失敗：" + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "刪除失敗，且錯誤訊息無法解析", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "刪除錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //刪除用藥提醒的鬧鐘
    private void cancelReminderAlarm(int reminderId, String medicineName) {
        Context context = requireContext();
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("medicine_name", medicineName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("AlarmCancel", "已取消 reminderId = " + reminderId);
        } else {
            Log.e("AlarmCancel", "AlarmManager 為 null，無法取消提醒");
        }
    }

    // 取得 Token（統一處理）
    private String getToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "尚未登入", Toast.LENGTH_SHORT).show();
            return null;
        }
        return token;
    }

    // 清空所有輸入欄位
    private void clearInputFields() {
        etMedicineName.setText("");
        etMedicineTime.setText("");
        etMedicineNote.setText("");
        etReminderTime.setText("");
    }

    private void navigateTo(int id) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(id);
    }

    // 更新頭像列資料
    private void loadUserProfile() {
        UserProfileManager.loadUserProfile(requireContext(), new UserProfileManager.OnProfileLoadedListener() {
            @Override
            public void onProfileLoaded(String username, Bitmap avatar) {
                cachedUsername = username;
                cachedAvatar = avatar;

                tvUsername.setText(username);
                if (avatar != null) {
                    ivUserPhoto.setImageBitmap(avatar);
                } else {
                    ivUserPhoto.setImageResource(R.drawable.ic_user_photo);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //更新資料後的toast
    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        // 不等後端回傳，直接更新顯示
        Toast.makeText(getContext(), "資料更新中...", Toast.LENGTH_SHORT).show();
        if (!newName.isEmpty()) {
            cachedUsername = newName;
            tvUsername.setText(newName);
        }
        if (imageUri != null) {
            ivUserPhoto.setImageURI(imageUri);
        }

        // 同時還是呼叫一次後端去刷新快取
        tvUsername.postDelayed(this::loadUserProfile, 2000);
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            }
        }
    }
}