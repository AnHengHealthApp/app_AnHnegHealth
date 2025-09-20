package com.example.ahhapp.ui.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ahhapp.R;
import com.example.ahhapp.data.modle.BasicHealthInfoResponse;
import com.example.ahhapp.data.modle.GetVitalsResponse;
import com.example.ahhapp.data.modle.GetBloodSugarResponse;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.example.ahhapp.utils.UserProfileManager;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import android.util.Log;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {

    //Ui 元件宣告
    private TextView tvHeight, tvWeight, tvAge, tvGender,
            tvAvgHeartRate, tvAvgSystolic, tvAvgDiastolic, tvMaxBpDiff,
            tvAvgFastingGlucose, tvAvgPostprandialGlucose, tvMaxGlucoseDiff;

    private TextView tvUsername;
    private ImageView ivUserPhoto;

    // 暫存
    private Bitmap cachedAvatar = null;
    private String cachedUsername = null;

    //取得 SharedPreferences 裡的 token
    private String getToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "尚未登入", Toast.LENGTH_SHORT).show();
            return null;
        }
        return "Bearer " + token;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

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

        // 綁定 UI 元件
        tvHeight = view.findViewById(R.id.tvHeight);
        tvWeight = view.findViewById(R.id.tvWeight);
        tvAge = view.findViewById(R.id.tvAge);
        tvGender = view.findViewById(R.id.tvGender);
        tvAvgHeartRate = view.findViewById(R.id.tvAvgHeartRate);
        tvAvgSystolic = view.findViewById(R.id.tvAvgSystolic);
        tvAvgDiastolic = view.findViewById(R.id.tvAvgDiastolic);
        tvMaxBpDiff = view.findViewById(R.id.tvMaxBpDiff);
        tvAvgFastingGlucose = view.findViewById(R.id.tvAvgFastingGlucose);
        tvAvgPostprandialGlucose = view.findViewById(R.id.tvAvgPostprandialGlucose);
        tvMaxGlucoseDiff = view.findViewById(R.id.tvMaxGlucoseDiff);

        //返回建
        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // 返回上一個 Fragment（通常是 Home）
            Navigation.findNavController(v).popBackStack();
        });

        //頭像列
        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this); // 傳入當前 Fragment 作為 listener
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });

        // 呼叫API 拿資料
        fetchBasicInfo();
        fetchVitals();
        fetchBloodSugar();

        return view;
    }

    // 取得基本健康資訊
    private void fetchBasicInfo() {
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        api.getBasicHealthInfo(getToken()).enqueue(new Callback<BasicHealthInfoResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicHealthInfoResponse> call, @NonNull Response<BasicHealthInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicHealthInfoResponse.BasicHealthData data = response.body().getData();

                    if (data != null) {
                        tvHeight.setText("身高: " + data.getHeight() + " cm");
                        tvWeight.setText("體重: " + data.getWeight() + " kg");

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date birthDate = sdf.parse(data.getBirthday());
                            Calendar birthCal = Calendar.getInstance();
                            birthCal.setTime(birthDate);
                            Calendar today = Calendar.getInstance();

                            int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
                            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                                age--;
                            }
                            tvAge.setText("年齡: " + age + " 歲");
                        } catch (Exception e) {
                            tvAge.setText("年齡: 無資料");
                        }

                        String genderStr = "未填寫";
                        if (data.getGender() != null) {
                            switch (data.getGender()) {
                                case 0:
                                    genderStr = "男";
                                    break;
                                case 1:
                                    genderStr = "女";
                                    break;
                                case 2:
                                    genderStr = "其他";
                                    break;
                            }
                        }
                        tvGender.setText("性別: " + genderStr);
                    } else {
                        tvHeight.setText("身高: 無資料");
                        tvWeight.setText("體重: 無資料");
                        tvAge.setText("年齡: 無資料");
                        tvGender.setText("性別: 無資料");
                    }
                } else {
                    tvHeight.setText("身高: 無資料");
                    tvWeight.setText("體重: 無資料");
                    tvAge.setText("年齡: 無資料");
                    tvGender.setText("性別: 無資料");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BasicHealthInfoResponse> call, @NonNull Throwable t) {
                tvHeight.setText("身高: 無資料");
                tvWeight.setText("體重: 無資料");
                tvAge.setText("年齡: 無資料");
                tvGender.setText("性別: 無資料");
                Toast.makeText(getContext(), "無法取得基本健康資訊", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 取得血壓資訊
    private void fetchVitals() {
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        api.getVitals(getToken()).enqueue(new Callback<GetVitalsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetVitalsResponse> call, @NonNull Response<GetVitalsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetVitalsResponse.VitalsData> list = response.body().getData();
                    if (list == null || list.isEmpty()) {
                        showNoVitalsData();
                        return;
                    }

                    int totalHr = 0, totalSys = 0, totalDia = 0, maxDiff = 0;
                    for (GetVitalsResponse.VitalsData v : list) {
                        totalHr += v.getHeartRate();
                        totalSys += v.getSystolicPressure();
                        totalDia += v.getDiastolicPressure();
                        int diff = v.getSystolicPressure() - v.getDiastolicPressure();
                        if (diff > maxDiff) maxDiff = diff;
                    }
                    int count = list.size();
                    tvAvgHeartRate.setText("本週平均心跳: " + (totalHr / count) + " 下/分鐘");
                    tvAvgSystolic.setText("本週平均收縮壓: " + (totalSys / count) + " mmHg");
                    tvAvgDiastolic.setText("本週平均舒張壓: " + (totalDia / count) + " mmHg");
                    tvMaxBpDiff.setText("本週最大血壓差: " + maxDiff + " mmHg");
                } else {
                    showNoVitalsData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetVitalsResponse> call, @NonNull Throwable t) {
                showNoVitalsData();
            }

            private void showNoVitalsData() {
                tvAvgHeartRate.setText("本週平均心跳: 無資料");
                tvAvgSystolic.setText("本週平均收縮壓: 無資料");
                tvAvgDiastolic.setText("本週平均舒張壓: 無資料");
                tvMaxBpDiff.setText("本週最大血壓差: 無資料");
            }
        });
    }

    // 取得血糖資訊
    private void fetchBloodSugar() {
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        String token = getToken();
        api.getBloodSugar(getToken()).enqueue(new Callback<GetBloodSugarResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetBloodSugarResponse> call,
                                   @NonNull Response<GetBloodSugarResponse> response) {
                // ✅ 成功進到 onResponse 時，把解析後的物件轉回 JSON 印出（驗證欄位）
                try {
                    Log.d("BloodSugarAPI_Info", "parsed body = " + new Gson().toJson(response.body()));
                } catch (Exception e) {
                    Log.e("BloodSugarAPI_Info", "log parsed body failed", e);
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<GetBloodSugarResponse.BloodSugarData> list = response.body().getData();
                    if (list == null || list.isEmpty()) {
                        showNoBloodSugarData();
                        return;
                    }

                    double fastingSum = 0, postSum = 0;
                    int fastingCount = 0, postCount = 0;

                    Map<String, Day> dayMap = new TreeMap<>();
                    SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (GetBloodSugarResponse.BloodSugarData sugar : list) {
                        // 🔎 逐筆印出原始值，方便找是哪筆髒資料
                        Log.d("BloodSugarAPI_Info:item",
                                "date=" + sugar.getMeasurementDate()
                                        + ", ctx=" + sugar.getMeasurementContext()
                                        + ", val=" + sugar.getBloodSugar());

                        String dateKey = "";
                        try {
                            Date date = serverFormat.parse(sugar.getMeasurementDate());
                            dateKey = displayFormat.format(date);
                        } catch (Exception e) {
                            continue;
                        }

                        Day day = dayMap.getOrDefault(dateKey, new Day());

                        Integer ctx = sugar.getMeasurementContext();
                        Double val = sugar.getBloodSugar();
                        if (ctx == null || val == null) continue;

                        if (ctx == 0 || ctx == 1) { // 空腹 or 餐前
                            day.fasting = val;
                            fastingSum += val;
                            fastingCount++;
                        } else if (ctx == 2) { // 餐後
                            day.postMeal = val;
                            postSum += val;
                            postCount++;
                        }
                        dayMap.put(dateKey, day);
                    }

                    double fastingAvg = fastingCount > 0 ? fastingSum / fastingCount : 0;
                    double postAvg = postCount > 0 ? postSum / postCount : 0;

                    double maxDiff = 0;
                    for (Map.Entry<String, Day> entry : dayMap.entrySet()) {
                        Day d = entry.getValue();
                        if (d.fasting != null && d.postMeal != null) {
                            double diff = d.postMeal - d.fasting;
                            if (diff > maxDiff) {
                                maxDiff = diff;
                            }
                        }
                    }

                    tvAvgFastingGlucose.setText("本週平均空腹血糖: " + (fastingCount > 0 ? (int) fastingAvg + " mg/dL" : "無資料"));
                    tvAvgPostprandialGlucose.setText("本週平均餐後血糖: " + (postCount > 0 ? (int) postAvg + " mg/dL" : "無資料"));
                    tvMaxGlucoseDiff.setText("本週最大血糖差: " + (maxDiff > 0 ? (int) maxDiff + " mg/dL" : "無資料"));
                } else {
                    showNoBloodSugarData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetBloodSugarResponse> call, @NonNull Throwable t) {
                // ❗大多數會進到這裡（NumberFormatException），所以再打 raw 端點把 JSON 印出來
                Log.e("BloodSugarAPI_Info", "parse fail: " + t);
                fetchBloodSugarRawForLog(token, "BloodSugarRAW_Info");

                showNoBloodSugarData();
            }

            private void showNoBloodSugarData() {
                tvAvgFastingGlucose.setText("本週平均空腹血糖: 無資料");
                tvAvgPostprandialGlucose.setText("本週平均餐後血糖: 無資料");
                tvMaxGlucoseDiff.setText("本週最大血糖差: 無資料");
            }
        });
    }

    // 只做除錯：拉 raw JSON 來看
    private void fetchBloodSugarRawForLog(String token, String tag) {
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        api.getBloodSugarRaw(token).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                try {
                    String json = (r.body() != null) ? r.body().string() : null;
                    Log.e(tag, "RAW JSON = " + json);
                } catch (Exception e) {
                    Log.e(tag, "read raw failed", e);
                }
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Log.e(tag, "raw api fail", t);
            }
        });
    }

    private static class Day {
        Double fasting;
        Double postMeal;
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
}
