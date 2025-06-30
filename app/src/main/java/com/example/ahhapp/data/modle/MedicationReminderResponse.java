package com.example.ahhapp.data.modle;

import java.util.List;

public class MedicationReminderResponse {
    public String status;
    public String message;
    public List<MedicationReminder> data;  // 這裡對應的是後端的 data 陣列
}