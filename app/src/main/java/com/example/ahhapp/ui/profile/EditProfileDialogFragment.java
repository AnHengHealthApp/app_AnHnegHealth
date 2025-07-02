package com.example.ahhapp.ui.profile;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ahhapp.R;
import com.example.ahhapp.data.modle.UpdateAvatarResponse;
import com.example.ahhapp.data.modle.UpdateUserProfileRequest;
import com.example.ahhapp.data.modle.UpdateUserProfileResponse;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.utils.FileUtil;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileDialogFragment extends DialogFragment {

    // 建立一個介面供外部呼叫的 Fragment 實作，用來接收回傳資料（名稱、Email）
    public interface OnProfileUpdatedListener {
        void onProfileUpdated(String newName, String newEmail, Uri imageUri);
    }
    private OnProfileUpdatedListener listener;
    private Uri selectedImageUri;
    private ImageView ivAddIcon;
    private boolean isProfileUpdated = false;
    private boolean isAvatarUploaded = false;
    private boolean profileSuccess = false;
    private boolean avatarSuccess = false;


    // ActivityResultLauncher：用來從相簿選取圖片
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivAddIcon.setImageURI(uri);
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
        ivAddIcon.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // 點「確認更改」
        btnConfirm.setOnClickListener(v -> {
            String newName = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            isProfileUpdated = false;
            isAvatarUploaded = false;
            profileSuccess = false;
            avatarSuccess = false;

            if (newName.isEmpty() && newEmail.isEmpty() && selectedImageUri == null) {
                safeToast("請至少更改一項資料");
                return;
            }

            if (!newName.isEmpty() || !newEmail.isEmpty()) {
                updateUserProfile(newName, newEmail);
            } else {
                isProfileUpdated = true;
                profileSuccess = true;
            }

            if (selectedImageUri != null) {
                uploadAvatar(selectedImageUri);
            } else {
                isAvatarUploaded = true;
                avatarSuccess = true;
            }

            if (listener != null) {
                listener.onProfileUpdated(newName, newEmail, selectedImageUri);
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
        ivClose.setOnClickListener(v -> dismiss());

        return view;
    }

    // 呼叫 API 更新使用者名稱與 Email
    private void updateUserProfile(String name, String email) {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            safeToast("尚未登入，無法更新資料");
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(name, email);

        Call<UpdateUserProfileResponse> call = apiService.updateUserProfile(request, "Bearer " + token);
        call.enqueue(new Callback<UpdateUserProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateUserProfileResponse> call, Response<UpdateUserProfileResponse> response) {
                if (!isAdded()) return;
                isProfileUpdated = true;
                profileSuccess = response.isSuccessful() && response.body() != null;
                checkIfAllDone();
            }

            @Override
            public void onFailure(Call<UpdateUserProfileResponse> call, Throwable t) {
                if (!isAdded()) return;
                isProfileUpdated = true;
                profileSuccess = false;
                checkIfAllDone();
            }
        });
    }

    //呼叫API更新使用者頭像
    private void uploadAvatar(Uri imageUri) {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            safeToast("尚未登入，無法上傳頭像");
            return;
        }

        MultipartBody.Part imagePart = FileUtil.prepareFilePart(context, "avatar", imageUri);
        if (imagePart == null) {
            Log.e("UploadDebug", "prepareFilePart 回傳 null，無法處理圖片");
            safeToast("無法處理圖片檔案");
            return;
        }

        //debug
        Log.d("UploadDebug", "開始上傳頭像：" + imageUri.toString());
        Log.d("UploadDebug", "MimeType: " + imagePart.body().contentType());
        Log.d("UploadDebug", "Part headers: " + imagePart.headers());
        Log.d("UploadDebug", "實際上傳的 Part: " + imagePart.toString());

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<UpdateAvatarResponse> call = apiService.uploadAvatar(imagePart, "Bearer " + token);

        call.enqueue(new Callback<UpdateAvatarResponse>() {
            @Override
            public void onResponse(Call<UpdateAvatarResponse> call, Response<UpdateAvatarResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Log.d("UploadDebug", "頭像上傳成功");
                    safeToast("頭像更新成功");
                    dismiss();
                } else {
                    Log.e("UploadDebug", "頭像上傳失敗：" + response.code() + " " + response.message());
                    safeToast("頭像上傳失敗：" + response.code());
                }
            }

            @Override
            public void onFailure(Call<UpdateAvatarResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("UploadDebug", "頭像上傳錯誤：" + t.getMessage(), t);
                safeToast("錯誤：" + t.getMessage());
            }
        });
    }

    //確認資料回傳
    private void checkIfAllDone() {
        if ((selectedImageUri == null || isAvatarUploaded) && isProfileUpdated) {
            if (profileSuccess && (selectedImageUri == null || avatarSuccess)) {
                safeToast("資料已成功更新");
            } else {
                safeToast("部分資料更新失敗");
            }
            dismiss();
        }
    }


    //安全toast防crash
    private void safeToast(String msg) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    // Dialog UI 設定
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.profile_card_bg);
        }
    }
}
