package com.example.ahhapp.data.modle;


public class BasicHealthInfoResponse {
    private String status;
    private String message;
    private BasicHealthData data;

    public BasicHealthData getData() {
        return data;
    }

    public static class BasicHealthData {
        private int health_id;
        private int user_id;
        private double height;
        private double weight;
        private String birthday;
        private Integer gender;

        public double getHeight() { return height; }
        public double getWeight() { return weight; }
        public String getBirthday() { return birthday; }
        public Integer getGender() { return gender; }
    }
}
