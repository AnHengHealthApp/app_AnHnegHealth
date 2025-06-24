package com.example.ahhapp.data.modle;

// 傳送血壓紀錄用的資料模型（請求格式）
public class VitalsRequest {
    private String measurement_date;     // 測量時間（格式：yyyy-MM-dd HH:mm:ss）
    private int heart_rate;              // 心跳（每分鐘）
    private int systolic_pressure;       // 收縮壓
    private int diastolic_pressure;      // 舒張壓

    // 建構子：建立這個物件時要傳入全部欄位
    public VitalsRequest(String measurement_date, int heart_rate, int systolic_pressure, int diastolic_pressure) {
        this.measurement_date = measurement_date;
        this.heart_rate = heart_rate;
        this.systolic_pressure = systolic_pressure;
        this.diastolic_pressure = diastolic_pressure;
    }
}
