package com.example.ahhapp.ui.report;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ahhapp.R;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.data.modle.IssueReportRequest;
import com.example.ahhapp.utils.UserProfileManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ErrorReportFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {
    private EditText etErrorMessage;

    private TextView tvUsername;
    private ImageView ivUserPhoto;

    // 暫存
    private Bitmap cachedAvatar = null;
    private String cachedUsername = null;

    public ErrorReportFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_error_report, container, false);

        //初始化頭像列 UI 元件
        tvUsername = view.findViewById(R.id.tvUsername);
        ivUserPhoto = view.findViewById(R.id.ivUserPhoto);

        // 如果有快取資料先顯示
        if (cachedUsername != null) tvUsername.setText(cachedUsername);
        if (cachedAvatar != null) {
            ivUserPhoto.setImageBitmap(cachedAvatar);
        } else {
            ivUserPhoto.setImageResource(R.drawable.ic_user_photo);
        }

        // 載入使用者資料
        loadUserProfile();

        etErrorMessage = view.findViewById(R.id.etErrorMessage);

        // 點擊返回鍵
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // 點擊「確認回報」按鈕
        view.findViewById(R.id.btnSubmitError).setOnClickListener(v -> submitIssueReport(v));

        // 點擊頭像區塊 → 開啟編輯視窗
        LinearLayout etProfile = view.findViewById(R.id.etProfile);
        etProfile.setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this);
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });

        return view;
    }

    //提交問題回報
    private void submitIssueReport(View view) {
        String description = etErrorMessage.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(getContext(), "問題描述不能為空", Toast.LENGTH_SHORT).show();
            return;
        }

        IssueReportRequest request = new IssueReportRequest(description);

        // 從 SharedPreferences 取得 JWT Token
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "請先登入帳戶", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<JsonObject> call = apiService.submitIssueReport("Bearer " + token, request);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "已送出回報，感謝您的回饋！", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                } else {
                    Toast.makeText(getContext(), "回報失敗，請稍後再試", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "網路錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ErrorReport", "API Error", t);
            }
        });
    }

    private void navigateTo(int id) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(id);
    }

    // 更新頭像列資料
    private void loadUserProfile() {
        UserProfileManager.loadUserProfile(requireContext(), new UserProfileManager.OnProfileLoadedListener() {
            @Override
            public void onProfileLoaded(String username, Bitmap avatar) {
                cachedUsername = username;
                cachedAvatar = avatar;

                tvUsername.setText(username);
                if (avatar != null) {
                    ivUserPhoto.setImageBitmap(avatar);
                } else {
                    ivUserPhoto.setImageResource(R.drawable.ic_user_photo);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    //更新資料後的toast
    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        // 不等後端回傳，直接更新顯示
        Toast.makeText(getContext(), "資料更新中...", Toast.LENGTH_SHORT).show();
        if (!newName.isEmpty()) {
            cachedUsername = newName;
            tvUsername.setText(newName);
        }
        if (imageUri != null) {
            ivUserPhoto.setImageURI(imageUri);
        }

        // 同時還是呼叫一次後端去刷新快取
        tvUsername.postDelayed(this::loadUserProfile, 2000);
    }
}
