package com.example.ahhapp.data.modle;

public class BloodSugarRequest {
    private String measurement_date;
    private int measurement_context; // 0=空腹, 1=餐前, 2=餐後
    private double blood_sugar;

    public BloodSugarRequest(String measurement_date, int measurement_context, double blood_sugar) {
        this.measurement_date = measurement_date;
        this.measurement_context = measurement_context;
        this.blood_sugar = blood_sugar;
    }
}
