package com.example.ahhapp.ui.health;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.data.modle.UpdateProfileRequest;
import com.example.ahhapp.data.modle.UpdateProfileResponse;
import com.example.ahhapp.data.modle.BloodSugarRequest;
import com.example.ahhapp.data.modle.BloodSugarResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EditBloodSugarFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {

    public EditBloodSugarFragment() {
        // 空建構子（必要）
    }

    private LinearLayout etProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 載入 XML 畫面
        View view = inflater.inflate(R.layout.fragment_edit_blood_sugar, container, false);

        // 綁定頭像欄區塊並設定點擊事件
        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this); // 傳入當前 Fragment 作為 listener
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });

        // 綁定返回按鈕，點擊時返回上一頁
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());


        //點擊儲存(血糖)
        view.findViewById(R.id.btnSubmitSuger).setOnClickListener(v -> {
            EditText etEmpty = view.findViewById(R.id.etEmptyBloodSuger);
            EditText etFull = view.findViewById(R.id.etFullBloodSuger);

            String emptyText = etEmpty.getText().toString().trim();
            String fullText = etFull.getText().toString().trim();

            // 依據使用者填寫的欄位來上傳
            if (!emptyText.isEmpty()) sendBloodSugar(view, 0); // 空腹
            if (!fullText.isEmpty()) sendBloodSugar(view, 2);  // 餐後

            if (emptyText.isEmpty() && fullText.isEmpty()) {
                Toast.makeText(getContext(), "請至少填寫一項血糖數值", Toast.LENGTH_SHORT).show();
            }
        });

        //點及儲存(基本健康資料)
        view.findViewById(R.id.btnSubmitinfo).setOnClickListener(v -> updateProfileInfo(view));

        return view;
    }


    //上傳基本健康資料
    private void updateProfileInfo(View rootView) {
        EditText etHeight = rootView.findViewById(R.id.etHeight);
        EditText etWeight = rootView.findViewById(R.id.etWeight);
        EditText etBirthday = rootView.findViewById(R.id.etBirthday);
        EditText etGender = rootView.findViewById(R.id.etGender);

        int height = Integer.parseInt(etHeight.getText().toString().trim());
        int weight = Integer.parseInt(etWeight.getText().toString().trim());
        String birthday = etBirthday.getText().toString().trim();
        int gender = Integer.parseInt(etGender.getText().toString().trim());

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "尚未登入", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(height, weight, birthday, gender);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

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

    //上傳血糖資料 0= 空腹 1 = 餐後
    private void sendBloodSugar(View rootView, int context) {
        EditText etDate = rootView.findViewById(R.id.etDate);
        EditText etEmpty = rootView.findViewById(R.id.etEmptyBloodSuger);
        EditText etFull = rootView.findViewById(R.id.etFullBloodSuger);

        String date = etDate.getText().toString().trim();
        String sugarStr = (context == 0) ? etEmpty.getText().toString().trim() : etFull.getText().toString().trim();

        if (date.isEmpty() || sugarStr.isEmpty()) {
            Toast.makeText(getContext(), "請填寫日期與" + (context == 0 ? "空腹" : "餐後") + "血糖", Toast.LENGTH_SHORT).show();
            return;
        }

        double sugar;
        try {
            sugar = Double.parseDouble(sugarStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "血糖數值格式錯誤", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "尚未登入，無法傳送血糖", Toast.LENGTH_SHORT).show();
            return;
        }

        BloodSugarRequest request = new BloodSugarRequest(date, context, sugar);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<BloodSugarResponse> call = apiService.addBloodSugar(request, "Bearer " + token);

        call.enqueue(new Callback<BloodSugarResponse>() {
            @Override
            public void onResponse(Call<BloodSugarResponse> call, Response<BloodSugarResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), (context == 0 ? "空腹" : "餐後") + "血糖上傳成功", Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call<BloodSugarResponse> call, Throwable t) {
                Toast.makeText(getContext(), "血糖錯誤：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        Toast.makeText(getContext(), "資料已回傳！" ,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}