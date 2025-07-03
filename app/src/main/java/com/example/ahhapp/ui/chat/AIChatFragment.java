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

public class AIChatFragment extends  Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {
    private ArrayList<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;

    private TextView tvUsername;
    private ImageView ivUserPhoto;

    // 暫存
    private Bitmap cachedAvatar = null;
    private String cachedUsername = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);

        //初始化頭像列 UI 元件
        tvUsername = view.findViewById(R.id.tvUsername);
        ivUserPhoto = view.findViewById(R.id.ivUserPhoto);

        // 如果有快取資料先顯示
        if (cachedUsername != null) tvUsername.setText(cachedUsername);
        if (cachedAvatar != null) {
            ivUserPhoto.setImageBitmap(cachedAvatar);
        } else {
            ivUserPhoto.setImageResource(R.drawable.ic_user_photo);
        }

        // 載入使用者資料
        loadUserProfile();

        // 綁定頭像欄區塊並設定點擊事件
        view.findViewById(R.id.etProfile).setOnClickListener(v -> {
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.setListener(this); // 傳入當前 Fragment 作為 listener
            dialog.show(getParentFragmentManager(), "EditProfileDialog");
        });

        // RecyclerView 初始化
        RecyclerView recyclerChat = view.findViewById(R.id.recyclerChat);
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(getContext(), messageList);
        recyclerChat.setAdapter(chatAdapter);


        // 發送訊息按鈕
        EditText etMessage = view.findViewById(R.id.etMessage);
        ImageView btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                messageList.add(new ChatMessage(msg, true)); // 加入我方訊息
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerChat.scrollToPosition(messageList.size() - 1);
                etMessage.setText("");

                // 模擬的AI回應
                recyclerChat.postDelayed(() -> {
                    messageList.add(new ChatMessage("我有什麼能夠幫到您的嗎?", false)); // 假AI回應
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerChat.scrollToPosition(messageList.size() - 1);
                }, 500); // 模擬延遲 800 毫秒
            }
        });

        // 綁定返回按鈕，點擊時返回上一頁
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        return view;
    }

    private void navigateTo(int id) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(id);
    }

    // 更新頭像列資料
    private void loadUserProfile() {
        UserProfileManager.loadUserProfile(requireContext(), new UserProfileManager.OnProfileLoadedListener() {
            @Override
            public void onProfileLoaded(String username, Bitmap avatar) {
                cachedUsername = username;
                cachedAvatar = avatar;

                tvUsername.setText(username);
                if (avatar != null) {
                    ivUserPhoto.setImageBitmap(avatar);
                } else {
                    ivUserPhoto.setImageResource(R.drawable.ic_user_photo);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //更新資料後的toast
    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        // 不等後端回傳，直接更新顯示
        Toast.makeText(getContext(), "資料更新中...", Toast.LENGTH_SHORT).show();
        if (!newName.isEmpty()) {
            cachedUsername = newName;
            tvUsername.setText(newName);
        }
        if (imageUri != null) {
            ivUserPhoto.setImageURI(imageUri);
        }

        // 同時還是呼叫一次後端去刷新快取
        tvUsername.postDelayed(this::loadUserProfile, 2000);
    }

}
