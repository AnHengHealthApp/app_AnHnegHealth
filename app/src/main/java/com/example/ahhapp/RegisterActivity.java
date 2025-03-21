package com.example.ahhapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    // 宣告返回按鈕（圖示）
    private ImageView btnBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 設定畫面 layout 為 activity_register.xml
        setContentView(R.layout.activity_register);

        // 找到畫面中的返回按鈕（ImageView）
        btnBack = findViewById(R.id.btn_back);
        // 點擊返回按鈕時，結束目前的註冊頁面，回到前一個頁面（通常是 MainActivity 登入頁）
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();
            } // 關閉當前 Activity
        });
    }
}
