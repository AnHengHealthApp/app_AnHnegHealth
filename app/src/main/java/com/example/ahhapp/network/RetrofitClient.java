package com.example.ahhapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// 建立一個 Retrofit 客戶端物件，只建立一次（Singleton）
public class RetrofitClient {
    // 換成你的後端伺服器 IP 或網址
    private static final String BASE_URL = "http://anhenghealth.ddns.net/api/v1/";

    private static Retrofit retrofit;

    // 回傳 Retrofit 實體（只有第一次會建立）
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // 設定伺服器網址
                    .addConverterFactory(GsonConverterFactory.create()) // 使用 Gson 轉換 JSON
                    .build();
        }
        return retrofit;
    }
}
