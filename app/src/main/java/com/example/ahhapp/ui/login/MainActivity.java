package com.example.ahhapp.ui.login;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ahhapp.ui.main.BoardActivity;
import com.example.ahhapp.R;
import com.example.ahhapp.ui.register.RegisterActivity;
import com.example.ahhapp.data.modle.LoginRequest;
import com.example.ahhapp.data.modle.LoginResponse;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    // 宣告兩個按鈕：登入與註冊
    private Button btnLogin,btnRegister;
    // 宣告忘記密碼
    private TextView tvForgotPassword;

    private static final int NOTIFICATION_PERMISSION_REQUEST = 1001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 設定畫面使用的 layout 為 activity_login.xml（登入頁面）
        setContentView(R.layout.activity_login);

        // 將 XML 中的按鈕元件連接到 Java 程式碼
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 取得使用者輸入的帳號和密碼（EditText）
                EditText etUsername = findViewById(R.id.et_account);
                EditText etPassword = findViewById(R.id.et_password);

                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // 將使用者輸入資料包成 LoginRequest 物件
                LoginRequest request = new LoginRequest(username, password);

                // 建立 API 呼叫物件
                ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
                Call<LoginResponse> call = apiService.login(request);

                // 呼叫 login API（非同步執行）
                call.enqueue(new Callback<LoginResponse>() {

                    // 如果伺服器有回應（不管成功或失敗都會進來這裡）
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful()) {
                            // 登入成功，從回應中取得 JWT token
                            String token = response.body().getToken();
                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            prefs.edit().putString("token", token).apply();
                            Log.d("Login", "登入成功，Token: " + token);

                            Toast.makeText(MainActivity.this, "登入成功", Toast.LENGTH_SHORT).show();

                            // 登入成功後跳轉首頁
                            Intent intent = new Intent(MainActivity.this, BoardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                // 解析錯誤訊息 JSON
                                String errorBody = response.errorBody().string();

                                // 將錯誤 JSON 轉成 JSONObject
                                JSONObject jsonObject = new JSONObject(errorBody);
                                String errorMessage = jsonObject.getJSONObject("error").getString("message");

                                // 顯示錯誤訊息給使用者
                                Toast.makeText(MainActivity.this, "登入失敗：" + errorMessage, Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                                // fallback 如果解析失敗就顯示狀態碼
                                Toast.makeText(MainActivity.this, "登入失敗：" + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    // 如果伺服器完全沒回應（網路錯誤、連不上）
                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });



        // 當使用者點擊「註冊」按鈕時
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳轉到註冊畫面 RegisterActivity
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //當點擊忘記密碼時
        tvForgotPassword.setOnClickListener(view -> {
            //顯示Fragment容器
            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

            //影藏原本登入區塊
            findViewById(R.id.login_section).setVisibility(View.GONE);
            // 切換到 ForgetPasswordFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ForgetPasswordFragment())
                    .commit();
        });
    }

    // Android 13 通知權限檢查
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }
    }

    // 處理使用者點選結果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已授權通知權限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未授權通知，提醒功能可能無法運作", Toast.LENGTH_LONG).show();
            }
        }
    }
}
