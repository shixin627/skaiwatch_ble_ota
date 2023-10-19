
import 'package:ble_ota/ota_file.dart';

import 'ble_ota_platform_interface.dart';

class BleOta {
  Future<String?> getPlatformVersion() {
    return BleOtaPlatform.instance.getPlatformVersion();
  }

  Future<void> setFile(OtaFile otaFile) {
    return BleOtaPlatform.instance.setFile(otaFile);
  }

  Future<bool> startDfuProcess() {
    return BleOtaPlatform.instance.startDfuProcess();
  }
}
