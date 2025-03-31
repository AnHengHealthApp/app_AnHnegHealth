package com.example.ahhapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// 適配器類別，控制用藥提醒清單的顯示
public class MedicineReminderAdapter extends RecyclerView.Adapter<MedicineReminderAdapter.ReminderViewHolder> {

    private List<String> reminderList; // 儲存提醒資料（目前先用 String 作為假資料）

    // 建構子：傳入資料
    public MedicineReminderAdapter(List<String> reminderList) {
        this.reminderList = reminderList;
    }

    // 建立 ViewHolder（載入 item_medicine_reminder.xml）
    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    // 綁定每一筆資料到畫面上
    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        String medicineName = reminderList.get(position);
        holder.tvMedicineName.setText(medicineName); // 顯示藥物名稱

        // 刪除按鈕邏輯
        holder.btnDelete.setOnClickListener(v -> {
            reminderList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, reminderList.size());
        });
    }

    // 回傳項目數量
    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    // ViewHolder：對應 item 佈局元件
    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        ImageView icPills;       // 左側藥品圖片
        TextView tvMedicineName;
        ImageView btnDelete;    // 右側刪除按鈕

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            icPills = itemView.findViewById(R.id.icPills); // 左側圖片
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName); // 顯示名稱
            btnDelete = itemView.findViewById(R.id.btnDelete); // 刪除按鈕
        }
    }
}
