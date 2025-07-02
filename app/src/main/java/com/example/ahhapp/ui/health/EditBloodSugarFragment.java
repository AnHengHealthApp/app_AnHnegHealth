package com.example.ahhapp.ui.health;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ahhapp.R;
import com.example.ahhapp.data.modle.BloodSugarRequest;
import com.example.ahhapp.data.modle.BloodSugarResponse;
import com.example.ahhapp.data.modle.UpdateProfileRequest;
import com.example.ahhapp.data.modle.UpdateProfileResponse;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditBloodSugarFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    public EditBloodSugarFragment() {
        // 空建構子（必要）
    }

    private LinearLayout etProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_blood_sugar, container, false);

        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this);
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });

        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        view.findViewById(R.id.btnSubmitSuger).setOnClickListener(v -> {
            EditText etEmpty = view.findViewById(R.id.etEmptyBloodSuger);
            EditText etFull = view.findViewById(R.id.etFullBloodSuger);
            String emptyText = etEmpty.getText().toString().trim();
            String fullText = etFull.getText().toString().trim();

            if (!emptyText.isEmpty()) sendBloodSugar(view, 0);
            if (!fullText.isEmpty()) sendBloodSugar(view, 2);
            if (emptyText.isEmpty() && fullText.isEmpty()) {
                Toast.makeText(getContext(), "請至少填寫一項血糖數值", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnSubmitinfo).setOnClickListener(v -> updateProfileInfo(view));

        return view;
    }

    private void updateProfileInfo(View rootView) {
        EditText etHeight = rootView.findViewById(R.id.etHeight);
        EditText etWeight = rootView.findViewById(R.id.etWeight);
        EditText etBirthday = rootView.findViewById(R.id.etBirthday);
        EditText etGender = rootView.findViewById(R.id.etGender);

        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String genderStr = etGender.getText().toString().trim();

        if (heightStr.isEmpty() || weightStr.isEmpty() || birthday.isEmpty() || genderStr.isEmpty()) {
            Toast.makeText(getContext(), "請完整填寫基本健康資料", Toast.LENGTH_SHORT).show();
            return;
        }

        int height, weight, gender;
        try {
            height = Integer.parseInt(heightStr);
            weight = Integer.parseInt(weightStr);
            gender = Integer.parseInt(genderStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "請輸入有效數字（身高、體重、性別）", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "尚未登入", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(height, weight, birthday, gender);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        Call<UpdateProfileResponse> call = apiService.updateProfile(request, "Bearer " + token);
        call.enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "基本資料更新成功", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(errorBody, JsonObject.class);
                        JsonObject errorObj = jsonObject.getAsJsonObject("error");
                        String errorMessage = errorObj.get("message").getAsString();
                        Toast.makeText(getContext(), "更新失敗：" + errorMessage, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "更新失敗，無法解析錯誤訊息", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                Toast.makeText(getContext(), "錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendBloodSugar(View rootView, int context) {
        EditText etDate = rootView.findViewById(R.id.etDate);
        EditText etEmpty = rootView.findViewById(R.id.etEmptyBloodSuger);
        EditText etFull = rootView.findViewById(R.id.etFullBloodSuger);

        String date = etDate.getText().toString().trim();
        String sugarStr = (context == 0) ? etEmpty.getText().toString().trim() : etFull.getText().toString().trim();

        if (date.isEmpty() || sugarStr.isEmpty()) {
            Toast.makeText(getContext(), "請填寫日期與" + (context == 0 ? "空腹" : "餐後") + "血糖", Toast.LENGTH_SHORT).show();
            return;
        }

        double sugar;
        try {
            sugar = Double.parseDouble(sugarStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "血糖數值格式錯誤", Toast.LENGTH_SHORT).show();
            return;
        }

        if (context == 0 && sugar > 130) {
            showHealthNotification("空腹血糖偏高", "您的空腹血糖已超過 130，請注意飲食與作息");
        } else if (context == 2 && sugar > 180) {
            showHealthNotification("餐後血糖偏高", "餐後血糖高於 180，建議監測與調整飲食");
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "尚未登入，無法傳送血糖", Toast.LENGTH_SHORT).show();
            return;
        }

        BloodSugarRequest request = new BloodSugarRequest(date, context, sugar);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<BloodSugarResponse> call = apiService.addBloodSugar(request, "Bearer " + token);

        call.enqueue(new Callback<BloodSugarResponse>() {
            @Override
            public void onResponse(Call<BloodSugarResponse> call, Response<BloodSugarResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), (context == 0 ? "空腹" : "餐後") + "血糖上傳成功", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(getContext(), "上傳失敗：" + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "上傳失敗，無法解析錯誤訊息", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BloodSugarResponse> call, Throwable t) {
                Toast.makeText(getContext(), "血糖錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
        }
    }

    private void showHealthNotification(String title, String message) {
        checkNotificationPermissionIfNeeded();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "health_channel")
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "缺少通知權限，無法顯示提醒", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        Toast.makeText(getContext(), "資料更新中..." ,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}