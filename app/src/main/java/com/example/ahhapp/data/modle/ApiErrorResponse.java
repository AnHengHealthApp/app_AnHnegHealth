package com.example.ahhapp.data.modle;

public class ApiErrorResponse {
    public String status;
    public Error error;

    public static class Error {
        public String code;
        public String message;
    }
}