package com.example.ahhapp.data.modle;

public class UpdateUserProfileRequest {
    private String display_name;
    private String email;

    public UpdateUserProfileRequest(String display_name, String email) {
        this.display_name = display_name;
        this.email = email;
    }
}
