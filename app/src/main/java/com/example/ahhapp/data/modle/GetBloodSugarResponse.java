package com.example.ahhapp.data.modle;

import java.util.List;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.example.ahhapp.utils.gson.MeasurementContextAdapter;

public class GetBloodSugarResponse {
    private String status;
    private String message;
    private List<BloodSugarData> data;

    public List<BloodSugarData> getData() { return data; }

    public static class BloodSugarData {
        private Integer record_id;
        private Integer user_id;
        private String measurement_date;

        // ★ 只在這個欄位使用 Adapter，讓 "after_meal" 或 "2" 都能解析成 2
        @SerializedName("measurement_context")
        @JsonAdapter(MeasurementContextAdapter.class)
        private Integer measurement_context;

        // ★ 用包裝型別避免空值
        private Double blood_sugar;

        public Integer getMeasurementContext() { return measurement_context; }
        public Double getBloodSugar() { return blood_sugar; }
        public String getMeasurementDate(){ return measurement_date; }
    }
}
