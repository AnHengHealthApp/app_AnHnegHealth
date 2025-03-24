package com.example.ahhapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class EditBloodPressureFragment extends Fragment {
    public EditBloodPressureFragment(){}
    private LinearLayout etProfile;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 載入對應的畫面 XML
        View view = inflater.inflate(R.layout.fragment_edit_blood_pressure, container, false);

        // 上方個人資訊列
        etProfile = view.findViewById(R.id.etProfile);
        etProfile.setOnClickListener(v -> ProfileUtils.showEditProfileDialog(requireContext()));

        // 綁定返回按鈕
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // 返回上一個 Fragment（通常是 Home）
            Navigation.findNavController(v).popBackStack();
        });

        //點擊"血糖紀錄->"跳轉
        TextView tvGoToBloodSugar = view.findViewById(R.id.tvGoToBloodSugar);
        tvGoToBloodSugar.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.editBloodSugarFragment);
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveUserInput(); // ⚠️ 頁面離開時自動儲存
    }

    private void saveUserInput() {
        // TODO: 這裡之後串接資料庫時再實作
        // 例如：從 EditText 取得輸入值並存進 SQLite 或傳給 API
    }
}
