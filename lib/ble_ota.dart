
import 'ble_ota_platform_interface.dart';

class BleOta {
  Future<String?> getPlatformVersion() {
    return BleOtaPlatform.instance.getPlatformVersion();
  }

  Future<bool> startDfuProcess() {
    return BleOtaPlatform.instance.startDfuProcess();
  }
}
