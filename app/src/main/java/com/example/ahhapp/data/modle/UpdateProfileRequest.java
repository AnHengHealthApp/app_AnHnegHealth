package com.example.ahhapp.data.modle;

public class UpdateProfileRequest {
    private int height;
    private int weight;
    private String birthday;
    private int gender;

    public UpdateProfileRequest(int height, int weight, String birthday, int gender) {
        this.height = height;
        this.weight = weight;
        this.birthday = birthday;
        this.gender = gender;
    }

    // 可加上 getter/setter 或讓 Gson 自動處理
}