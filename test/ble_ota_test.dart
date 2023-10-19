import 'package:flutter_test/flutter_test.dart';
import 'package:ble_ota/ble_ota.dart';
import 'package:ble_ota/ble_ota_platform_interface.dart';
import 'package:ble_ota/ble_ota_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockBleOtaPlatform
    with MockPlatformInterfaceMixin
    implements BleOtaPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final BleOtaPlatform initialPlatform = BleOtaPlatform.instance;

  test('$MethodChannelBleOta is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelBleOta>());
  });

  test('getPlatformVersion', () async {
    BleOta bleOtaPlugin = BleOta();
    MockBleOtaPlatform fakePlatform = MockBleOtaPlatform();
    BleOtaPlatform.instance = fakePlatform;

    expect(await bleOtaPlugin.getPlatformVersion(), '42');
  });
}
