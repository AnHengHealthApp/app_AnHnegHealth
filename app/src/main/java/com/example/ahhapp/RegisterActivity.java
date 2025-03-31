package com.example.ahhapp;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

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

        //註冊按鈕
        Button btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 顯示註冊成功的提示訊息
                Toast.makeText(RegisterActivity.this, "註冊完成", Toast.LENGTH_SHORT).show();

                // 關閉註冊頁，回到登入頁
                finish();
            }
        });
    }
}
