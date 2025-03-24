package com.example.ahhapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class ErrorReportFragment extends Fragment{
    public ErrorReportFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_error_report, container, false);
        // 點擊返回鍵
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // 點擊「確認回報」按鈕
        view.findViewById(R.id.btnSubmitError).setOnClickListener(v -> {
            Toast.makeText(getContext(), "已送出回報，感謝您的協助！", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).popBackStack(); // 回到上一頁
        });

        // 點擊頭像區塊（與 Home 一樣可彈出編輯視窗）
        LinearLayout etProfile = view.findViewById(R.id.etProfile);
        etProfile.setOnClickListener(v -> ProfileUtils.showEditProfileDialog(requireContext()));

        return view;
    }
}
