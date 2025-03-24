package com.example.ahhapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // 定義兩種訊息類型：使用者發送、AI 回覆
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    private final Context context;
    private final ArrayList<ChatMessage> messages; // 存放訊息資料的列表

    // 建構子，初始化 context 和訊息資料
    public ChatAdapter(Context context, ArrayList<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    // 根據訊息的類型回傳不同的 viewType（讓系統知道該用哪個 layout）
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    // 創建對應類型的 ViewHolder（根據訊息是自己還是 AI）
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用者訊息使用 item_chat_user.xml
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            // AI 訊息使用 item_chat_bot.xml
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    // 將資料綁定到畫面（根據類型決定顯示的文字）
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position); // 取得當前訊息物件

        // 如果是使用者訊息
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).userMessage.setText(msg.getMessage());

            // 如果是 AI 訊息
        } else {
            ((BotViewHolder) holder).botMessage.setText(msg.getMessage());
        }
    }

    // 回傳列表長度（顯示幾筆訊息）
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // 自己訊息用的 ViewHolder，綁定 item_chat_user.xml 的元件
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.tvUserMessage); // 訊息文字元件
        }
    }

    // AI 訊息用的 ViewHolder，綁定 item_chat_bot.xml 的元件
    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView botMessage;
        ImageView ivBotAvatar;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botMessage = itemView.findViewById(R.id.tvBotMessage); // AI訊息文字
            ivBotAvatar = itemView.findViewById(R.id.ivBotAvatar); // AI 頭像
        }
    }
}
