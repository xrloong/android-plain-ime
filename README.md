# android-plain-input-method
Android 上的樸素輸入法

---

# 樸素輸入法 專案完整規劃文件

## 一、專案目標

「樸素輸入法」是一款 Android 平台的中文輸入法，初期支援倉頡，未來可擴充行列、嘸蝦米、大易、鄭碼等。強調統一的 UI/UX 標準、查表式輸入法邏輯、響應式設計與高品質自動化測試。專案不支援雲端同步或自訂詞庫。

---

## 二、元件劃分

1. **查表管理元件（TableManager）**  
   - 集中管理所有輸入法查表資料，支援熱切換與快取。
   - 僅支援 CIN 格式，於初始化時以背景執行緒載入資源並調用獨立 CIN parser。
   - 以 LiveData<sealed class> 通知 UI 層資料載入狀態，狀態包含 Loading、Success、Error（攜帶詳細錯誤訊息與重試建議）。
   - 提供 retry() 方法，僅支援手動重試。
   - 查詢 API 僅於 Success 狀態下可用。
2. **CIN parser（獨立模組）**  
   - 完全獨立於專案主架構，便於單元測試與重用。
   - 支援 CIN 格式常見語法（註解、%keyname、%chardef、%endkey 等）。
   - 解析後產生「字符→編碼」與「編碼→候選字」快取。
   - 詳細設計、錯誤訊息與重試建議結構另存於 docs/TableLoadState.md。
3. **輸入法邏輯層（InputMethodEngine）**  
   - 調用 TableManager 查詢字根/符號，處理輸入邏輯。
4. **鍵盤 UI 元件（KeyboardView, KeyButton, PopupWindow）**  
   - 負責鍵盤佈局、按鍵繪製、長按彈窗、響應式設計。
   - UI/UX 標準參考 Gboard，所有輸入法切換後體驗一致。
   - 根據 TableManager 狀態自動顯示 loading、錯誤提示，僅於 Error 狀態時顯示手動重試按鈕。
5. **IME 服務層（SimpleInputMethodService）**  
   - Android IME 入口，管理鍵盤 UI 與輸入法邏輯。
6. **資料層（TableData, CIN/資源檔）**  
   - 儲存各輸入法查表資料，建議版本控管，便於日後維護。
7. **單元測試（TableManagerTest, InputMethodEngineTest, CINParserTest 等）**  
   - 覆蓋查表、切換、查詢、CIN 解析等功能。
8. **自動化 UI 測試（Espresso：KeyboardUiTest, InputFlowUiTest 等）**  
   - 驗證鍵盤顯示、互動、長按彈窗、輸入流程。

---

## 三、架構設計

- 採用「資料-邏輯-介面」分層架構，UI/邏輯/資料分離。
- TableManager 以 LiveData<sealed class> 狀態通知 UI，並於背景執行緒載入 CIN 查表資料。
- TableManager 僅支援 CIN 格式，未來如需擴充其他格式可再調整。
- CIN parser 完全獨立，便於單元測試與重用。
- InputMethodEngine 根據當前輸入法，調用 TableManager 查詢。
- KeyboardView/KeyButton 負責 UI 呈現與互動，長按彈窗自動調整位置。
- SimpleInputMethodService 負責系統事件、鍵盤顯示與輸入法切換。
- 查表資料與 UI/邏輯完全解耦，便於未來擴充新輸入法。

---

## 四、分階段任務（每階段皆納入自動化 UI 測試）

### 0. 最小可運行輸入法架構（MVP）
- 建立 Android IME 服務，顯示簡單 QWERTY 鍵盤。
- 實作基本按鍵輸入（無查表、無長按、無切換）。
- 撰寫 Espresso UI 測試：驗證鍵盤顯示、按鍵點擊可輸入文字。

### 1. 基礎架構與倉頡輸入法（細分次階段）

#### 1.1 TableManager 與 CIN parser 架構建立
- 設計 TableManager 介面與資料快取結構。
- 實作獨立 CIN parser，支援 CIN 格式解析。
- TableManager 於背景執行緒載入 CIN 並以 LiveData<sealed class> 通知狀態。

#### 1.2 倉頡查表資料準備與驗證
- 準備並導入倉頡 CIN 查表檔。
- 撰寫單元測試，驗證 CIN parser 解析正確性與 TableManager 查詢功能。

#### 1.3 InputMethodEngine 串接查表邏輯
- 實作 InputMethodEngine，串接 TableManager 查詢候選字。
- 撰寫單元測試，驗證輸入邏輯與查表整合。

#### 1.4 QWERTY 鍵盤 UI 設計與整合
- 設計 QWERTY 鍵盤 UI，支援長按彈窗與標點符號。
- UI 層根據 TableManager 狀態顯示 loading、錯誤提示與手動重試。

#### 1.5 IME 服務整合與端到端驗證
- 整合 SimpleInputMethodService、UI 與 Engine，實現完整輸入流程。
- 撰寫 Espresso UI 測試，驗證查表輸入、標點符號、長按彈窗等端到端功能。

### 2. 多輸入法支援與 UI/UX 統一
- 新增行列、嘸蝦米、大易、鄭碼查表資料。
- 擴充 TableManager 與 Engine 支援多輸入法熱切換。
- 確保所有輸入法切換後 UI/UX 標準一致。
- 強化長按彈窗位置自動調整邏輯。
- 增加多裝置（手機/平板）與橫直向適配測試。
- 撰寫 Espresso UI 測試：驗證輸入法切換、不同輸入法下的輸入與彈窗行為。

### 3. 自動化 UI 測試與優化
- 擴充 Espresso UI 測試，覆蓋所有功能、裝置與方向。
- 優化查表效能與快取策略。
- 增加異常處理與錯誤提示。
- 撰寫完整開發與維護文件。
- 擴充單元與 UI 測試覆蓋率。
- 蒐集用戶回饋，持續優化 UI/UX。

---

## 五、進階考量

1. 每階段新功能皆需對應自動化 UI 測試，確保回歸測試與品質。
2. 查表資料建議版本控管，便於日後維護。
3. 不支援雲端同步或自訂詞庫，專注本地查表與輸入體驗。
4. TableManager 狀態設計、錯誤訊息與重試建議的結構與用途，請詳見 docs/TableLoadState.md。

---

## 六、附註

- 自動化 UI 測試統一採用 Espresso。
- UI/UX 標準參考 Gboard，所有輸入法切換後體驗一致。
- 查表元件支援熱切換，並有單元測試保障正確性。
- 專案分階段推進，確保每階段成果可驗證、易於維護與擴充。

---

*最後更新：2026-01-07*
