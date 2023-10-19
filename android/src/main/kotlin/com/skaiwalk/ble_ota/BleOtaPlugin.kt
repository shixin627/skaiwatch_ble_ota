package com.skaiwalk.ble_ota

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.realsil.sdk.core.RtkConfigure
import com.realsil.sdk.core.RtkCore
import com.realsil.sdk.core.bluetooth.RtkBluetoothManager
import com.realsil.sdk.core.logger.ZLogger
import com.realsil.sdk.dfu.RtkDfu
import com.realsil.sdk.support.RtkSupport
import com.realtek.sdk.support.debugger.DebuggerConfigure
import com.realtek.sdk.support.debugger.RtkDebugger
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

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "ble_ota")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        initRtkCore(context)
        if (isRtkDfuInitialized) {
            intiGattDfuModule()
        }
        mGattDfuModule!!.onCreate()

    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
//                initDfuProcess()
            }

            "startDfuProcess" -> {
                if (mGattDfuModule!!.mBondedDevice != null) {
                    startDfuProcess()
                } else {
                    initDfuProcess()
                }
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

    private fun intiGattDfuModule() {
        if (mGattDfuModule == null) {
            mGattDfuModule = GattDfuModule()
            mGattDfuModule!!.onInit(context)
        }
    }

    private fun initDfuProcess() {
//                var targetDevice = call.argument<String>("targetDevice")
        var address = "16:3a:7d:c4:ba:69"
        var byteArray = ByteArray(6)
        byteArray[0] = 0x69
        byteArray[1] = 0xba.toByte()
        byteArray[2] = 0xc4.toByte()
        byteArray[3] = 0x7d
        byteArray[4] = 0x3a
        byteArray[5] = 0x16
        // ble device from address
        var paired =  mRtkBluetoothManager!!.pair(byteArray)
        if (paired) {
            ZLogger.d(true, "[startDfuProcess]paired")
        } else {
            ZLogger.d(true, "[startDfuProcess]not paired")
        }

    }

    private fun startDfuProcess() {
        ZLogger.d(true, "[startDfuProcess]startDfuProcess")
        if (mGattDfuModule!!.deviceConnected) {
            mGattDfuModule!!.checkFileContentAndStartOTA()
        } else {
            mGattDfuModule!!.connectRemoteDevice(mGattDfuModule!!.mBondedDevice, false)
        }
    }

//    private fun stopDfuProcess() {
//        mGattDfuModule!!.stopOTA()
//    }
}
