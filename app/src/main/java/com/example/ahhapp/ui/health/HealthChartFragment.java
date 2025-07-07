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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HealthChartFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {
    private LineChart bloodPressureChart, bloodSugarChart;
    private TextView tvUsername;
    private ImageView ivUserPhoto;

    private Bitmap cachedAvatar = null;
    private String cachedUsername = null;

    public HealthChartFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health_chart, container, false);

        tvUsername = view.findViewById(R.id.tvUsername);
        ivUserPhoto = view.findViewById(R.id.ivUserPhoto);

        if (cachedUsername != null) tvUsername.setText(cachedUsername);
        if (cachedAvatar != null) ivUserPhoto.setImageBitmap(cachedAvatar);
        else ivUserPhoto.setImageResource(R.drawable.ic_user_photo);

        loadUserProfile();

        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this);
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        bloodPressureChart = view.findViewById(R.id.lineChartBloodPressure);
        bloodSugarChart = view.findViewById(R.id.lineChartBloodSugar);

        fetchAndShowVitals();
        fetchAndShowBloodSugar();

        return view;
    }

    private String getAuthHeader() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "Token 不存在，請重新登入", Toast.LENGTH_SHORT).show();
            return null;
        }
        return "Bearer " + token;
    }

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
                    Toast.makeText(getContext(), "目前您沒有血壓資料，無法生成圖表", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (data.size() < 7) {
                    Toast.makeText(getContext(), "血壓資料未滿7天，顯示目前資料", Toast.LENGTH_SHORT).show();
                }

                //將傳入的資料順序顛倒,確保x軸資料由日期最遠排到最近(ex: 7/1 - 7/7)
                Collections.reverse(data);

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

                LineDataSet set1 = createBeautifulDataSet(systolicEntries, "收縮壓", Color.RED);
                LineDataSet set2 = createBeautifulDataSet(diastolicEntries, "舒張壓", Color.BLUE);
                LineDataSet set3 = createBeautifulDataSet(heartRateEntries, "心跳", Color.GREEN);

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
                    Toast.makeText(getContext(), "目前您沒有血糖資料，無法生成圖表", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (data.size() < 7) {
                    Toast.makeText(getContext(), "血糖資料未滿7天，顯示目前資料", Toast.LENGTH_SHORT).show();
                }

                // 合併同一天的空腹 & 餐後
                TreeMap<String, BloodSugarDay> dayMap = new TreeMap<>();
                SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

                for (GetBloodSugarResponse.BloodSugarData item : data) {
                    String dateKey = "";
                    try {
                        Date date = serverFormat.parse(item.getMeasurementDate());
                        dateKey = displayFormat.format(date);
                    } catch (Exception e) {
                        continue;
                    }

                    BloodSugarDay day = dayMap.getOrDefault(dateKey, new BloodSugarDay());
                    if (item.getMeasurementContext() == 0) {
                        day.fasting = (float) item.getBloodSugar();
                    } else if (item.getMeasurementContext() == 2) {
                        day.postMeal = (float) item.getBloodSugar();
                    }
                    dayMap.put(dateKey, day);
                }

                List<Entry> fastingEntries = new ArrayList<>();
                List<Entry> postMealEntries = new ArrayList<>();
                List<String> xLabels = new ArrayList<>();

                int index = 0;
                for (Map.Entry<String, BloodSugarDay> entry : dayMap.entrySet()) {
                    String date = entry.getKey();
                    BloodSugarDay day = entry.getValue();

                    if (day.fasting != null) {
                        fastingEntries.add(new Entry(index, day.fasting));
                    }
                    if (day.postMeal != null) {
                        postMealEntries.add(new Entry(index, day.postMeal));
                    }
                    xLabels.add(date);
                    index++;
                }

                LineDataSet set1 = createBeautifulDataSet(fastingEntries, "空腹血糖", Color.RED);
                LineDataSet set2 = createBeautifulDataSet(postMealEntries, "餐後血糖", Color.MAGENTA);

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

    //使用MAP防止血糖資料日期重複
    private static class BloodSugarDay {
        Float fasting;
        Float postMeal;
    }

    //設定DataSet
    private LineDataSet createBeautifulDataSet(List<Entry> entries, String label, int color) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setLineWidth(3f);
        set.setColor(color);
        set.setCircleColor(color);
        set.setCircleRadius(5f);
        set.setCircleHoleRadius(2f);
        set.setDrawCircles(true);
        set.setDrawValues(false);
        return set;
    }

    //設定圖表樣式
    private void styleChart(LineChart chart) {
        chart.setBackgroundColor(Color.BLACK); // 暗色背景
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);

        // 留白
        chart.setExtraOffsets(10, 10, 10, 10);
        chart.setExtraBottomOffset(20f);

        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

        // X 軸
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(14f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-30f);// 防擠壓

        // Y 軸
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextSize(14f);
        leftAxis.setTextColor(Color.WHITE);

        chart.getAxisRight().setEnabled(false);

        // Legend
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(14f);
        legend.setFormSize(14f);
        legend.setXEntrySpace(20f);
        legend.setFormToTextSpace(8f);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);
        legend.setTextColor(Color.WHITE);


        chart.animateX(500); // 加點動畫
        chart.invalidate();
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
