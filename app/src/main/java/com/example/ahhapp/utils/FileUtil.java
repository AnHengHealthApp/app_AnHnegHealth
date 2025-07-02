package com.example.ahhapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileUtil {
    public static MultipartBody.Part prepareFilePart(Context context, String partName, Uri fileUri) {
        try {
            // 取得輸入串流
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                Log.e("UploadDebug", "無法取得 inputStream");
                return null;
            }

            // 取得檔名與副檔名
            String fileName = getFileName(context, fileUri);
            String suffix = ".jpg";
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex != -1) {
                suffix = fileName.substring(dotIndex);
            }

            // 建立暫存檔案
            File tempFile = File.createTempFile("upload_", suffix, context.getCacheDir());

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            inputStream.close();
            outputStream.close();

            // 檢查檔案有效性
            if (!tempFile.exists() || tempFile.length() == 0) {
                Log.e("UploadDebug", "暫存圖片檔案不存在或為空");
                return null;
            }

            // 取得真實 MIME 類型
            String mimeType = context.getContentResolver().getType(fileUri);
            if (mimeType == null) {
                mimeType = "image/jpeg"; // fallback 預設值
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData(partName, fileName, requestFile);

            // Debug Log
            Log.d("UploadDebug", "準備上傳檔案：" + fileName);
            Log.d("UploadDebug", "MimeType: " + mimeType);
            Log.d("UploadDebug", "Uri: " + fileUri.toString());
            Log.d("UploadDebug", "暫存路徑：" + tempFile.getAbsolutePath());
            Log.d("UploadDebug", "檔案大小：" + tempFile.length() + " bytes");

            return body;

        } catch (Exception e) {
            Log.e("UploadDebug", "檔案處理失敗：" + e.getMessage(), e);
            return null;
        }
    }

    private static String getFileName(Context context, Uri uri) {
        String result = "image.jpg";
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                result = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return result;
    }
}
