import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'ble_ota_method_channel.dart';

abstract class BleOtaPlatform extends PlatformInterface {
  /// Constructs a BleOtaPlatform.
  BleOtaPlatform() : super(token: _token);

  static final Object _token = Object();

  static BleOtaPlatform _instance = MethodChannelBleOta();

  /// The default instance of [BleOtaPlatform] to use.
  ///
  /// Defaults to [MethodChannelBleOta].
  static BleOtaPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [BleOtaPlatform] when
  /// they register themselves.
  static set instance(BleOtaPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool> startDfuProcess() {
    throw UnimplementedError('startDfuProcess() has not been implemented.');
  }
}
