package com.example.ahhapp.ui.report;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ahhapp.R;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.data.modle.IssueReportRequest;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ErrorReportFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {
    private EditText etErrorMessage;

    public ErrorReportFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_error_report, container, false);

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


    // 這是用來接收 dialog 回傳資料的地方（只顯示 Toast）
    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        Toast.makeText(getContext(), "輸入完成,已成功變更", Toast.LENGTH_SHORT).show();
    }
}
