# 測試執行指南

## 執行所有測試

```bash
./gradlew test
```

## 執行特定測試類別

```bash
# CIN Parser 測試
./gradlew test --tests cin.CINParserTest

# TableManager 測試
./gradlew test --tests table.TableManagerTest
```

## 執行特定測試方法

```bash
# 測試 CIN 解析
./gradlew test --tests cin.CINParserTest.testParse_validCIN_basic

# 測試 TableManager 載入
./gradlew test --tests table.TableManagerTest.testLoadTable_success_updatesStateToSuccess
```

## 查看測試報告

測試報告位於：
- HTML 報告: `app/build/reports/tests/testDebugUnitTest/index.html`
- XML 報告: `app/build/test-results/testDebugUnitTest/`

## 重新執行測試 (忽略快取)

```bash
./gradlew test --rerun-tasks
```

## 測試覆蓋範圍

### CIN Parser 測試 (13 個測試)
- ✅ 基本解析功能
- ✅ 多候選字支援
- ✅ 註解與控制指令
- ✅ 錯誤處理
- ✅ 邊界情境

### TableManager 測試 (11 個測試)
- ✅ 狀態管理 (Loading/Success/Error)
- ✅ 背景載入
- ✅ 快取機制
- ✅ 查詢 API
- ✅ 錯誤處理與重試

## 測試資料

測試使用的倉頡 CIN 檔案位於：
- `app/src/test/resources/cangjie_test.cin`

包含約 60+ 個字符定義，涵蓋單字根與多字根組合。

