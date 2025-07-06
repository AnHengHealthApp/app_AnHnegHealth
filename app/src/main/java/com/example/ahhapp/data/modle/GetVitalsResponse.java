package com.example.ahhapp.data.modle;

import java.util.List;

public class GetVitalsResponse {
    private String status;
    private String message;
    private List<VitalsData> data;

    public List<VitalsData> getData() {
        return data;
    }

    public static class VitalsData {
        private int vital_id;
        private int user_id;
        private String measurement_date;
        private int heart_rate;
        private int systolic_pressure;
        private int diastolic_pressure;

        public int getHeartRate() { return heart_rate; }
        public int getSystolicPressure() { return systolic_pressure; }
        public int getDiastolicPressure() { return diastolic_pressure; }
        public String getMeasurementDate() { return measurement_date; }
    }
}