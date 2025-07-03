package com.example.ahhapp.ui.health;

import android.graphics.Bitmap;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ahhapp.R;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.example.ahhapp.utils.UserProfileManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;


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

        setupBloodPressureChart();
        setupBloodSugarChart();

        return view;
    }


    //將血壓資料填入圖表
    private void setupBloodPressureChart() {
        List<Entry> systolicEntries = new ArrayList<>();
        List<Entry> diastolicEntries = new ArrayList<>();
        List<Entry> heartRateEntries = new ArrayList<>();

        //假資料測試
        systolicEntries.add(new Entry(0, 130));
        systolicEntries.add(new Entry(1, 128));
        systolicEntries.add(new Entry(2, 125));

        diastolicEntries.add(new Entry(0, 80));
        diastolicEntries.add(new Entry(1, 78));
        diastolicEntries.add(new Entry(2, 82));

        heartRateEntries.add(new Entry(0, 72));
        heartRateEntries.add(new Entry(1, 70));
        heartRateEntries.add(new Entry(2, 75));
        LineDataSet set1 = new LineDataSet(systolicEntries, "收縮壓");
        set1.setColor(Color.RED);
        set1.setCircleColor(Color.RED);
        set1.setLineWidth(4f); // 收縮壓
        set1.setCircleRadius(6f);
        set1.setValueTextSize(14f);
        set1.setDrawValues(false);

        LineDataSet set2 = new LineDataSet(diastolicEntries, "舒張壓");
        set2.setColor(Color.BLUE);
        set2.setCircleColor(Color.BLUE);
        set2.setLineWidth(4f); // 舒張壓
        set2.setCircleRadius(6f);
        set2.setValueTextSize(14f);
        set2.setDrawValues(false);

        LineDataSet set3 = new LineDataSet(heartRateEntries, "心跳");
        set3.setColor(Color.GREEN);
        set3.setCircleColor(Color.GREEN);
        set3.setLineWidth(4f); // 心跳
        set3.setCircleRadius(6f);
        set3.setValueTextSize(14f);
        set3.setDrawValues(false);

        LineData lineData = new LineData(set1, set2, set3);
        bloodPressureChart.setData(lineData);
        styleChart(bloodPressureChart);
    }

    //將血糖資料填入圖表
    private void setupBloodSugarChart() {
        List<Entry> fastingEntries = new ArrayList<>();
        List<Entry> postMealEntries = new ArrayList<>();

        // 加入假資料（最近幾天）
        fastingEntries.add(new Entry(0, 90));
        fastingEntries.add(new Entry(1, 88));
        fastingEntries.add(new Entry(2, 92));

        postMealEntries.add(new Entry(0, 140));
        postMealEntries.add(new Entry(1, 125));
        postMealEntries.add(new Entry(2, 130));

        LineDataSet set1 = new LineDataSet(fastingEntries, "空腹血糖");
        set1.setColor(Color.RED);
        set1.setCircleColor(Color.RED);
        set1.setLineWidth(4f); // 空腹
        set1.setCircleRadius(6f);
        set1.setValueTextSize(14f);
        set1.setDrawValues(false);

        LineDataSet set2 = new LineDataSet(postMealEntries, "餐後血糖");
        set2.setColor(Color.MAGENTA);
        set2.setCircleColor(Color.MAGENTA);
        set2.setLineWidth(4f); // 餐後
        set2.setCircleRadius(6f);
        set2.setValueTextSize(14f);
        set2.setDrawValues(false);

        LineData lineData = new LineData(set1, set2);
        bloodSugarChart.setData(lineData);
        styleChart(bloodSugarChart);
    }

    //設定圖表樣式
    private void styleChart(LineChart chart) {
        chart.getDescription().setEnabled(false); // 不顯示描述
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X 軸在底部
        xAxis.setDrawGridLines(false); // 不畫網格線
        xAxis.setTextSize(18f); // X軸標籤文字

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true); // 顯示左側網格線
        leftAxis.setTextSize(18f); // Y軸標籤文字

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false); // 右側 Y 軸不要

        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(20f); // 圖例文字



        chart.invalidate(); // 刷新圖表
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
