package com.example.ahhapp.ui.register;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ahhapp.data.modle.RegisterRequest;
import com.example.ahhapp.data.modle.RegisterResponse;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    // 宣告返回按鈕（圖示）
    private ImageView btnBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 設定畫面 layout 為 activity_register.xml
        setContentView(R.layout.activity_register);

        // 初始化輸入欄位
        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etDisplayName = findViewById(R.id.et_display_name);


        // 找到畫面中的返回按鈕（ImageView）
        btnBack = findViewById(R.id.btn_back);
        // 點擊返回按鈕時，結束目前的註冊頁面，回到前一個頁面（通常是 MainActivity 登入頁）
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();
            } // 關閉當前 Activity
        });

        //註冊按鈕
        Button btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 取得使用者輸入的資料
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String displayName = etDisplayName.getText().toString().trim();

                // 建立一個註冊請求物件
                RegisterRequest request = new RegisterRequest(username, password, email, displayName);

                // 取得 API 服務實體
                ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

                // 呼叫 register API，並傳入資料
                Call<RegisterResponse> call = apiService.register(request);

                // 非同步呼叫 API，並處理回應
                call.enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                        if (response.isSuccessful()) {
                            // 成功：顯示註冊成功
                            Toast.makeText(RegisterActivity.this, "註冊成功", Toast.LENGTH_SHORT).show();
                            finish(); // 回到登入畫面
                        } else {
                            // 有回應但失敗：例如帳號已存在
                            Log.d("Register", "Response code: " + response.code());
                            Log.d("Register", "Error body: " + response.errorBody());
                            Toast.makeText(RegisterActivity.this, "註冊失敗：" + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterResponse> call, Throwable t) {
                        // API 呼叫錯誤，例如沒有連線
                        Toast.makeText(RegisterActivity.this, "錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
