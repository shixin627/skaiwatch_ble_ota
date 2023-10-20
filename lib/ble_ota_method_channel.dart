import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'ble_ota_platform_interface.dart';
import 'ota_model.dart';

/// An implementation of [BleOtaPlatform] that uses method channels.
class MethodChannelBleOta extends BleOtaPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('ble_ota');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<void> createBond(String address) async {
    await methodChannel.invokeMethod<String>('createBond', <String, dynamic>{
      'address': address,
    });
  }

  @override
  Future<void> setFile(OtaModel model) async {
    await methodChannel.invokeMethod<String>('setFile', model.toJson());
  }

  @override
  Future<bool> startDfuProcess(OtaModel model) async {
    try {
      bool success =
          await methodChannel.invokeMethod('startDfuProcess', model.toJson());
      return success;
    } catch (e) {
      print('Error starting DFU process: $e');
      return false;
    }
  }
}
