package com.example.ahhapp.ui.chat;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ahhapp.adapter.ChatAdapter;
import com.example.ahhapp.data.modle.ChatMessage;
import com.example.ahhapp.R;
import com.example.ahhapp.ui.profile.EditProfileDialogFragment;

import java.util.ArrayList;

public class AIChatFragment extends  Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener {
    private ArrayList<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);

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

        // 假訊息測試
        messageList.add(new ChatMessage("我今天血壓比較高要怎麼辦？", true));
        messageList.add(new ChatMessage("最近天氣冷，血壓偏高是正常的，可以多觀察情緒。", false));
        messageList.add(new ChatMessage("我今天血壓比較高要怎麼辦？", true));
        messageList.add(new ChatMessage("最近天氣冷，血壓偏高是正常的，可以多觀察情緒。", false));
        messageList.add(new ChatMessage("我今天血壓比較高要怎麼辦？", true));
        messageList.add(new ChatMessage("最近天氣冷，血壓偏高是正常的，可以多觀察情緒。", false));
        messageList.add(new ChatMessage("我今天血壓比較高要怎麼辦？", true));
        messageList.add(new ChatMessage("最近天氣冷，血壓偏高是正常的，可以多觀察情緒。", false));
        chatAdapter.notifyDataSetChanged();

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
                // TODO: 等待串接 API 回覆 AI 回應訊息
            }
        });

        // 綁定返回按鈕，點擊時返回上一頁
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        return view;
    }

    @Override
    public void onProfileUpdated(String newName, String newEmail, Uri imageUri) {
        Toast.makeText(getContext(), "資料已回傳！" ,Toast.LENGTH_SHORT).show();
    }

}
