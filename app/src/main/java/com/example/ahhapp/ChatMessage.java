package com.example.ahhapp;

public class ChatMessage {
    private String message;
    private boolean isUser; // true 表示是使用者發的訊息，false 表示機器人

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }
}