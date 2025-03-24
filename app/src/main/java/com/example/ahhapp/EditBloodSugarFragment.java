package com.example.ahhapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class EditBloodSugarFragment extends Fragment {

    public EditBloodSugarFragment() {
        // 空建構子（必要）
    }

    private LinearLayout etProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 載入 XML 畫面
        View view = inflater.inflate(R.layout.fragment_edit_blood_sugar, container, false);

        // 綁定頭像欄區塊並設定點擊事件
        etProfile = view.findViewById(R.id.etProfile);
        etProfile.setOnClickListener(v -> ProfileUtils.showEditProfileDialog(requireContext()));

        // 綁定返回按鈕，點擊時返回上一頁
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveUserInput(); // 離開畫面時儲存輸入
    }

    private void saveUserInput() {
        // TODO: 之後串接 SQLite 或 API 時，把 EditText 中的資料存進資料庫

        // 示意：目前用 log 確認執行時機
        Log.d("AutoSave", "EditBloodSugarFragment 頁面離開，觸發自動儲存");
    }
}