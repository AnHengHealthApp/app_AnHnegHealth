package com.example.ahhapp.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitClient {
    private static final String BASE_URL = "http://anhenghealth.ddns.net/api/v1/";
    private static Retrofit retrofit;

    private static OkHttpClient buildOkHttp() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // ★完整 Body
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(45, TimeUnit.SECONDS)   // 連線（TCP）逾時
                .writeTimeout(60, TimeUnit.SECONDS)     // 送出 request body 逾時
                .readTimeout(120, TimeUnit.SECONDS)      // 讀回應（AI計算時間）逾時 ↑↑
                .callTimeout(150, TimeUnit.SECONDS)      // 整個 call 的上限（可選）
                .retryOnConnectionFailure(true)         // 連線層重試
                .build();
    }

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient() // 寬鬆模式，避免小格式問題中斷
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(buildOkHttp())
                    .build();
        }
        return retrofit;
    }
}
