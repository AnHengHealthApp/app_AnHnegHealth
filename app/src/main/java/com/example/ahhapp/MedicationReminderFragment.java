package com.example.ahhapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MedicationReminderFragment extends Fragment{
    private RecyclerView recyclerView;
    private MedicineReminderAdapter adapter;
    private List<String> medicineList;
    private LinearLayout etProfile;


    // 空建構子
    public MedicationReminderFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_medication_reminder, container, false);

        //頭像按鈕邏輯
        view.findViewById(R.id.etProfile).setOnClickListener(v ->
                ProfileUtils.showEditProfileDialog(requireContext()));

        //綁定確定新增按鈕
        Button btnAddMedicine = view.findViewById(R.id.btnAddReminder);
        btnAddMedicine.setOnClickListener(v -> {
            Toast.makeText(getContext(), "已新增藥物提醒", Toast.LENGTH_SHORT).show();
        });

        // 假資料測試
        medicineList = new ArrayList<>();
        medicineList.add("降血壓藥");
        medicineList.add("糖尿病用藥");
        medicineList.add("膽固醇控制藥");

        // 設定 RecyclerView
        recyclerView = view.findViewById(R.id.rvReminderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicineReminderAdapter(medicineList);
        recyclerView.setAdapter(adapter);

        // 綁定返回按鈕，點擊時返回上一頁
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        return view;
    }
}