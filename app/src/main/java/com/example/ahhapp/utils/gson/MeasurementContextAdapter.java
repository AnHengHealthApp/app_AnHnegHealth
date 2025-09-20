package com.example.ahhapp.utils.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

// 後端有時候回 "fasting"/"before_meal"/"after_meal"，也可能回 0/1/2
// 這個 Adapter 只負責把任一格式「安全」轉成 Integer(0/1/2)
public class MeasurementContextAdapter extends TypeAdapter<Integer> {

    @Override
    public void write(JsonWriter out, Integer value) throws java.io.IOException {
        if (value == null) { out.nullValue(); return; }
        out.value(value);
    }

    @Override
    public Integer read(JsonReader in) throws java.io.IOException {
        JsonToken token = in.peek();

        if (token == JsonToken.NULL) { in.nextNull(); return null; }

        if (token == JsonToken.NUMBER) {
            return in.nextInt();
        }

        if (token == JsonToken.STRING) {
            String s = in.nextString();
            if (s == null) return null;
            s = s.trim().toLowerCase();

            // 文字 → 代碼
            switch (s) {
                case "fasting":       // 空腹
                case "0":
                    return 0;
                case "before_meal":   // 餐前
                case "1":
                    return 1;
                case "after_meal":    // 餐後
                case "2":
                    return 2;
                default:
                    // 不認得就回 null，避免拋例外
                    return null;
            }
        }

        // 其他型別直接略過
        in.skipValue();
        return null;
    }
}
