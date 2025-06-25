package com.example.ahhapp.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import android.widget.Toast;
import android.widget.EditText;
import android.util.Log;

import com.example.ahhapp.R;
import com.example.ahhapp.data.modle.ForgotPasswordRequest;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.network.ApiService;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetPasswordFragment extends Fragment {

    private EditText emailEditText;
    private Button btnSendEmail, btnBackToLogin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 載入 fragment_forget_password.xml 畫面
        View view = inflater.inflate(R.layout.fragment_forget_password, container, false);

        // 綁定元件
        emailEditText = view.findViewById(R.id.email_input);
        btnSendEmail = view.findViewById(R.id.btn_send_email);
        btnBackToLogin = view.findViewById(R.id.btn_back_to_login);

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

        // 點擊發送郵件按鈕
        btnSendEmail.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            // 簡單格式驗證
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "請輸入有效的電子郵件", Toast.LENGTH_SHORT).show();
                return;
            }

            // 建立請求物件
            ForgotPasswordRequest request = new ForgotPasswordRequest(email);
            ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
            Call<JsonObject> call = apiService.sendForgotPasswordEmail(request);

            // 發送 API 請求
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String message = response.body().get("message").getAsString();
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

                        // 返回登入畫面
                        requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
                        requireActivity().findViewById(R.id.login_section).setVisibility(View.VISIBLE);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .remove(ForgetPasswordFragment.this)
                                .commit();
                    } else {
                        Toast.makeText(getContext(), "寄送失敗，請確認信箱是否正確", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getContext(), "發生錯誤：" + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ForgetPassword", "API Error", t);
                }
            });
        });


        return view;
    }
}
