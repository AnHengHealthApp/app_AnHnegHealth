package com.example.ahhapp;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class ProfileUtils {

    // 提供開啟「個人資料編輯對話框」的功能
    public static void showEditProfileDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit_profile);

        // 設定 Dialog 尺寸與背景樣式
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.profile_card_bg);

        // 把 dialog 內的元件一一連接起來
        EditText etUsername = dialog.findViewById(R.id.etNewUsername);
        EditText etEmail = dialog.findViewById(R.id.etNewEmail);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmChange);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        ImageView cancel = dialog.findViewById(R.id.ivClose);

        btnConfirm.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            //更新邏輯待寫
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}