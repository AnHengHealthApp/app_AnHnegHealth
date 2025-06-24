package com.example.ahhapp.ui.health;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ahhapp.R;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.example.ahhapp.data.modle.VitalsRequest;
import com.example.ahhapp.data.modle.VitalsResponse;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.data.modle.UpdateProfileRequest;
import com.example.ahhapp.data.modle.UpdateProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EditBloodPressureFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {
    public EditBloodPressureFragment() {
    }

    private LinearLayout etProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 載入對應的畫面 XML
        View view = inflater.inflate(R.layout.fragment_edit_blood_pressure, container, false);

        // 綁定頭像欄區塊並設定點擊事件
        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this); // 傳入當前 Fragment 作為 listener
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });

        // 綁定返回按鈕
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // 返回上一個 Fragment（通常是 Home）
            Navigation.findNavController(v).popBackStack();
        });


        //點擊"血糖紀錄->"跳轉
        TextView tvGoToBloodSugar = view.findViewById(R.id.tvGoToBloodSugar);
        tvGoToBloodSugar.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.editBloodSugarFragment);
        });

        //點擊保存(基本健康資料)
        view.findViewById(R.id.btnSubmitinfo).setOnClickListener(v -> updateProfileInfo(view));
        //點擊保存(血壓)
        view.findViewById(R.id.btnSubmitBlood).setOnClickListener(v -> sendBloodPressure(view));

        return view;
    }

    //傳血壓資料
    private void sendBloodPressure(View rootView) {
        // 取得輸入欄位元件
        EditText etDate = rootView.findViewById(R.id.etDate);
        EditText etHeart = rootView.findViewById(R.id.etHeartRate);
        EditText etSys = rootView.findViewById(R.id.etSystolic);
        EditText etDia = rootView.findViewById(R.id.etDiastolic);

        //先以字串檢查是否有缺失值(防crash)
        String date = etDate.getText().toString().trim();
        String heartStr = etHeart.getText().toString().trim();
        String sysStr = etSys.getText().toString().trim();
        String diaStr = etDia.getText().toString().trim();

        // 空值檢查
        if (date.isEmpty() || heartStr.isEmpty() || sysStr.isEmpty() || diaStr.isEmpty()) {
            Toast.makeText(getContext(), "請完整填寫血壓資料", Toast.LENGTH_SHORT).show();
            return;
        }

        //將資料型態轉回Int
        int heart, sys, dia;
        try {
            heart = Integer.parseInt(heartStr);
            sys = Integer.parseInt(sysStr);
            dia = Integer.parseInt(diaStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "請輸入有效數字", Toast.LENGTH_SHORT).show();
            return;
        }

        // 取得儲存在本地的 JWT Token
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        // 如果沒有 token，代表尚未登入
        if (token == null) {
            Toast.makeText(getContext(), "尚未登入，無法傳送", Toast.LENGTH_SHORT).show();
            return;
        }

        // 建立血壓紀錄的請求物件
        VitalsRequest request = new VitalsRequest(date, heart, sys, dia);

        // 建立 Retrofit API 實體
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // 呼叫新增血壓 API，並帶入 token
        Call<VitalsResponse> call = apiService.addVitals(request, "Bearer " + token);
        call.enqueue(new Callback<VitalsResponse>() {
            @Override
            public void onResponse(Call<VitalsResponse> call, Response<VitalsResponse> response) {
                if (response.isSuccessful()) {
                    // 成功上傳
                    Toast.makeText(getContext(), "血壓紀錄成功", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(getContext(), "上傳失敗：" + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "上傳失敗，無法解析錯誤訊息", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<VitalsResponse> call, Throwable t) {
                // 無法連線等錯誤
                Toast.makeText(getContext(), "錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //傳基本健康資料
    private void updateProfileInfo(View rootView) {
        // 取得 EditText 欄位
        EditText etHeight = rootView.findViewById(R.id.etHeight);
        EditText etWeight = rootView.findViewById(R.id.etWeight);
        EditText etBirthday = rootView.findViewById(R.id.etBirthday);
        EditText etGender = rootView.findViewById(R.id.etGender);

        //將輸入資料轉為字串做檢查(防crash)
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String genderStr = etGender.getText().toString().trim();

        if (heightStr.isEmpty() || weightStr.isEmpty() || birthday.isEmpty() || genderStr.isEmpty()) {
            Toast.makeText(getContext(), "請完整填寫基本健康資料", Toast.LENGTH_SHORT).show();
            return;
        }

        //將資料轉回Int
        int height, weight, gender;
        try {
            height = Integer.parseInt(heightStr);
            weight = Integer.parseInt(weightStr);
            gender = Integer.parseInt(genderStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "請輸入有效數字（身高、體重、性別）", Toast.LENGTH_SHORT).show();
            return;
        }

        // 取得 token
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "尚未登入", Toast.LENGTH_SHORT).show();
            return;
        }

        // 建立請求物件
        UpdateProfileRequest request = new UpdateProfileRequest(height, weight, birthday, gender);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // 發送 API 請求
        Call<UpdateProfileResponse> call = apiService.updateProfile(request, "Bearer " + token);
        call.enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "基本資料更新成功", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        // 從伺服器的錯誤 body 擷取錯誤訊息
                        String errorBody = response.errorBody().string();

                        // 使用 Gson 解析錯誤 JSON
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(errorBody, JsonObject.class);
                        JsonObject errorObj = jsonObject.getAsJsonObject("error");
                        String errorMessage = errorObj.get("message").getAsString();

                        Toast.makeText(getContext(), "更新失敗：" + errorMessage, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "更新失敗，無法解析錯誤訊息", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                Toast.makeText(getContext(), "錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        Toast.makeText(getContext(), "資料已回傳！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
