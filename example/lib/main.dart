import 'package:ble_ota/ota_file.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:ble_ota/ble_ota.dart';
import 'dart:async';
import 'package:flutter/services.dart' show rootBundle;
import 'package:path_provider/path_provider.dart';
import 'dart:io';

void main() {
  runApp(const MyApp());
}
String otaFileName = 'ImgPacketFile-d40f416a178a33658b3aced19feb1637.bin';
String otaFileAssetPath = 'asset/bin/$otaFileName';
Future<void> copyFileFromAssetsToCache(String assetPath) async {
  final ByteData data = await rootBundle.load(assetPath);
  final List<int> bytes = data.buffer.asUint8List();

  final String cacheDir = (await getTemporaryDirectory()).path;
  final String filePath = '$cacheDir/${assetPath.split('/').last}';
  print('[copyFileFromAssetsToCache]filePath: $filePath');

  File file = File(filePath);
  if (await file.exists()) {
    return;
  }
  await file.writeAsBytes(bytes, flush: true);
  print('[copyFileFromAssetsToCache]file: $file');
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _bleOtaPlugin = BleOta();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await _bleOtaPlugin.getPlatformVersion() ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }
  bool _isDfuInProgress = false;

  Future<void> setFile(String fileName) async {
    OtaFile otaFile = OtaFile(name: fileName);
    await _bleOtaPlugin.setFile(otaFile);
  }

  Future<void> startDfuProcess() async {
    _isDfuInProgress = await _bleOtaPlugin.startDfuProcess();
    if (_isDfuInProgress) {
      print('DFU process started successfully');
    } else {
      print('DFU process failed to start');
    }
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(useMaterial3: true),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin ble ota app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Running on: $_platformVersion\n'),
              ElevatedButton(
                onPressed: () => copyFileFromAssetsToCache(otaFileAssetPath),
                child: const Text('Query File'),
              ),
              ElevatedButton(
                onPressed: () => setFile(otaFileName),
                child: const Text('Set File'),
              ),
              if (_isDfuInProgress) const Text('DFU process in progress'),
              ElevatedButton(
                onPressed: startDfuProcess,
                child: const Text('Start DFU process'),
              )
            ],
          ),
        ),
      ),
    );
  }
}
