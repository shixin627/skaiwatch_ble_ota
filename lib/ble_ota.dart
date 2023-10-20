
import 'package:ble_ota/ota_model.dart';

import 'ble_ota_platform_interface.dart';

class BleOta {
  Future<String?> getPlatformVersion() {
    return BleOtaPlatform.instance.getPlatformVersion();
  }

  Future<void> createBond(String address) {
    return BleOtaPlatform.instance.createBond(address);
  }

  Future<void> setFile(OtaModel model) {
    return BleOtaPlatform.instance.setFile(model);
  }

  Future<bool> startDfuProcess(OtaModel model) {
    return BleOtaPlatform.instance.startDfuProcess(model);
  }
}
