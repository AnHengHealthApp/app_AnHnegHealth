package com.example.ahhapp.ui.chat;

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
import com.example.ahhapp.data.modle.ChatMessage;
import com.example.ahhapp.R;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;
import com.example.ahhapp.utils.UserProfileManager;

import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;

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

                        // (A) 只填入輸入框，讓使用者確認後再送
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

        // 3) 呼叫 API（此處用假呼叫），回來後替換內容
        fakeCall(new AiCallback() {
            @Override public void onSuccess(String aiText) {
                if (!isAdded()) return; // Fragment 已離開畫面
                chatAdapter.removeTypingIfExists();
                messageList.add(new ChatMessage(aiText, false));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerChat.scrollToPosition(messageList.size() - 1);
                if (btnSend != null) btnSend.setEnabled(true);
            }
            @Override public void onError(String err) {
                if (!isAdded()) return;
                chatAdapter.removeTypingIfExists();
                messageList.add(new ChatMessage("抱歉，連線太忙碌，請稍後再試。", false));
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
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-TW"); // 可改 Locale.getDefault()
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請開始說話…");
        try {
            voiceInputLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "裝置未安裝語音服務，請更新 Google App", Toast.LENGTH_LONG).show();
            }
        }
    }

    // === 模擬 API ===
    private interface AiCallback {
        void onSuccess(String aiText);
        void onError(String err);
    }

    private void fakeCall(AiCallback cb) {
        // 使用 recyclerChat 發送延遲 runnable；若視圖已被銷毀就不執行
        if (recyclerChat == null) return;
        recyclerChat.postDelayed(() -> {
            if (!isAdded() || recyclerChat == null) return;
            cb.onSuccess("我有什麼能夠幫到您的嗎？");
        }, 1200);
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
