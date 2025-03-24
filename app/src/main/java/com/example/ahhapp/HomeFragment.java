package com.example.ahhapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    // 宣告一個 LinearLayout，當作可點擊的個人資訊區塊
    private LinearLayout etProfile;
    public HomeFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將 fragment_home.xml 佈局載入到畫面上
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 點擊頭像區塊 ➜ 顯示 Dialog（共用方法）
        etProfile = view.findViewById(R.id.etProfile);
        etProfile.setOnClickListener(v -> ProfileUtils.showEditProfileDialog(requireContext()));


        // 編輯健康資訊與紀錄按鈕跳轉(to 血壓)
        Button btnEditHealthInfo = view.findViewById(R.id.btnEditHealthInfo);
        btnEditHealthInfo.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.editBloodPressureFragment);
        });

        //錯誤回報按鈕跳轉
        Button btnErrorReport = view.findViewById(R.id.btnErrorReport);
        btnErrorReport.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.errorReportFragment);
        });

        return view;// 傳回畫面
    }
}
