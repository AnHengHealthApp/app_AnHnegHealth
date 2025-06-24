package com.example.ahhapp.data.modle;


// 接收血壓紀錄 API 成功回傳的訊息
public class VitalsResponse {
    private String message;  // 回傳訊息，例如 "血壓紀錄已成功新增"

    public String getMessage() {
        return message;
    }
}