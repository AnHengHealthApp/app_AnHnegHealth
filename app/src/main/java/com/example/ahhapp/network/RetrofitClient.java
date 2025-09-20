package com.example.ahhapp.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
