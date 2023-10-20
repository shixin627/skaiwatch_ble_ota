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

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.realsil.sdk.core.bluetooth.GlobalGatt
import com.realsil.sdk.core.bluetooth.scanner.LeScannerPresenter
import com.realsil.sdk.core.bluetooth.scanner.ScannerParams
import com.realsil.sdk.core.logger.ZLogger
import com.realsil.sdk.dfu.DfuConstants
import com.realsil.sdk.dfu.DfuException
import com.realsil.sdk.dfu.exception.LoadFileException
import com.realsil.sdk.dfu.image.FirmwareLoaderX
import com.realsil.sdk.dfu.image.LoadParams
import com.realsil.sdk.dfu.model.*
import com.skaiwalk.ble_ota.DfuHelperImpl
import com.skaiwalk.ble_ota.settings.SettingsHelper
import com.realsil.sdk.dfu.utils.ConnectParams
import com.realsil.sdk.dfu.utils.DfuAdapter
import com.realsil.sdk.dfu.utils.GattDfuAdapter
import com.skaiwalk.ble_ota.settings.AppSettingsHelper
import java.util.*

/**
 * OTA Over GATT
 * @author nat_zhang@realsil.com.cn
 */
class GattDfuModule : BaseBluetoothDfuModule<GattDfuAdapter>() {
    private lateinit var context: Context
    private var mScannerPresenter: LeScannerPresenter? = null
    private val mDfuHelperCallback = object : DfuAdapter.DfuHelperCallback() {
        override fun onStateChanged(state: Int) {
            super.onStateChanged(state)
            if (state == DfuAdapter.STATE_INIT_BINDING_SERVICE) {
            }
            if (state == DfuAdapter.STATE_INIT_OK) {
            } else if (state == DfuAdapter.STATE_PREPARED) {
                mOtaDeviceInfo = dfuAdapter.otaDeviceInfo
                sendMessage(mHandle, MSG_TARGET_INFO_CHANGED)
            } else if (state == DfuAdapter.STATE_DISCONNECTED || state == DfuAdapter.STATE_CONNECT_FAILED) {
                if (!isOtaProcessing) {
                    mOtaDeviceInfo = null
                    sendMessage(mHandle, MSG_TARGET_INFO_CHANGED)
                }
            }
        }

        override fun onError(type: Int, code: Int) {
            if (isOtaProcessing) {
                mOtaDeviceInfo = null
            }

            val message = DfuHelperImpl.parseError(context, type, code)
            if (type == DfuException.Type.CONNECTION) {
            } else {
            }
            notifyProcessStateChanged(STATE_ABORTED)
        }

        override fun onProcessStateChanged(state: Int, throughput: Throughput?) {
            super.onProcessStateChanged(state, throughput)
            mProcessState = state
            if (state == DfuConstants.PROGRESS_IMAGE_ACTIVE_SUCCESS) {

                mOtaDeviceInfo = null
                mBinInfo = null

                if (AppSettingsHelper.getInstance()!!.isDfuSuccessHintEnabled) {
                    notifyProcessStateChanged(STATE_OTA_SUCCESS)
                } else {
                    notifyProcessStateChanged(STATE_OTA_BANKLINK_PROCESSING)
                    setBankLinkEnbled(true)
                }
            } else if (state == DfuConstants.PROGRESS_PENDING_ACTIVE_IMAGE) {
                onPendingActiveImage()
            } else if (state == DfuConstants.PROGRESS_STARTED) {
            } else if (state == DfuConstants.PROGRESS_START_DFU_PROCESS) {
            } else {
            }
        }

        override fun onProgressChanged(dfuProgressInfo: DfuProgressInfo?) {
            super.onProgressChanged(dfuProgressInfo)
            if (mProcessState == DfuConstants.PROGRESS_START_DFU_PROCESS && dfuProgressInfo != null) {
            }
        }
    }

    override fun redirect2SettingsOptions() {
    }

    override fun getSettingsIndicator(): Int {
        return SettingsHelper.PREF_DFU or SettingsHelper.PREF_DFU_DEV or SettingsHelper.PREF_DFU_GATT
    }

    override fun refresh(forceLoad: Boolean) {
        try {
            refreshDeviceInfo()
            refreshBinInfo(forceLoad)
//            ZLogger.v("refresh mFilePath:$mFilePath")
            if (isOtaProcessing) {
            } else {
            }
        } catch (e: Exception) {
            ZLogger.w(e.toString())
        }
    }

    private fun refreshDeviceInfo() {
        if (mSelectedDevice != null) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            if (mOtaDeviceInfo != null) {
            } else {
            }
        } else {
        }
    }

    private fun refreshBinInfo(forceLoad: Boolean) {
        if (TextUtils.isEmpty(mFilePath)) {
            mBinInfo = null
            return
        }

        ZLogger.v(mFilePath)
        if (mBinInfo == null || forceLoad) {
            try {
                if (AppSettingsHelper.getInstance()!!.fileLocation == DfuConfig.FILE_LOCATION_ASSETS) {
                    val builder = LoadParams.Builder()
                        .with(context)
                        .setWorkMode(dfuConfig.otaWorkMode)
                        .fileLocation(DfuConfig.FILE_LOCATION_ASSETS)
                        .setFilePath(mFilePath)
                        .setFileSuffix(SettingsHelper.getInstance()!!.fileSuffix)
                        .setOtaDeviceInfo(mOtaDeviceInfo)
                        .setIcCheckEnabled(SettingsHelper.getInstance()!!.isDfuChipTypeCheckEnabled)
                        .setSectionSizeCheckEnabled(SettingsHelper.getInstance()!!.isDfuImageSectionSizeCheckEnabled)
                        .versionCheckEnabled(
                            SettingsHelper.getInstance()!!.isDfuVersionCheckEnabled,
                            SettingsHelper.getInstance()!!.dfuVersionCheckMode
                        )
                    mBinInfo = FirmwareLoaderX.loadImageBinInfo(builder.build())
                } else {
                    val builder = LoadParams.Builder()
                        .with(context)
                        .setWorkMode(dfuConfig.otaWorkMode)
                        .setFilePath(mFilePath)// Mandatory
                        .setFileSuffix(SettingsHelper.getInstance()!!.fileSuffix)//Optional
                        .setOtaDeviceInfo(mOtaDeviceInfo)// Recommend
                        .setIcCheckEnabled(SettingsHelper.getInstance()!!.isDfuChipTypeCheckEnabled)
                        .setSectionSizeCheckEnabled(SettingsHelper.getInstance()!!.isDfuImageSectionSizeCheckEnabled)
                        .versionCheckEnabled(
                            SettingsHelper.getInstance()!!.isDfuVersionCheckEnabled,
                            SettingsHelper.getInstance()!!.dfuVersionCheckMode
                        )
                    mBinInfo = FirmwareLoaderX.loadImageBinInfo(builder.build())
                }
            } catch (e: DfuException) {
                return
            }
        }

        if (mBinInfo != null) {
            if (mBinInfo.status == LoadFileException.SUCCESS) {
                if (mBinInfo.supportBinInputStreams != null && mBinInfo.supportBinInputStreams.size <= 0) {
                    mBinInfo = null
                } else {
                    for (stream in mBinInfo.supportBinInputStreams) {
                        ZLogger.v(stream.toString())
                    }
                    dfuConfig.filePath = mFilePath
                }
            } else {
                mBinInfo = null
            }
        } else {
            // load failed
        }
    }

    fun setFileName(activityContext: Context, fileName: String) {
        var packageName = activityContext.packageName
        mFilePath = "/data/user/0/${packageName}/cache/${fileName}"
        ZLogger.v("[setFileName]mFilePath:$mFilePath")
    }

    fun initialize(activityContext: Context) {
        context = activityContext
        setFileName(activityContext, "ImgPacketFile-d40f416a178a33658b3aced19feb1637")
//        if (!isBLESupported()) {
//            finish()
//        }

        // request to enable BT
//        if (!isBLEEnabled()) {
//            redirect2EnableBT()
//        } else {
//            initialize()
//        }
        dfuAdapter.initialize(mDfuHelperCallback)

        initScannerPresenter()
    }

    override fun getDfuAdapter(): GattDfuAdapter {
        if (mDfuAdapter == null) {
            mDfuAdapter = GattDfuAdapter.getInstance(context)
        }
        return mDfuAdapter
    }

    override fun onResume() {
        super.onResume()
        GlobalGatt.CLOSE_GATT_ENABLED =
            SettingsHelper.getInstance()!!.isDfuErrorActionCloseGattEnabled
    }

    fun onPause() {
        // stop the scan when close the activity
        mScannerPresenter?.stopScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        mScannerPresenter?.onDestroy()

        if (mDfuAdapter != null) {
            mDfuAdapter.abort()
            mDfuAdapter.close()
        }
    }

    override fun connectRemoteDevice(bluetoothDevice: BluetoothDevice, isHid: Boolean) {
        mSelectedDevice = bluetoothDevice
        mScannerPresenter?.stopScan()
        sendMessage(mHandle, MSG_CONNECTING_DEVICE)
        val connectParamsBuilder = ConnectParams.Builder()
            .address(mSelectedDevice.address)
            .reconnectTimes(SettingsHelper.getInstance()!!.dfuMaxReconnectTimes)
            .localName(dfuConfig.localName)
            .batteryValueFormat(ConnectParams.BATTERY_VALUE_F1)

        val otaServiceUuid = SettingsHelper.getInstance()!!.otaServiceUUID
        if (!TextUtils.isEmpty(otaServiceUuid)) {
            connectParamsBuilder.otaServiceUuid(UUID.fromString(otaServiceUuid))
        }
        val ret = dfuAdapter.connectDevice(connectParamsBuilder.build())
        if (!ret) {
            deviceConnected = false
            ZLogger.v("connectDevice failed")
        } else {
            deviceConnected = true
            ZLogger.v("connectDevice success")
        }
    }

    var deviceConnected = false

    private fun initScannerPresenter() {
        val scannerParams = ScannerParams(ScannerParams.SCAN_MODE_GATT)
        scannerParams.scanPeriod = (60 * 1000).toLong()
        if (mScannerPresenter == null) {
            mScannerPresenter = LeScannerPresenter(context, scannerParams, mScannerCallback)
        }
        mScannerPresenter?.setScannerParams(scannerParams)
    }

    override fun processBackconnect() {
        super.processBackconnect()
        val pairedDevices = mScannerPresenter!!.pairedDevices
        if (pairedDevices != null && pairedDevices.size > 0) {
            for (device in pairedDevices) {
                val bluetoothDevice = device.getDevice()
                if (bluetoothDevice != null && mSelectedDevice != null && bluetoothDevice.address == mSelectedDevice.address) {
                    if (!isOtaProcessing) {
//                        banklink
                        ZLogger.v("banklink paired device:" + bluetoothDevice.address)
                        connectRemoteDevice(bluetoothDevice, false)
                        return
                    }
                }
            }
        }
        ZLogger.v("wait acl connected")
        blockBankLink(6 * 1000)

        ZLogger.v("auto scan to connect")
        initScannerPresenter()
        mScannerPresenter?.startScan()
    }
    /**
     * Called when Select Target was pressed.
     */
    private fun selectTargetDevice() {
        //disconnect previious device, only hold one connection

        dfuAdapter.disconnect()
        setBankLinkEnbled(false)

        val scannerParams = ScannerParams(ScannerParams.SCAN_MODE_GATT)
        scannerParams.isNameNullable = false
        scannerParams.scanPeriod = (60 * 1000).toLong()
        //        scannerParams.setServiceParcelUuids(new ParcelUuid[]{new ParcelUuid(UUID.fromString("000005fd-3c17-d293-8e48-14fe2e4da212"))});
        //        scannerParams.setServiceUuids(new UUID[]{UUID.fromString("000005fd-3c17-d293-8e48-14fe2e4da212")});
        //        if (scannerParams.getServiceParcelUuids() != null) {
        //            ZLogger.d("scannerParams.getServiceParcelUuids().length=" + scannerParams.getServiceParcelUuids().length);
        //        } else {
        //            ZLogger.d("scannerParams.getServiceParcelUuids() == null");
        //        }

//        val otaServiceUuid = SettingsHelper.getInstance()!!.otaServiceUUID
//        if (!TextUtils.isEmpty(otaServiceUuid)) {
//            val scanFilters: MutableList<CompatScanFilter> = java.util.ArrayList()
//            scanFilters.add(
//                CompatScanFilter.Builder()
//                    .setServiceUuid(ParcelUuid.fromString(otaServiceUuid)).build())
//            scannerParams.scanFilters = scanFilters
//        }

//        val intent = Intent(this, LeScannerActivity::class.java)
//        intent.putExtra(ScannerActivity.EXTRA_KEY_SCAN_PARAMS, scannerParams)
//        intent.putExtra(ScannerActivity.EXTRA_KEY_SCAN_FILTER_ENABLED, true)
//        startBluetoothScanner(intent)
    }

    override fun configureDevOps() {
        super.configureDevOps()

        dfuConfig.activeImageDelayTime = SettingsHelper.getInstance()!!.dfuActiveImageDelayTime

        dfuConfig.connectionParameters = ConnectionParameters.Builder()
            .maxInterval(SettingsHelper.getInstance()!!.dfuConnectionParameterMaxInterval)
            .minInterval(SettingsHelper.getInstance()!!.dfuConnectionParameterMinInterval)
            .latency(SettingsHelper.getInstance()!!.dfuConnectionParameterLatency)
            .timeout(SettingsHelper.getInstance()!!.dfuConnectionParameterTimeout)
            .build()
    }

    fun checkFileContentAndStartOTA() {
        checkFileContent()
    }

    fun OtaDeviceInfo.test() {

    }

    companion object {
        private const val TAG = "DfuActivity"

        private const val D = true
    }

}
