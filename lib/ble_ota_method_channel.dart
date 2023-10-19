import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'ble_ota_platform_interface.dart';

/// An implementation of [BleOtaPlatform] that uses method channels.
class MethodChannelBleOta extends BleOtaPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('ble_ota');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

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
}
