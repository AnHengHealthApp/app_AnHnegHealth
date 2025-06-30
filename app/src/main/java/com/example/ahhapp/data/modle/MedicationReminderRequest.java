package com.example.ahhapp.data.modle;

// POST 請求用的用藥提醒資料物件
public class MedicationReminderRequest {
    private String medication_name;
    private String dosage_time;
    private String dosage_condition;
    private String reminder_time;

    public MedicationReminderRequest(String medication_name, String dosage_time, String dosage_condition, String reminder_time) {
        this.medication_name = medication_name;
        this.dosage_time = dosage_time;
        this.dosage_condition = dosage_condition;
        this.reminder_time = reminder_time;
    }
}
