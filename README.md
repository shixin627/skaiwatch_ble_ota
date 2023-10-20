# ble_ota
藍芽升級RTK芯片 BLE OTA
Flutter plugin project for Realtek RTL8762D DFU GATT OTA.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android.

For help getting started with Flutter development, view the
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

Android專案使用connectRemoteDevice函式建立與藍芽設備BluetoothDevice的GATT連線，並使用checkFileContent函式設定參數後進行藍芽無線升級：
- DfuConfig中設備位址、檔案路徑
- 以configureDevOps配置進階參數
    - getDfuConfig().setBatteryCheckEnabled(false)暫時關閉電池電量檢查
    - getDfuConfig().setVersionCheckEnabled(false)暫時關閉韌體版本檢查
- 以getDfuAdapter().startOtaProcedure(getDfuConfig(), mOtaDeviceInfo, true)啟動升級。
  This content is only supported in a Lark Docs
1. 建立專案
   flutter create --template=plugin --platforms=android -a kotlin ble_ota
2. 在BleOtaPlatform中新增函式
   Future<bool> startDfuProcess() {
   throw UnimplementedError('startDfuProcess() has not been implemented.');
   }
3. 使用Flutter EventChannel API methodChannel.invokeMethod，在MethodChannelBleOta模組中複寫函式
   @override
   Future<bool> startDfuProcess() async {
   try {
   bool success = await methodChannel.invokeMethod('startDfuProcess');
   return success;
   } catch (e) {
   print('Error starting DFU process: $e');
   return false;
   }
   }
4. 在BleOta模組新增函式
   Future<bool> startDfuProcess() {
   return BleOtaPlatform.instance.startDfuProcess();
   }
5. 在安卓專案android/src/main/java/<your_package_name>/ directory中BleOtaPlugin物件，建立對應事件startDfuProcess的接口函式
   when (call.method) {
   "getPlatformVersion" -> {
   result.success("Android ${android.os.Build.VERSION.RELEASE}")
   }
   "startDfuProcess" -> {
   result.success(true)
   }
   else -> {
   result.notImplemented()
   }
   }
6. 在主程式中創建BleOta實例，並建立ElevatedButton按鍵以呼叫startDfuProcess函式，若返回字串'DFU process started successfully'，表示此函式由Flutter API 順利發送至Android原生平台並成功返回消息。
   final _bleOtaPlugin = BleOta();
   Future<void> startDfuProcess() async {
   bool success = await _bleOtaPlugin.startDfuProcess();
   if (success) {
   print('DFU process started successfully');
   } else {
   print('DFU process failed to start');
   }
   }
   ElevatedButton(
   onPressed: startDfuProcess,
   child: const Text('Start DFU process'),
   ),
7. 適配與移植程式:
   1. 將Android專案複製，並刪減其中不需要出現於plugin內的UI元素，留下OTA服務功能（將原本的主程式GattDfuActivity改為非Activity的模組並導入BleOtaPlugin模組中）。
   2. 撰寫Android Plugin中對應flutter APIMethodChannel的onMethodCall接口函式
      override fun onMethodCall(call: MethodCall, result: Result) {
      when (call.method) {
      "startDfuProcess" -> {
      startDfuProcess()
      result.success(isRtkDfuInitialized)
      }
      else -> {
      result.notImplemented()
      }
      }
      }

Flutter API
- createBond: 建立綁定、選定設備
   - 參數: 藍芽設備位址
- setFile: 設定檔案
   - 參數: OTA檔案物件(包含名稱)
- startDfuProcess: 連線設備並啟動遠程升級