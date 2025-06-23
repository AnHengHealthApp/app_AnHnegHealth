package com.example.ahhapp.data.modle;

// 用來打 API 時，傳送註冊資訊的資料模型
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String display_name;

    // 建構子，建立這個物件時就需要傳入這四個值
    public RegisterRequest(String username, String password, String email, String display_name) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.display_name = display_name;
    }
}