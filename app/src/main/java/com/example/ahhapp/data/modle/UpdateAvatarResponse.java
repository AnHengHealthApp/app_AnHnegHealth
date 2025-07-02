package com.example.ahhapp.data.modle;

public class UpdateAvatarResponse {

    private String status;
    private String message;
    private Data data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        private int user_id;

        public int getUserId() {
            return user_id;
        }
    }
}