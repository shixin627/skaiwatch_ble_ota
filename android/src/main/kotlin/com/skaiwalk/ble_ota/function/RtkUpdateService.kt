/*
 * Copyright (c) 2017-2022. Realtek Semiconductor Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skaiwalk.ble_ota.function

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import com.realsil.sdk.core.RtkConfigure
import com.realsil.sdk.core.RtkCore
import com.realsil.sdk.core.logger.ZLogger
import com.realsil.sdk.dfu.DfuConstants
import com.realsil.sdk.dfu.RtkDfu
import com.realsil.sdk.dfu.model.DfuConfig
import com.realsil.sdk.dfu.model.DfuProgressInfo
import com.realsil.sdk.dfu.model.Throughput
import com.skaiwalk.ble_ota.settings.SettingsHelper
import com.skaiwalk.ble_ota.settings.SettingsHelper.Companion.getInstance
import com.realsil.sdk.dfu.utils.ConnectParams
import com.realsil.sdk.dfu.utils.DfuAdapter
import com.realsil.sdk.dfu.utils.GattDfuAdapter
import com.realsil.sdk.support.compat.IntentCompat.getParcelableExtraCompat
import com.realtek.sdk.support.debugger.DebuggerSettings

/**
 * <pre> adb shell am startservice -n com.realsil.ota/com.realsil.ota.function.RtkUpdateService</pre>
 *
 * @author nat_zhang@realsil.com.cn
 */
class RtkUpdateService : Service() {
    private val stopSelfWhenOtaSuccess = true

    private var mDfuHelper: GattDfuAdapter? = null
    private var mDfuConfig: DfuConfig? = null
    private var mHandler: Handler? = null

    val dfuHelper: GattDfuAdapter?
        get() {
            if (mDfuHelper == null) {
                mDfuHelper = GattDfuAdapter.getInstance(this)
            }

            return mDfuHelper
        }

    protected val dfuConfig: DfuConfig
        get() {
            if (mDfuConfig == null) {
                mDfuConfig = DfuConfig()
            }
            return mDfuConfig as DfuConfig
        }

    private val mDfuHelperCallback = object : DfuAdapter.DfuHelperCallback() {
        override fun onStateChanged(state: Int) {
            super.onStateChanged(state)
            ZLogger.d(String.format(">> onStateChanged: 0x%04X", state))
            if (state == DfuAdapter.STATE_INIT_OK) {

                val ret = dfuHelper!!.connectDevice(
                    ConnectParams.Builder()
                        .address(dfuConfig.address).build()
                )
                if (!ret) {
                    ZLogger.w("connectDevice failed")
                }
            } else if (state == DfuAdapter.STATE_PREPARED) {
                //When state change to STATE_PREPARED, you can get OtaDeviceInfo
                //                mOtaDeviceInfo = getDfuHelper().getOtaDeviceInfo();
                val modeInfo =
                    dfuHelper!!.getPriorityWorkMode(DfuConstants.OTA_MODE_SILENT_FUNCTION)
                dfuConfig.otaWorkMode = modeInfo.workmode
                mHandler!!.sendEmptyMessage(MSG_START_OTA_PROCEDURE)
            }
        }

        override fun onError(type: Int, code: Int) {
            ZLogger.w("type=$type, code=$code")
//            val intent = Intent(BackgroundDfuActivity.ACTION_BACKGROUND_OTA_ERROR)
//            intent.putExtra(BackgroundDfuActivity.EXTRA_ERROR_TYPE, type)
//            intent.putExtra(BackgroundDfuActivity.EXTRA_ERROR_CODE, code)
//            sendBroadcast(intent)

            if (stopSelfWhenOtaSuccess) {
                stopSelf()
            }
        }

        override fun onProcessStateChanged(state: Int, throughput: Throughput?) {
            super.onProcessStateChanged(state, throughput)
            ZLogger.d(String.format("onProcessStateChanged: 0x%04X", state))
//            val intent = Intent(BackgroundDfuActivity.ACTION_BACKGROUND_OTA_PROGRESS_STATE_CHANGED)
//            intent.putExtra(BackgroundDfuActivity.EXTRA_PROGRESS_STATE, state)
//            sendBroadcast(intent)

            if (state == DfuConstants.PROGRESS_IMAGE_ACTIVE_SUCCESS) {
                if (stopSelfWhenOtaSuccess) {
                    stopSelf()
                }
            }
        }

        override fun onProgressChanged(dfuProgressInfo: DfuProgressInfo?) {
            super.onProgressChanged(dfuProgressInfo)
            if (dfuProgressInfo != null) {
                ZLogger.v(dfuProgressInfo.toString())
//                val intent = Intent(BackgroundDfuActivity.ACTION_BACKGROUND_OTA_PROGRESS_CHANGED)
//                intent.putExtra(BackgroundDfuActivity.EXTRA_PROGRESS, dfuProgressInfo)
//                sendBroadcast(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ZLogger.d("RtkUpdateService onCreate ++")

        //        ZLogger.initialize(TAG, true);
        SettingsHelper.initialize(this)
        //Mandatory
        val configure = RtkConfigure.Builder()
            .debugEnabled(true)
            .printLog(true)
            .logTag("OTA")
            .globalLogLevel(DebuggerSettings.getInstance()!!.debugLevel)
            .build()
        RtkCore.initialize(this, configure)
        RtkDfu.initialize(this, DebuggerSettings.getInstance()!!.isDebugEnabled)

        val thread = HandlerThread(TAG)
        thread.start()
        mHandler = object : Handler(thread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    MSG_START_OTA_PROCEDURE -> {
                        val ret = startOtaProcess()
                        if (!ret) {
                            ZLogger.w("startOtaProcess failed")
                        }
                    }
                    else -> {
                    }
                }
            }
        }
        ZLogger.d(TAG + "RtkUpdateService onCreate --")
    }

    override fun onStartCommand(intent: Intent?, i: Int, i1: Int): Int {
        //        super.onStartCommand(intent, i, i1);
        ZLogger.v("onStartCommand")

        if (intent != null) {
            mDfuConfig = intent.getParcelableExtraCompat(EXTRA_DFU_CONFIG, DfuConfig::class.java)
        }

        if (dfuHelper!!.state >= DfuAdapter.STATE_INIT_OK) {
            mDfuHelper!!.dfuAdapterCallback = mDfuHelperCallback
            if (dfuHelper!!.isIdle) {
                val ret = startOtaProcess()
                if (!ret) {
                    ZLogger.w("startOtaProcess failed")
                }
            } else {
                ZLogger.d("current ota state is busy")
            }
        } else {
            ZLogger.d("init DfuHelper object")
            val ret = dfuHelper!!.initialize(mDfuHelperCallback)
            ZLogger.d("init DfuHelper object: $ret")
        }

        //        showNotification("Realtek OTA", "Realtek OTA Service is running");
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        ZLogger.d("RtkUpdateService onDestroy")
        if (mDfuHelper != null) {
            mDfuHelper!!.abort()
            mDfuHelper!!.close()
        }
    }

    private fun startOtaProcess(): Boolean {
        //        getDfuConfig().setAddress(BT_ADDR);
        //        getDfuConfig().setFilePath(FILE_PATH);
        //        getDfuConfig().setOtaWorkMode(DfuConstants.OTA_MODE_SILENT_FUNCTION);

        dfuConfig.isBreakpointResumeEnabled =
            SettingsHelper.getInstance()!!.isDfuBreakpointResumeEnabled
        dfuConfig.isAutomaticActiveEnabled =
            SettingsHelper.getInstance()!!.isDfuAutomaticActiveEnabled
        dfuConfig.isBatteryCheckEnabled = SettingsHelper.getInstance()!!.isDfuBatteryCheckEnabled
        dfuConfig.lowBatteryThreshold = SettingsHelper.getInstance()!!.dfuLowBatteryThreshold
        dfuConfig.batteryLevelFormat = SettingsHelper.getInstance()!!.dfuBatteryLevelFormat
        dfuConfig.isVersionCheckEnabled = SettingsHelper.getInstance()!!.isDfuVersionCheckEnabled
        dfuConfig.versionCheckMode = getInstance()!!.dfuVersionCheckMode
        dfuConfig.isIcCheckEnabled = SettingsHelper.getInstance()!!.isDfuChipTypeCheckEnabled
        dfuConfig.isSectionSizeCheckEnabled =
            SettingsHelper.getInstance()!!.isDfuImageSectionSizeCheckEnabled
        dfuConfig.isThroughputEnabled = SettingsHelper.getInstance()!!.isDfuThroughputEnabled
        dfuConfig.isMtuUpdateEnabled = SettingsHelper.getInstance()!!.isDfuMtuUpdateEnabled
        dfuConfig.isConParamUpdateLatencyEnabled =
            SettingsHelper.getInstance()!!.isDfuConnectionParameterLatencyEnabled

        return dfuHelper!!.startOtaProcedure(dfuConfig)
    }

    companion object {

        private const val TAG = "RtkUpdateService"
        const val EXTRA_DFU_CONFIG = "DFU_CONFIG"

        private const val MSG_START_OTA_PROCEDURE = 0x01
    }

}
