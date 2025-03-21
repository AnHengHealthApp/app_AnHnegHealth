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

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    // 宣告一個 LinearLayout，當作可點擊的個人資訊區塊
    private LinearLayout etProfile;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將 fragment_home.xml 佈局載入到畫面上
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 找到畫面中的 etProfile（點擊後會彈出編輯資料的 Dialog）
        etProfile = view.findViewById(R.id.etProfile);

        // 為個人資訊區塊設定點擊事件
        etProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 建立一個自定義 Dialog（彈出視窗）
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_edit_profile);// 設定使用的 layout

                // 設定 Dialog 尺寸與背景樣式
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.profile_card_bg);

                // 把 dialog 內的元件一一連接起來
                EditText etUsername = dialog.findViewById(R.id.etNewUsername);
                EditText etEmail = dialog.findViewById(R.id.etNewEmail);
                Button btnConfirm = dialog.findViewById(R.id.btnConfirmChange);
                Button btnCancel = dialog.findViewById(R.id.btnCancel);
                ImageView cancel = dialog.findViewById(R.id.ivClose);

                // 當按下「確認」按鈕時
                btnConfirm.setOnClickListener(v -> {
                    String newUsername = etUsername.getText().toString().trim();
                    String newEmail = etEmail.getText().toString().trim();
                    dialog.dismiss();
                });

                // 當按下「取消」或「X」圖示時，直接關閉 Dialog
                btnCancel.setOnClickListener(v -> {
                    dialog.dismiss();
                });

                cancel.setOnClickListener(v -> {
                    dialog.dismiss();
                });

                // 顯示對話框
                dialog.show();
            }
        });

        return view;// 傳回畫面
    }
}
