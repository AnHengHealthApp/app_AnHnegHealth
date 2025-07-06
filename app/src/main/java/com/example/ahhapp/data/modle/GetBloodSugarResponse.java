package com.example.ahhapp.data.modle;

import java.util.List;

public class GetBloodSugarResponse {
    private String status;
    private String message;
    private List<BloodSugarData> data;

    public List<BloodSugarData> getData() {
        return data;
    }

    public static class BloodSugarData {
        private int record_id;
        private int user_id;
        private String measurement_date;
        private int measurement_context;
        private double blood_sugar;

        public int getMeasurementContext() { return measurement_context; }
        public double getBloodSugar() { return blood_sugar; }
        public String getMeasurementDate(){ return measurement_date; }
    }
}
