package com.example.ahhapp.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ahhapp.R;
import com.example.ahhapp.data.modle.MedicationReminder;

import java.util.List;

// 用藥提醒列表的適配器（連接 RecyclerView）
public class MedicineReminderAdapter extends RecyclerView.Adapter<MedicineReminderAdapter.ReminderViewHolder> {

    private List<MedicationReminder> reminderList; // 儲存所有提醒資料
    private OnDeleteClickListener deleteClickListener; // 刪除事件監聽器

    // 刪除按鈕點擊的 callback interface
    public interface OnDeleteClickListener {
        void onDeleteClick(int reminderId); // 傳入被刪除提醒的 id
    }

    // 供外部設定刪除監聽器
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    // 建構子：初始化資料
    public MedicineReminderAdapter(List<MedicationReminder> reminderList) {
        this.reminderList = reminderList;
    }

    // 更新提醒資料（重新載入清單時使用）
    public void setData(List<MedicationReminder> newData) {
        this.reminderList = newData;
    }

    // 建立每一個 item 的 ViewHolder（對應 item layout）
    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    // 綁定每一筆資料到畫面
    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        MedicationReminder reminder = reminderList.get(position); // 取得目前 item

        // 設定顯示的內容
        String title = reminder.getMedication_name();
        String time = "用藥時間：" + reminder.getDosage_time();
        String reminderTime = "提醒：" + reminder.getReminder_time();

        holder.tvMedicineName.setText(title);
        holder.tvTime.setText(time);
        holder.tvReminderTime.setText(reminderTime);

        // 刪除按鈕的點擊事件
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(reminder.getId()); // 呼叫外部定義的刪除邏輯
            }
        });
    }

    // 回傳列表總數量
    @Override
    public int getItemCount() {
        return reminderList != null ? reminderList.size() : 0;
    }

    // 每一筆 item 對應的 UI 元件
    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        ImageView icPills, btnDelete;
        TextView tvMedicineName, tvTime, tvReminderTime;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            icPills = itemView.findViewById(R.id.icPills); // 藥品圖示
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName); // 藥名
            tvTime = itemView.findViewById(R.id.tvMedicineTime); // 服藥時間
            tvReminderTime = itemView.findViewById(R.id.tvReminderTime); // 提醒時間
            btnDelete = itemView.findViewById(R.id.btnDelete); // 刪除按鈕
        }
    }
}
