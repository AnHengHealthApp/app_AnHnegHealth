# 安衡健康助手 App

安衡健康助手是一款專為慢性病（高血壓、高血糖）人群設計的健康管理 Android App，
提供健康數據記錄、趨勢分析、用藥提醒、錯誤回報等功能，並透過與後端 API 串接實現帳號登入、資料同步與通知等功能。

## 📱 App 簡介

- 平台：Android 8.0+
- 開發工具：Android Studio 2024.2.2 (Ladybug)
- 語言：Java
- UI 設計：Figma
- 圖表視覺化：MPAndroidChart

## ✨ 主要功能

### 前端功能

✅ 健康數據記錄
- 手動輸入血壓、血糖、心跳等數據。
- 畫面顯示趨勢圖表幫助使用者理解健康變化。

✅ 用藥提醒
- 新增/刪除用藥提醒。
- 系統透過本地排程與通知提醒使用者服藥。

✅ 帳號系統
- 註冊、登入、忘記密碼。
- 跨裝置資料同步。

✅ 錯誤回報
- 使用者可以提交錯誤描述，透過後端寄送給開發者。

✅ AI 健康助手（前端介面）
- 提供使用者與 AI 聊天的頁面與基本串接測試。

## 🔗 前後端 API 串接

前端 App 透過 HTTP 請求串接後端 RESTful API，進行以下操作：

### 認證
- `POST /auth/register` - 註冊
- `POST /auth/login` - 登入
- `POST /user/forgot-password-mail` - 忘記密碼

### 健康數據
- `GET /health/basic`、`POST /health/basic` - 取得/更新基本健康資訊
- `POST /health/vitals`、`GET /health/vitals` - 新增/查詢血壓與心跳紀錄
- `POST /health/bloodSugar`、`GET /health/bloodSugar` - 新增/查詢血糖紀錄

### 用藥提醒
- `POST /health/medication` - 新增用藥提醒
- `GET /health/medication` - 查詢用藥提醒
- `DELETE /health/medication/:id` - 刪除用藥提醒

### 錯誤回報
- `POST /report/issue` - 提交錯誤回報

## 📂 開發與設計架構

- 採用 Fragment + Navigation Component 管理多頁面切換。
- 使用 MPAndroidChart 呈現健康數據趨勢。
- 使用 Toast、Dialog 等元件展示與測試 API 回應。

## 🚀 快速開始

```bash
# Clone 專案
$ git clone <repository-url>

# 開啟 Android Studio 並選擇專案
# 編譯並執行於模擬器或 Android 實機
```

## 👨‍💻 貢獻者

| 姓名       | 角色                      |
|------------|---------------------------|
| 李忠翰     | 前端介面設計與實作、API 整合測試 |
| 鄭昕承     | 前後端 API 串接整合       |
| 王鈞霖     | 資料庫設計、後端開發      |
| 馬昱家     | AI 模組整合              |

---

本專案僅供學術用途，實際使用請依據專業醫療建議。
