package com.skaiwalk.ble_ota

import android.bluetooth.BluetoothManager
import android.content.Context
import com.realsil.sdk.core.RtkConfigure
import com.realsil.sdk.core.RtkCore
import com.realsil.sdk.core.bluetooth.RtkBluetoothManager
import com.realsil.sdk.core.logger.ZLogger
import com.realsil.sdk.dfu.RtkDfu
import com.skaiwalk.ble_ota.function.GattDfuModule
import com.skaiwalk.ble_ota.settings.AppSettingsHelper
import com.skaiwalk.ble_ota.settings.SettingsHelper
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** BleOtaPlugin */
class BleOtaPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var isRtkDfuInitialized: Boolean = false
    private var mGattDfuModule: GattDfuModule? = null
    private var mRtkBluetoothManager: RtkBluetoothManager? = null
    private var packageName: String? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "ble_ota")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        initRtkCore(context)
        if (isRtkDfuInitialized) {
            initGattDfuModule()
        }
        mGattDfuModule!!.onCreate()
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }

            "createBond" -> {
                var address = call.argument<String>("address")
                var byteArray = parseAddressToByteArray(address!!)
                createBondWithAddress(byteArray)
            }

            "setFile" -> {
                var name = call.argument<String>("name")
                mGattDfuModule!!.setFileName(context, name!!)
            }

            "startDfuProcess" -> {
                var address = call.argument<String>("address")
                var name = call.argument<String>("fileName")
                startDfuProcess(address!!, name!!)
                result.success(isRtkDfuInitialized)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun initRtkCore(context: Context) {
        val pid = android.os.Process.myPid()
        val isDebug = true

        // Mandatory, initialize rtk-core library
        // this: context
        // isDebug: true, switch on debug log; false, switch off debug log
        val configure = RtkConfigure.Builder()
            .debugEnabled(isDebug)
            .printLog(true)
            .logTag("OTA")
            .globalLogLevel(ZLogger.INFO)
            .build()
        RtkCore.initialize(context, configure)
        // Optional for demo
//        RtkSupport.initialize(context, true)
        // Optional, log related
//        RtkDebugger.initialize(
//            context, DebuggerConfigure.Builder()
//                .logTag("OTA")
//                .bugly("8fe5f9f85c").build()
//        )

        RtkBluetoothManager.initial(context)
        mRtkBluetoothManager = RtkBluetoothManager(context)
        RtkDfu.initialize(context, isDebug)
        AppSettingsHelper.initialize(context)
        SettingsHelper.initialize(context)
        isRtkDfuInitialized = true
        ZLogger.v(true, "RtkDfu initialized.")
    }

    private fun initGattDfuModule() {
        if (mGattDfuModule == null) {
            mGattDfuModule = GattDfuModule()
            mGattDfuModule!!.initialize(context)
        }
    }

    private fun parseAddressToByteArray(address: String): ByteArray {
        // split address with ':' to array
        var addressArray = address!!.split(":")
        // reverse array
        addressArray = addressArray.reversed()
        // convert to byte array
        var byteArray = ByteArray(6)
        for (i in addressArray.indices) {
            byteArray[i] = addressArray[i].toInt(16).toByte()
        }
        return byteArray
    }

    private fun createBondWithAddress(byteArray: ByteArray) {
        mRtkBluetoothManager!!.createBond(byteArray)
    }

    private fun startDfuProcess(address: String, name: String) {
        var byteArray = parseAddressToByteArray(address)
        byteArray.reverse()
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mGattDfuModule!!.mTargetDevice = bluetoothManager.adapter.getRemoteDevice(byteArray)
        ZLogger.d(true, "[startDfuProcess]get remote device: ${mGattDfuModule!!.mTargetDevice}")
        mGattDfuModule!!.connectRemoteDevice(mGattDfuModule!!.mTargetDevice!!, false)
        if (mGattDfuModule!!.deviceConnected) {
            mGattDfuModule!!.setFileName(context, name)
            ZLogger.d(true, "[startDfuProcess]checkFileContentAndStartOTA")
            mGattDfuModule!!.checkFileContentAndStartOTA()
        }
    }

//    private fun stopDfuProcess() {
//        mGattDfuModule!!.stopOTA()
//    }
}
