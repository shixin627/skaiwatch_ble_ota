import 'package:ble_ota/ota_model.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:ble_ota/ble_ota.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:io';

import 'screens/bluetooth_off_screen.dart';
import 'screens/scan_screen.dart';

const String otaFileName = 'watch.bin';
String otaFileAssetPath = 'asset/bin/$otaFileName';

void main() {
  runApp(const MyApp());
}

Future<File?> pickAndSaveFileToCache() async {
  try {
    FilePickerResult? result = await FilePicker.platform.pickFiles();

    if (result != null) {
      PlatformFile file = result.files.first;
      debugPrint('selected file: $file');
      String fileName = file.name;
      debugPrint('selected file name: $fileName');
      debugPrint('selected file path: ${file.path}');
      debugPrint('selected file size: ${file.size}');
      final targetFile = File(file.path!);
      Directory cacheDirectory = await getTemporaryDirectory();
      String filePath = '${cacheDirectory.path}/$fileName';
      debugPrint('filePath: $filePath');
      File newFile = await targetFile.copy(filePath);
      debugPrint('newFile: $newFile');
      debugPrint('newFile path: ${newFile.path}');
      debugPrint('newFile size: ${newFile.lengthSync()}');

      return newFile;
    } else {
      // User canceled the file picker
      return null;
    }
  } catch (e) {
    print("Error picking and saving file: $e");
    return null;
  }
}

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
  String _deviceAddress = "16:3a:7d:c4:ba:69";
  File? _otaFile;
  final TextEditingController _controller = TextEditingController();
  final RegExp _addressRegExp = RegExp(r'^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$');
  BluetoothAdapterState _adapterState = BluetoothAdapterState.unknown;
  late StreamSubscription<BluetoothAdapterState> _adapterStateStateSubscription;

  void _validateAddress(String value) {
    if (_addressRegExp.hasMatch(value)) {
      setState(() {
        _deviceAddress = value;
      });
    }
  }

  @override
  void initState() {
    super.initState();
    initPlatformState();
    _controller.text = _deviceAddress;
    // _adapterStateStateSubscription =
    //     FlutterBluePlus.adapterState.listen((state) {
    //   _adapterState = state;
    //   setState(() {});
    // });
  }

  @override
  void dispose() {
    // _adapterStateStateSubscription.cancel();
    super.dispose();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _bleOtaPlugin.getPlatformVersion() ??
          'Unknown platform version';
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

  Future<void> createBond(String deviceAddress) async {
    debugPrint('createBond deviceAddress: $deviceAddress');
    // await _bleOtaPlugin.createBond(deviceAddress);
  }

  Future<void> setFile(String fileName) async {
    OtaModel model = OtaModel(fileName: fileName);
    await _bleOtaPlugin.setFile(model);
  }

  Future<void> startDfuProcess(String deviceAddress, String fileName) async {
    OtaModel model = OtaModel(address: deviceAddress, fileName: fileName);
    debugPrint('startDfuProcess model: ${model.toJson()}');
    _isDfuInProgress = await _bleOtaPlugin.startDfuProcess(model);
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
              Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: TextFormField(
                      controller: _controller,
                      decoration: const InputDecoration(
                        border: OutlineInputBorder(),
                        labelText: 'Device Address',
                      ),
                      onChanged: _validateAddress,
                      keyboardType: TextInputType.text,
                      inputFormatters: [
                        LengthLimitingTextInputFormatter(17),
                        // Limit to 17 characters
                      ],
                    ),
                  ),
                  Text(
                    'Device Address: $_deviceAddress',
                    style: const TextStyle(fontSize: 18),
                  ),
                ],
              ),
              ElevatedButton(
                onPressed: () => createBond(_deviceAddress),
                child: const Text('Create Bond'),
              ),
              // ElevatedButton(
              //   onPressed: () {
              //     Widget screen = _adapterState == BluetoothAdapterState.on
              //         ? const ScanScreen()
              //         : BluetoothOffScreen(adapterState: _adapterState);
              //     Navigator.of(context).push(
              //       MaterialPageRoute(builder: (_) => screen),
              //     );
              //   },
              //   child: const Text('Scan for devices'),
              // ),
              // ElevatedButton(
              //   onPressed: () => copyFileFromAssetsToCache(otaFileAssetPath),
              //   child: const Text('Query File'),
              // ),
              ElevatedButton(
                onPressed: () async {
                  _otaFile = await pickAndSaveFileToCache();
                  setState(() {});
                },
                child: const Text('Pick File'),
              ),
              // ElevatedButton(
              //   onPressed: () => setFile(otaFileName),
              //   child: const Text('Set File'),
              // ),
              if (_otaFile != null)
                Text('File: ${_otaFile!.path.split('/').last}'),
              ElevatedButton(
                onPressed: () {
                  if (_otaFile != null) {
                    String fileName = _otaFile!.path.split('/').last;
                    debugPrint('fileName: $fileName');
                    if (fileName.isEmpty) {
                      debugPrint('fileName is empty');
                      return;
                    }
                    // debugPrint('_deviceAddress $_deviceAddress');
                    startDfuProcess(_deviceAddress, fileName);
                  }
                },
                child: const Text('Start DFU process'),
              ),
              if (_isDfuInProgress) const Text('DFU process in progress'),
            ],
          ),
        ),
      ),
      // navigatorObservers: [BluetoothAdapterStateObserver()],
    );
  }
}

class BluetoothAdapterStateObserver extends NavigatorObserver {
  StreamSubscription<BluetoothAdapterState>? _adapterStateSubscription;

  @override
  void didPush(Route route, Route? previousRoute) {
    super.didPush(route, previousRoute);
    if (route.settings.name == '/DeviceScreen') {
      // Start listening to Bluetooth state changes when a new route is pushed
      _adapterStateSubscription ??=
          FlutterBluePlus.adapterState.listen((state) {
        if (state != BluetoothAdapterState.on) {
          // Pop the current route if Bluetooth is off
          navigator?.pop();
        }
      });
    }
  }

  @override
  void didPop(Route route, Route? previousRoute) {
    super.didPop(route, previousRoute);
    // Cancel the subscription when the route is popped
    _adapterStateSubscription?.cancel();
    _adapterStateSubscription = null;
  }
}
