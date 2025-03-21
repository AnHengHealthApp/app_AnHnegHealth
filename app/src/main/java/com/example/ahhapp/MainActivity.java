package com.example.ahhapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    // 宣告兩個按鈕：登入與註冊
    private Button btnLogin,btnRegister;
    // 宣告忘記密碼
    private TextView tvForgotPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 設定畫面使用的 layout 為 activity_login.xml（登入頁面）
        setContentView(R.layout.activity_login);

        // 將 XML 中的按鈕元件連接到 Java 程式碼
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // 當使用者點擊「登入」按鈕時
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            // 跳轉到主畫面 BoardActivity（通常是登入後的首頁）
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BoardActivity.class);
                startActivity(intent);
//                finish();
            }
        });

        // 當使用者點擊「註冊」按鈕時
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳轉到註冊畫面 RegisterActivity
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);

//                finish();
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


    /*private ImageButton btnHom
    e, btnNotify, btnProfile;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnHome = findViewById(R.id.btnHome);
        btnNotify = findViewById(R.id.btnNotify);
        btnProfile = findViewById(R.id.btnProfile);

        fragmentManager = getSupportFragmentManager();


        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNotify.setSelected(false);
                btnProfile.setSelected(false);
                btnHome.setSelected(true);
            }
        });

        btnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNotify.setSelected(true);
                btnProfile.setSelected(false);
                btnHome.setSelected(false);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNotify.setSelected(false);
                btnProfile.setSelected(true);
                btnHome.setSelected(false);
            }
        });
    }*/
}
