package com.example.ahhapp.ui.profile;


import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ahhapp.R;

public class EditProfileDialogFragment extends DialogFragment {

    // 建立一個介面供外部呼叫的 Fragment 實作，用來接收回傳資料（名稱、Email）
    public interface OnProfileUpdatedListener {
        void onProfileUpdated(String newName, String newEmail, Uri imageUri);
    }
    private OnProfileUpdatedListener listener;
    private Uri selectedImageUri;
    private ImageView ivAddIcon;

    // ActivityResultLauncher：用來從相簿選取圖片
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivAddIcon.setImageURI(uri); // 預覽選中的圖片
                }
            });

    public void setListener(OnProfileUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 載入 dialog 的 XML 佈局
        View view = inflater.inflate(R.layout.dialog_edit_profile, container, false);

        // 取得 UI 元件參考
        EditText etUsername = view.findViewById(R.id.etNewUsername);
        EditText etEmail = view.findViewById(R.id.etNewEmail);
        Button btnConfirm = view.findViewById(R.id.btnConfirmChange);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        ImageView ivClose = view.findViewById(R.id.ivClose);
        ivAddIcon = view.findViewById(R.id.ivAddIcon);

        // 點圖片時 → 開啟選圖器
        ivAddIcon.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*"); // 選擇一張圖片
        });

        // 點「確認更改」
        btnConfirm.setOnClickListener(v -> {
            String newName = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            // 基本欄位檢查
            if (newName.isEmpty() && newEmail.isEmpty() && selectedImageUri == null) {
                Toast.makeText(getContext(), "請至少更改一項資料", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onProfileUpdated(newName, newEmail, selectedImageUri);
            }

            dismiss(); // 關閉 Dialog
        });

        // 點擊「取消」或「X」關閉對話框
        btnCancel.setOnClickListener(v -> dismiss());
        ivClose.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 自定義對話框的尺寸與背景樣式alog 寬高與背景
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.profile_card_bg);
        }
    }
}
