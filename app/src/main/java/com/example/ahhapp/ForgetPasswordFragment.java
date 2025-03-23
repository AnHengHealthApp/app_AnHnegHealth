package com.example.ahhapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

public class ForgetPasswordFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 載入 fragment_forget_password.xml 畫面
        View view = inflater.inflate(R.layout.fragment_forget_password, container, false);

        // 找到按鈕
        Button btnBackToLogin = view.findViewById(R.id.btn_back_to_login);

        // 設定點擊事件 ➝ 回到登入畫面
        btnBackToLogin.setOnClickListener(v -> {
            // 隱藏 Fragment 畫面
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);

            // 顯示登入頁元件（你也可以選擇只顯示特定欄位）
            requireActivity().findViewById(R.id.login_section).setVisibility(View.VISIBLE);

            // 將此 Fragment 從畫面中移除
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(ForgetPasswordFragment.this)
                    .commit();
        });

        return view;
    }
}
