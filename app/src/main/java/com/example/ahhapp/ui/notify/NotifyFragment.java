package com.example.ahhapp.ui.notify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.ahhapp.R;

public class NotifyFragment extends Fragment {
    @Override
    // 當這個 Fragment 建立畫面時會呼叫這個方法
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將 fragment_notify.xml 佈局載入並顯示在畫面上
        View view = inflater.inflate(R.layout.fragment_notify, container, false);
        // 傳回這個畫面 (View 物件) 給系統顯示
        return view;
    }
}
