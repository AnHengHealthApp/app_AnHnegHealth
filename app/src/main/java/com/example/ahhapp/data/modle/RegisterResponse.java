package com.example.ahhapp.data.modle;

// API 回傳成功時會收到這個格式：包含訊息與 user_id
public class RegisterResponse {
    private String message;   // 回傳訊息，例如 "註冊成功"
    private int user_id;      // 新用戶的 ID

    public String getMessage() {
        return message;
    }

    public int getUser_id() {
        return user_id;
    }
}