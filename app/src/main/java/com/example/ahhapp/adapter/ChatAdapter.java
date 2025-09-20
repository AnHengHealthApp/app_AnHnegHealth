package com.example.ahhapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ahhapp.data.modle.ChatMessage;
import com.example.ahhapp.R;

import java.util.ArrayList;
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // 定義三種訊息類型：使用者發送、AI 回覆、等待AI回覆
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    private static final int VIEW_TYPE_TYPING = 3;

    // 內部用的標記
    private static final String MSG_TYPING_SENTINEL = "\uD83D\uDD04__TYPING__";

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
        ChatMessage m = messages.get(position);
        if (!m.isUser() && MSG_TYPING_SENTINEL.equals(m.getMessage())) {
            return VIEW_TYPE_TYPING; // typing 泡泡
        }
        return m.isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    // 創建對應類型的 ViewHolder（根據訊息是自己還是 AI）
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(context);
        // 使用者訊息使用 item_chat_user.xml
        if (viewType == VIEW_TYPE_USER) {
            View v = inf.inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(v);
        } else if (viewType == VIEW_TYPE_BOT) {
            View v = inf.inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(v);
        } else {
            View v = inf.inflate(R.layout.item_chat_typing, parent, false);
            return new TypingViewHolder(v);
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
        } else if (holder instanceof BotViewHolder) {
            ((BotViewHolder) holder).botMessage.setText(msg.getMessage());
            //如果等待AI訊息
        } else {
            // XML 內文字與 ProgressBar 已處理
        }
    }

    // 回傳列表長度（顯示幾筆訊息）
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // 方便 Fragment 呼叫：插入 / 移除 typing
    public void addTyping() {
        messages.add(new ChatMessage(MSG_TYPING_SENTINEL, /*isUser*/ false));
        notifyItemInserted(messages.size() - 1);
    }
    public void removeTypingIfExists() {
        int last = messages.size() - 1;
        if (last >= 0) {
            ChatMessage m = messages.get(last);
            if (!m.isUser() && MSG_TYPING_SENTINEL.equals(m.getMessage())) {
                messages.remove(last);
                notifyItemRemoved(last);
            }
        }
    }

    // 自己訊息用的 ViewHolder，綁定 item_chat_user.xml 的元件
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.tvUserMessage); // 訊息文字元件
        }
    }

    // AI 訊息用的 ViewHolder，綁定 item_chat_bot.xml 的元件
    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView botMessage;
        ImageView ivBotAvatar;
        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botMessage = itemView.findViewById(R.id.tvBotMessage); // AI訊息文字
            ivBotAvatar = itemView.findViewById(R.id.ivBotAvatar); // AI 頭像
        }
    }

    //將等待訊息綁定 item_chat_bot.xml 的元件
    static class TypingViewHolder extends RecyclerView.ViewHolder {
        TypingViewHolder(@NonNull View itemView) { super(itemView); }
    }
}
