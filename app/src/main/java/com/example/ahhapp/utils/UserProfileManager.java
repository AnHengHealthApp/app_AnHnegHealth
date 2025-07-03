package com.example.ahhapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.ahhapp.data.modle.UserProfileResponse;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;

import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileManager {
    public interface OnProfileLoadedListener {
        void onProfileLoaded(String username, Bitmap avatar);
        void onError(String errorMsg);
    }

    public static void loadUserProfile(Context context, OnProfileLoadedListener listener) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            listener.onError("尚未登入");
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // 取得username
        apiService.getUserProfile("Bearer " + token).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String display_name = response.body().getData().getDisplayName();
                    fetchAvatar(context, token, display_name, listener);
                } else {
                    listener.onError("取得個人資料失敗：" + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                listener.onError("錯誤：" + t.getMessage());
            }
        });
    }

    //取得頭像
    private static void fetchAvatar(Context context, String token, String username, OnProfileLoadedListener listener) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        apiService.getUserAvatar("Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        InputStream is = response.body().byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        listener.onProfileLoaded(username, bitmap);
                    } catch (Exception e) {
                        Log.e("UserProfileManager", "解析頭像失敗：" + e.getMessage(), e);
                        listener.onProfileLoaded(username, null);
                    }
                } else {
                    // 如果 404 (頭像不存在)，仍然回傳 username 並設 avatar = null
                    if (response.code() == 404) {
                        Log.w("UserProfileManager", "頭像不存在，僅顯示 username");
                        listener.onProfileLoaded(username, null);
                    } else {
                        listener.onError("取得頭像失敗：" + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.onError("頭像錯誤：" + t.getMessage());
            }
        });
    }
}
