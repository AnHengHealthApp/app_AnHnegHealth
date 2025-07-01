package com.example.ahhapp.network;

import com.example.ahhapp.data.modle.RegisterRequest;
import com.example.ahhapp.data.modle.RegisterResponse;
import com.example.ahhapp.data.modle.LoginRequest;
import com.example.ahhapp.data.modle.LoginResponse;
import com.example.ahhapp.data.modle.VitalsRequest;
import com.example.ahhapp.data.modle.VitalsResponse;
import com.example.ahhapp.data.modle.UpdateProfileRequest;
import com.example.ahhapp.data.modle.UpdateProfileResponse;
import com.example.ahhapp.data.modle.BloodSugarRequest;
import com.example.ahhapp.data.modle.BloodSugarResponse;
import com.example.ahhapp.data.modle.ForgotPasswordRequest;
import com.example.ahhapp.data.modle.IssueReportRequest;
import com.example.ahhapp.data.modle.MedicationReminder;
import com.example.ahhapp.data.modle.MedicationReminderRequest;
import com.example.ahhapp.data.modle.MedicationReminderResponse;
import com.example.ahhapp.data.modle.GetBloodSugarResponse;
import com.example.ahhapp.data.modle.GetVitalsResponse;
import com.example.ahhapp.data.modle.BasicHealthInfoResponse;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.Path;


// Retrofit API 的定義介面
public interface ApiService {

    // 定義一個 POST 方法，對應伺服器的 /auth/register 路徑
    //register
    @Headers("Content-Type: application/json") // 指定內容類型為 JSON
    @POST("/api/v1/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
    //login
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    //edit blood pressure
    @POST("health/vitals")
    // 傳送血壓資料時，請求主體是 VitalsRequest，Header 帶 Authorization token
    Call<VitalsResponse> addVitals(@Body VitalsRequest request, @Header("Authorization") String authHeader);

    //edit blood sugar
    @POST("/api/v1/health/bloodSugar")
    Call<BloodSugarResponse> addBloodSugar(@Body BloodSugarRequest request, @Header("Authorization") String token);

    //更新基本資料
    @POST("/api/v1/health/basic")
    Call<UpdateProfileResponse> updateProfile(@Body UpdateProfileRequest request, @Header("Authorization") String token);

    //忘記密碼
    @POST("user/forgot-password-mail")
    Call<JsonObject> sendForgotPasswordEmail(@Body ForgotPasswordRequest request);

    //問題回報
    @POST("report/issue")
    Call<JsonObject> submitIssueReport( @Header("Authorization") String token, @Body IssueReportRequest request);

    //取得用藥提醒
    @GET("health/medication")
    Call<MedicationReminderResponse> getAllMedication(@Header("Authorization") String token);

    //新增用藥提醒
    @POST("health/medication")
    Call<JsonObject> addMedication(@Header("Authorization") String token, @Body MedicationReminderRequest request);

    //刪除用藥提醒
    @DELETE("health/medication/{id}")
    Call<JsonObject> deleteMedication(@Path("id") int id, @Header("Authorization") String token);

    //取得基本健康資料
    @GET("health/basic")
    Call<BasicHealthInfoResponse> getBasicHealthInfo(@Header("Authorization") String token);

    //取得血壓資料
    @GET("health/vitals")
    Call<GetVitalsResponse> getVitals(@Header("Authorization") String token);

    //取得血糖資料
    @GET("health/bloodSugar")
    Call<GetBloodSugarResponse> getBloodSugar(@Header("Authorization") String token);

    // @Body 表示這個參數會變成 JSON 傳到伺服器
}
