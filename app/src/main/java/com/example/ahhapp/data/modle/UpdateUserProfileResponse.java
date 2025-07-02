package com.example.ahhapp.data.modle;


public class UpdateUserProfileResponse {

    private String status;
    private String message;
    private UserData data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public UserData getData() {
        return data;
    }

    public static class UserData {
        private int user_id;
        private String display_name;
        private String email;

        public int getUserId() {
            return user_id;
        }

        public String getDisplayName() {
            return display_name;
        }

        public String getEmail() {
            return email;
        }
    }
}