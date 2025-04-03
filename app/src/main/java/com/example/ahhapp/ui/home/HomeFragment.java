package com.example.ahhapp.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import androidx.fragment.app.Fragment;

import com.example.ahhapp.R;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;

public class HomeFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener{
    // 宣告一個 LinearLayout，當作可點擊的個人資訊區塊
    private LinearLayout etProfile;
    public HomeFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將 fragment_home.xml 佈局載入到畫面上
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 點擊頭像區塊 ➜ 顯示 Dialog（共用方法）
        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this); // 傳入當前 Fragment 作為 listener
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });


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

        //AI智能問答按鈕跳轉
        Button btnAIQA = view.findViewById(R.id.btnAIQA);
        btnAIQA.setOnClickListener(v-> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_ai_chat);
        });

        //用藥提醒按鈕跳轉
        Button btnMedicationReminder = view.findViewById(R.id.btnMedicationReminder);
        btnMedicationReminder.setOnClickListener(v-> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_medication_reminder);
        });

        //健康曲線圖按鈕跳轉
        Button btnHealthChart = view.findViewById(R.id.btnHealthChart);
        btnHealthChart.setOnClickListener(v-> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_health_chart);
        });
        return view;// 傳回畫面
    }
    //更新資料後的toast
    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        Toast.makeText(getContext(), "資料已回傳！" ,Toast.LENGTH_SHORT).show();
    }
}
