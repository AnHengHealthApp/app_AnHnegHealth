package com.example.ahhapp.network;

import com.example.ahhapp.data.modle.RegisterRequest;
import com.example.ahhapp.data.modle.RegisterResponse;
import com.example.ahhapp.data.modle.LoginRequest;
import com.example.ahhapp.data.modle.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

// Retrofit API 的定義介面
public interface ApiService {

    // 定義一個 POST 方法，對應伺服器的 /auth/register 路徑
    //regitster
    @Headers("Content-Type: application/json") // 指定內容類型為 JSON
    @POST("/api/v1/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
    //login
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    // @Body 表示這個參數會變成 JSON 傳到伺服器
}
