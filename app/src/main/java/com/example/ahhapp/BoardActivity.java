package com.example.ahhapp;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class BoardActivity extends AppCompatActivity {

    private boolean isNotificationActive = false;
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

        // 導覽頁監聽
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_notify) {
                //  如果是「消息通知」按鈕，則不跳轉，只改變樣式
                changeNotificationButtonStyle(bottomNavigationView);
                return true; // 阻止切換 Fragment
            } else {
                //  其他按鈕（首頁、資訊）則正常切換 Fragment
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });

    }
    //改變「消息通知」按鈕的樣式
    private void changeNotificationButtonStyle(BottomNavigationView bottomNavigationView) {
        MenuItem notifyItem = bottomNavigationView.getMenu().findItem(R.id.nav_notify);

        if (isNotificationActive) {
            // 如果是選中的狀態，切換回原本的圖標
            notifyItem.setIcon(R.drawable.ic_bell); // ⚡ 這裡換成你的原始圖標
        } else {
            // 如果是未選中狀態，切換成新的圖標
            notifyItem.setIcon(R.drawable.ic_cancel_notify); // ⚡ 這裡換成「已讀」或「亮起」的圖標
        }

        // 切換狀態
        isNotificationActive = !isNotificationActive;
    }
}
