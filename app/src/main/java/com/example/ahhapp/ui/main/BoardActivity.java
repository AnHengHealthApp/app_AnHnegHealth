package com.example.ahhapp.ui.main;

import android.os.Bundle;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.ahhapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class BoardActivity extends AppCompatActivity {

    private boolean isNotificationActive = true; // 初始為啟用狀態

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 取得 NavHostFragment（Navigation 的容器），負責控制 Fragment 切換
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // 透過 NavHostFragment 取得 NavController（導航控制器）
        NavController navController = navHostFragment.getNavController();

        // 找到底部導航欄的 View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //預設是選擇home鍵
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // 從 SharedPreferences 讀取通知狀態
        isNotificationActive = getNotificationPreference();
        updateNotificationIcon(bottomNavigationView);

        // 設定導覽按鈕點擊事件
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_notify) {
                // 點擊通知按鈕不跳頁面，只切換通知狀態
                toggleNotification(bottomNavigationView);
                return true;
            } else if (itemId == R.id.nav_home) {
                // 清除返回堆疊，回到首頁
                navController.popBackStack(R.id.nav_home, false);
                return NavigationUI.onNavDestinationSelected(item, navController);
            } else {
                // 其他按鈕正常導覽
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });
    }
    //改變「消息通知」按鈕的樣式
    private void toggleNotification(BottomNavigationView bottomNavigationView) {
        isNotificationActive = !isNotificationActive;

        // 更新 icon
        updateNotificationIcon(bottomNavigationView);

        // 儲存狀態到 SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("notifications_enabled", isNotificationActive).apply();

        // 顯示提示訊息
        String message = isNotificationActive ? "通知已開啟" : "通知已關閉";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // 更新通知圖示（依狀態顯示不同圖示）
    private void updateNotificationIcon(BottomNavigationView bottomNavigationView) {
        MenuItem notifyItem = bottomNavigationView.getMenu().findItem(R.id.nav_notify);
        if (isNotificationActive) {
            notifyItem.setIcon(R.drawable.ic_bell); // 通知開啟
        } else {
            notifyItem.setIcon(R.drawable.ic_cancel_notify); // 通知關閉
        }
    }

    // 取得目前通知是否啟用
    private boolean getNotificationPreference() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getBoolean("notifications_enabled", true); // 預設為 true
    }
}
