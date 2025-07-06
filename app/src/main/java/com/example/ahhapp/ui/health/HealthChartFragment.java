package com.example.ahhapp.ui.health;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ahhapp.R;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.data.modle.GetBloodSugarResponse;
import com.example.ahhapp.data.modle.GetVitalsResponse;
import com.example.ahhapp.utils.UserProfileManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HealthChartFragment extends  Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {
    private LineChart bloodPressureChart;
    private LineChart bloodSugarChart;

    private TextView tvUsername;
    private ImageView ivUserPhoto;

    // 暫存
    private Bitmap cachedAvatar = null;
    private String cachedUsername = null;

    //空建構子
    public HealthChartFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_health_chart, container, false);

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

        // 綁定頭像列並設點擊事件
        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this); // 傳入當前 Fragment 作為 listener
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });

        // 綁定返回鍵
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // 綁定圖表
        bloodPressureChart = view.findViewById(R.id.lineChartBloodPressure);
        bloodSugarChart = view.findViewById(R.id.lineChartBloodSugar);

        fetchAndShowVitals();
        fetchAndShowBloodSugar();

        return view;
    }


    //取得token
    private String getAuthHeader() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "Token 不存在，請重新登入", Toast.LENGTH_SHORT).show();
            return null;
        }
        return "Bearer " + token;
    }

    //取得血壓與心跳資料並顯示
    private void fetchAndShowVitals() {

        String authHeader = getAuthHeader();
        if (authHeader == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getVitals(authHeader).enqueue(new Callback<GetVitalsResponse>() {
            @Override
            public void onResponse(Call<GetVitalsResponse> call, Response<GetVitalsResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "血壓資料獲取失敗", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<GetVitalsResponse.VitalsData> data = response.body().getData();

                if (data == null || data.isEmpty()) {
                    bloodPressureChart.clear();
                    Toast.makeText(getContext(), "目前您沒有資料，無法生成圖表", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (data.size() < 7) {
                    Toast.makeText(getContext(), "資料未滿7天，顯示目前資料", Toast.LENGTH_SHORT).show();
                }

                List<Entry> systolicEntries = new ArrayList<>();
                List<Entry> diastolicEntries = new ArrayList<>();
                List<Entry> heartRateEntries = new ArrayList<>();
                List<String> xLabels = new ArrayList<>();

                SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

                for (int i = 0; i < data.size(); i++) {
                    GetVitalsResponse.VitalsData item = data.get(i);
                    systolicEntries.add(new Entry(i, item.getSystolicPressure()));
                    diastolicEntries.add(new Entry(i, item.getDiastolicPressure()));
                    heartRateEntries.add(new Entry(i, item.getHeartRate()));

                    try {
                        Date date = serverFormat.parse(item.getMeasurementDate());
                        xLabels.add(displayFormat.format(date));
                    } catch (Exception e) {
                        xLabels.add("");
                    }
                }

                LineDataSet set1 = new LineDataSet(systolicEntries, "收縮壓");
                set1.setColor(Color.RED);
                set1.setCircleColor(Color.RED);

                LineDataSet set2 = new LineDataSet(diastolicEntries, "舒張壓");
                set2.setColor(Color.BLUE);
                set2.setCircleColor(Color.BLUE);

                LineDataSet set3 = new LineDataSet(heartRateEntries, "心跳");
                set3.setColor(Color.GREEN);
                set3.setCircleColor(Color.GREEN);

                LineData lineData = new LineData(set1, set2, set3);
                bloodPressureChart.setData(lineData);

                XAxis xAxis = bloodPressureChart.getXAxis();
                xAxis.setGranularity(1f);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

                styleChart(bloodPressureChart);
            }

            @Override
            public void onFailure(Call<GetVitalsResponse> call, Throwable t) {
                Toast.makeText(getContext(), "血壓 API 錯誤: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //取得血糖資料並顯示
    private void fetchAndShowBloodSugar() {
        String authHeader = getAuthHeader();
        if (authHeader == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getBloodSugar(authHeader).enqueue(new Callback<GetBloodSugarResponse>() {
            @Override
            public void onResponse(Call<GetBloodSugarResponse> call, Response<GetBloodSugarResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "血糖資料獲取失敗", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<GetBloodSugarResponse.BloodSugarData> data = response.body().getData();
                if (data == null || data.isEmpty()) {
                    bloodSugarChart.clear();
                    Toast.makeText(getContext(), "目前您沒有資料，無法生成圖表", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (data.size() < 7) {
                    Toast.makeText(getContext(), "資料未滿7天，顯示目前資料", Toast.LENGTH_SHORT).show();
                }

                List<Entry> fastingEntries = new ArrayList<>();
                List<Entry> postMealEntries = new ArrayList<>();
                List<String> xLabels = new ArrayList<>();

                SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

                for (int i = 0; i < data.size(); i++) {
                    GetBloodSugarResponse.BloodSugarData item = data.get(i);
                    if (item.getMeasurementContext() == 0) {
                        fastingEntries.add(new Entry(i, (float) item.getBloodSugar()));
                    } else if (item.getMeasurementContext() == 2) {
                        postMealEntries.add(new Entry(i, (float) item.getBloodSugar()));
                    }

                    try {
                        Date date = serverFormat.parse(item.getMeasurementDate());
                        xLabels.add(displayFormat.format(date));
                    } catch (Exception e) {
                        xLabels.add("");
                    }
                }

                LineDataSet set1 = new LineDataSet(fastingEntries, "空腹血糖");
                set1.setColor(Color.RED);
                set1.setCircleColor(Color.RED);

                LineDataSet set2 = new LineDataSet(postMealEntries, "餐後血糖");
                set2.setColor(Color.MAGENTA);
                set2.setCircleColor(Color.MAGENTA);

                LineData lineData = new LineData(set1, set2);
                bloodSugarChart.setData(lineData);

                XAxis xAxis = bloodSugarChart.getXAxis();
                xAxis.setGranularity(1f);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

                styleChart(bloodSugarChart);
            }

            @Override
            public void onFailure(Call<GetBloodSugarResponse> call, Throwable t) {
                Toast.makeText(getContext(), "血糖 API 錯誤: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //設定圖表樣式
    private void styleChart(LineChart chart) {
        chart.getDescription().setEnabled(false); // 不顯示描述
        chart.setDrawGridBackground(false);

        // 給圖表留出額外的邊距
        chart.setExtraOffsets(10, 10, 10, 10);
        chart.setExtraBottomOffset(40f); // 留出 Legend 空間

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X 軸在底部
        xAxis.setDrawGridLines(false); // 不畫網格線
        xAxis.setTextSize(16f); // X軸標籤文字

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true); // 顯示左側網格線
        leftAxis.setTextSize(16f); // Y軸標籤文字

        chart.getAxisRight().setEnabled(false);// 右側 Y 軸不要

        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(16f);
        legend.setFormSize(12f);               // 小線條或方塊的大小
        legend.setXEntrySpace(20f);           // 每個 legend 項目之間的間距
        legend.setFormToTextSpace(5f);       // 小線/方塊 和文字的距離

        // 調整 Legend 位置
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);


        chart.invalidate(); // 刷新圖表
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
