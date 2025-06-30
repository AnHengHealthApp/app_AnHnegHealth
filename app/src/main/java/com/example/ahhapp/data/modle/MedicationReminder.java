package com.example.ahhapp.data.modle;

import com.google.gson.annotations.SerializedName;
// 對應後端回傳的用藥提醒資料
public class MedicationReminder {
    @SerializedName("reminder_id")
    private int id;
    private String medication_name;
    private String dosage_time;
    private String dosage_condition;
    private String reminder_time;

    // Getter methods
    public int getId() {
        return id;
    }

    public String getMedication_name() {
        return medication_name;
    }

    public String getDosage_time() {
        return dosage_time;
    }

    public String getDosage_condition() {
        return dosage_condition;
    }

    public String getReminder_time() {
        return reminder_time;
    }
}