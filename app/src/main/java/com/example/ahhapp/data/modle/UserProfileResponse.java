package com.example.ahhapp.data.modle;

import com.google.gson.annotations.SerializedName;
public class UserProfileResponse {

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
        @SerializedName("display_name")
        private String display_name;

        public String getDisplayName() {
            return display_name;
        }
    }
}