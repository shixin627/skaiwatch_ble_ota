import 'package:ble_ota/ota_model.dart';
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

  @override
  Future<void> createBond(String address) {
    // TODO: implement createBond
    throw UnimplementedError();
  }

  @override
  Future<void> setFile(OtaModel model) {
    // TODO: implement setFile
    throw UnimplementedError();
  }

  @override
  Future<bool> startDfuProcess(OtaModel model) {
    // TODO: implement startDfuProcess
    throw UnimplementedError();
  }
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
