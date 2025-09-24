package com.example.ahhapp.ui.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ahhapp.adapter.ChatAdapter;
import com.example.ahhapp.data.modle.AiChatRequest;
import com.example.ahhapp.data.modle.AiChatSuccess;
import com.example.ahhapp.data.modle.ApiErrorResponse;
import com.example.ahhapp.data.modle.ChatMessage;
import com.example.ahhapp.R;
import com.example.ahhapp.network.ApiService;
import com.example.ahhapp.network.RetrofitClient;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.example.ahhapp.utils.UserProfileManager;

import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

/**
 * AI 聊天頁 Fragment
 * - 支援：輸入訊息、Typing 泡泡、語音輸入 (RecognizerIntent)
 * - 重點：UI 元件升級成成員變數以避免 requireView() 崩潰；延遲呼叫加生命週期守門
 */
public class AIChatFragment extends Fragment
        implements EditProfileDialogFragment.OnProfileUpdatedListener {

    // === 狀態與 Adapter ===
    private final ArrayList<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;

    // === 主要 UI 參考（升級為成員變數，避免 requireView() 風險） ===
    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ImageView btnSend;

    // === 頭像列 ===
    private TextView tvUsername;
    private ImageView ivUserPhoto;

    // === 快取資料（避免每次重拉） ===
    private Bitmap cachedAvatar = null;
    private String cachedUsername = null;

    // === API service ===
    private ApiService api;

    // === 語音輸入：權限＆結果 Launcher ===
    private final ActivityResultLauncher<String> micPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startVoiceInput();
                } else {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "需要麥克風權限才能語音輸入", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> voiceInputLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> texts = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (texts != null && !texts.isEmpty()) {
                        String recognized = texts.get(0);
                        if (recognized != null) recognized = recognized.trim();
                        if (recognized == null || recognized.isEmpty()) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "沒有辨識到內容，請再試一次", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        // (A) 填入輸入框，讓使用者確認後再送
                        if (etMessage != null) {
                            etMessage.setText(recognized);
                            etMessage.setSelection(recognized.length());
                        }

                        // (B) 若想自動送出，改用：
                        // sendText(recognized);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);

        // === 初始化頭像列 ===
        tvUsername = view.findViewById(R.id.tvUsername);
        ivUserPhoto = view.findViewById(R.id.ivUserPhoto);
        if (cachedUsername != null) tvUsername.setText(cachedUsername);
        if (cachedAvatar != null) {
            ivUserPhoto.setImageBitmap(cachedAvatar);
        } else {
            ivUserPhoto.setImageResource(R.drawable.ic_user_photo);
        }
        // 非同步載入使用者資料
        loadUserProfile();

        // 點擊頭像列 -> 開啟編輯
        View editProfileEntry = view.findViewById(R.id.etProfile);
        if (editProfileEntry != null) {
            editProfileEntry.setOnClickListener(v -> {
                EditProfileDialogFragment dialog = new EditProfileDialogFragment();
                dialog.setListener(this);
                dialog.show(getParentFragmentManager(), "EditProfileDialog");
            });
        }

        // === RecyclerView 初始化 ===
        recyclerChat = view.findViewById(R.id.recyclerChat);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setStackFromEnd(true); // 新訊息靠底
        recyclerChat.setLayoutManager(lm);
        chatAdapter = new ChatAdapter(getContext(), messageList);
        recyclerChat.setAdapter(chatAdapter);

        // === Retrofit Service ===
        api = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // === 輸入與送出 ===
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (msg.isEmpty()) return;
            sendText(msg);
        });

        // === 麥克風 ===
        ImageView btnMic = view.findViewById(R.id.btnMic);
        btnMic.setOnClickListener(v -> {
            v.setEnabled(false);                 // 避免連點
            onMicClick();                        // 開啟語音輸入
            v.postDelayed(() -> v.setEnabled(true), 1500); // 1.5s 後恢復
        });

        // 返回鍵
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        return view;
    }

    // === 共用送出流程（按鈕/語音皆可調用） ===
    private void sendText(String msg) {
        // 1) 插入使用者訊息
        messageList.add(new ChatMessage(msg, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerChat.scrollToPosition(messageList.size() - 1);
        if (etMessage != null) etMessage.setText("");

        // 2) 顯示 Typing 泡泡
        chatAdapter.addTyping();
        recyclerChat.scrollToPosition(messageList.size() - 1);
        if (btnSend != null) btnSend.setEnabled(false);

        // 3) 呼叫 AI API
        callAi(msg, new AiCallback() {
            @Override public void onSuccess(String aiText) {
                if (!isAdded()) return;
                chatAdapter.removeTypingIfExists();
                messageList.add(new ChatMessage(aiText, false));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerChat.scrollToPosition(messageList.size() - 1);
                if (btnSend != null) btnSend.setEnabled(true);
            }
            @Override public void onError(String err) {
                if (!isAdded()) return;
                chatAdapter.removeTypingIfExists();
                messageList.add(new ChatMessage(err, false));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerChat.scrollToPosition(messageList.size() - 1);
                Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
                if (btnSend != null) btnSend.setEnabled(true);
            }
        });
    }

    // === 語音輸入 ===
    private void onMicClick() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceInput();
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-TW"); // 可改 Locale.getDefault(),預設中文
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請開始說話…");
        try {
            voiceInputLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "裝置未安裝語音服務，請更新 Google App", Toast.LENGTH_LONG).show();
            }
        }
    }

    // === AI API 呼叫 ===
    private interface AiCallback {
        void onSuccess(String aiText);
        void onError(String err);
    }

    private void callAi(String userMessage, AiCallback cb) {
        AiChatRequest body = new AiChatRequest(userMessage);
        String token = getToken(); // "Bearer xxx" 或空字串

        api.chatAi(token, body).enqueue(new Callback<AiChatSuccess>() {
            @Override
            public void onResponse(Call<AiChatSuccess> call, Response<AiChatSuccess> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    String text = response.body().data.response;
                    cb.onSuccess(text != null ? text : "");
                    return;
                }

                String friendly = mapError(response);
                cb.onError(friendly);
            }

            @Override
            public void onFailure(Call<AiChatSuccess> call, Throwable t) {
                if (!isAdded()) return;
                cb.onError("Network error. Please try again.");
            }
        });
    }

    /** 取出 Authorization header；換成你的實作（SharedPreferences/Session） */
    private String getToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            Toast.makeText(getContext(), "尚未登入", Toast.LENGTH_SHORT).show();
            return "";
        }
        return "Bearer " + token;
    }

    /** 錯誤碼提醒 */
    private String mapError(Response<?> resp) {
        int code = resp.code();
        String apiMsg = parseApiError(resp);

        if (code == 401) return "Unauthorized. Please sign in again.";
        if (code == 404) return apiMsg != null ? apiMsg : "Health data not found. Please complete your basic profile.";
        if (code == 408) return "AI server timeout. Please try again.";
        if (code == 400) return apiMsg != null ? apiMsg : "Invalid input.";
        if (code >= 500) return apiMsg != null ? apiMsg : "Server error. Please try later.";
        return apiMsg != null ? apiMsg : "Request failed. Please retry.";
    }

    /** 嘗試解析後端錯誤 body（ApiErrorResponse） */
    private @Nullable String parseApiError(Response<?> response) {
        try {
            if (response.errorBody() == null) return null;
            String json = response.errorBody().string();
            ApiErrorResponse err = new Gson().fromJson(json, ApiErrorResponse.class);
            if (err != null && err.error != null) {
                if (err.error.code != null && err.error.message != null) {
                    return err.error.code + ": " + err.error.message;
                }
                if (err.error.message != null) return err.error.message;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // === 導航（目前未使用，保留） ===
    private void navigateTo(int id) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(id);
    }

    // === 載入使用者資料（頭像列） ===
    private void loadUserProfile() {
        UserProfileManager.loadUserProfile(requireContext(), new UserProfileManager.OnProfileLoadedListener() {
            @Override
            public void onProfileLoaded(String username, Bitmap avatar) {
                cachedUsername = username;
                cachedAvatar = avatar;

                if (!isAdded()) return;
                tvUsername.setText(username);
                if (avatar != null) {
                    ivUserPhoto.setImageBitmap(avatar);
                } else {
                    ivUserPhoto.setImageResource(R.drawable.ic_user_photo);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // === 編輯個資回呼 ===
    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        if (!isAdded()) return;
        Toast.makeText(getContext(), "資料更新中...", Toast.LENGTH_SHORT).show();
        if (newName != null && !newName.isEmpty()) {
            cachedUsername = newName;
            tvUsername.setText(newName);
        }
        if (imageUri != null) {
            ivUserPhoto.setImageURI(imageUri);
        }
        tvUsername.postDelayed(this::loadUserProfile, 2000);
    }

    // === 清理：避免持有已被銷毀的 View 參考 ===
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerChat = null;
        etMessage = null;
        btnSend = null;
        tvUsername = null;
        ivUserPhoto = null;
    }
}
