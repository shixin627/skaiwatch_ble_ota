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

package com.skaiwalk.ble_ota.function;

import android.bluetooth.BluetoothDevice;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.skaiwalk.ble_ota.R;
import com.skaiwalk.ble_ota.settings.AppSettingsHelper;
import com.realsil.sdk.core.bluetooth.GlobalGatt;
import com.realsil.sdk.core.bluetooth.RtkBluetoothManager;
import com.realsil.sdk.core.bluetooth.RtkBluetoothManagerCallback;
import com.realsil.sdk.core.bluetooth.scanner.ExtendedBluetoothDevice;
import com.realsil.sdk.core.bluetooth.scanner.ScannerCallback;
import com.realsil.sdk.core.bluetooth.scanner.SpecScanRecord;
import com.realsil.sdk.core.bluetooth.utils.BluetoothUuid;
import com.realsil.sdk.core.logger.ZLogger;
import com.realsil.sdk.core.utility.DataConverter;
import com.realsil.sdk.dfu.DfuConstants;
import com.realsil.sdk.dfu.image.BinIndicator;
import com.realsil.sdk.dfu.model.DfuConfig;
import com.realsil.sdk.dfu.model.FileTypeInfo;
import com.realsil.sdk.dfu.model.OtaDeviceInfo;
import com.realsil.sdk.dfu.model.OtaModeInfo;
import com.skaiwalk.ble_ota.IOtaListener;
import com.skaiwalk.ble_ota.settings.SettingsHelper;
import com.realsil.sdk.dfu.utils.BluetoothDfuAdapter;
import com.realtek.sdk.support.debugger.DebuggerSettings;
import com.realtek.sdk.support.debugger.WriteLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author nat_zhang@realsil.com.cn
 */
public abstract class BaseBluetoothDfuModule<T extends BluetoothDfuAdapter> extends BaseDfuModule
        implements IOtaListener {

    private static final boolean D = true;

    public static final int CONNECT_TIME_OUT = 2 * 60 * 1000;

    public static final int STATE_INIT = 0x0000;
    public static final int STATE_INIT_OK = 0x0001;
    public static final int STATE_DEVICE_CONNECTING = 0x0100;
    public static final int STATE_DEVICE_PREPARED = 0x0200;
    public static final int STATE_OTA_PROCESSING = 0x0400;
    public static final int STATE_ABORTED = 0x0800;
    public static final int STATE_OTA_SUCCESS = STATE_ABORTED | 0x01;
    public static final int STATE_OTA_ERROR = STATE_ABORTED | 0x02;
    public static final int STATE_OTA_BANKLINK_PROCESSING = STATE_ABORTED | 0x03;

    protected int mState = STATE_INIT;

    boolean versionCheckEnabled = false;
    int versionCheckMode = 0;

    protected void notifyProcessStateChanged(int state) {
        ZLogger.v(String.format(Locale.US, "mstate= 0x%04X >> 0x%04X", mState, state));
        mState = state;
        sendMessage(mHandle, MSG_PROCESS_STATE_CHANGED);
    }

    @Override
    public boolean isOtaProcessing() {
        return (mState & STATE_OTA_PROCESSING) == STATE_OTA_PROCESSING;
    }

    protected static final int MSG_CONNECTING_DEVICE = 0x01;
    protected static final int MSG_PROCESS_STATE_CHANGED = 0x02;
    protected static final int MSG_TARGET_INFO_CHANGED = 0x03;
    protected static final int MSG_TARGET_ERROR = 0x04;


    protected Handler mHandle = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_TARGET_INFO_CHANGED:
                    mBinInfo = null;
                    refresh();

                    if (!isOtaProcessing() && mOtaDeviceInfo != null) {
                        selectWorkMode(false);
                    }
                    break;
                case MSG_PROCESS_STATE_CHANGED:
                    refresh();
                    break;
                case MSG_CONNECTING_DEVICE:
                    if (mSelectedDevice != null) {
                        // R.string.rtkbt_ota_connect_device, mSelectedDevice.getAddress()
                        ZLogger.v("[MSG_CONNECTING_DEVICE]Connecting device: " + mSelectedDevice.getAddress());
                    }
                    refresh();
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    protected T mDfuAdapter;
    protected OtaDeviceInfo mOtaDeviceInfo;
    protected int mProcessState;

    protected BluetoothDevice mSelectedDevice;
    public BluetoothDevice mBondedDevice;

    protected Object mScanLock = new Object();

    protected void notifyScanLock() {
        synchronized (mScanLock) {
            try {
                mScanLock.notifyAll();
            } catch (Exception e) {
                ZLogger.w(e.toString());
            }
        }
    }

    protected ScannerCallback mScannerCallback = new ScannerCallback() {
        @Override
        public void onNewDevice(final ExtendedBluetoothDevice device) {
            super.onNewDevice(device);

            if (device != null) {
                BluetoothDevice btDevice = device.getDevice();
//                ZLogger.d(">> " + device.toString());
                if (btDevice != null && mSelectedDevice != null && btDevice.getAddress().equals(mSelectedDevice.getAddress())) {
                    if (AppSettingsHelper.Companion.getInstance().isDfuBankLinkEnabled() && !isOtaProcessing()) {
                        ZLogger.v("bankLink: " + btDevice);
                        connectRemoteDevice(btDevice, false);
                    }
                    notifyScanLock();
                }
            }
        }

        @Override
        public void onScanStateChanged(int state) {
            super.onScanStateChanged(state);
            notifyScanLock();
        }
    };

    /**
     * create a DfuAdapter
     *
     * @return
     */
    public abstract T getDfuAdapter();

    public void onCreate() {
        versionCheckEnabled = SettingsHelper.getInstance().isDfuVersionCheckEnabled();
        versionCheckMode = SettingsHelper.getInstance().getDfuVersionCheckMode();
        ZLogger.v(String.format("versionCheckEnabled=%b, versionCheckMode=%d", versionCheckEnabled, versionCheckMode));
        RtkBluetoothManager.getInstance().addManagerCallback(mBluetoothManagerCallback);
    }

    private final RtkBluetoothManagerCallback mBluetoothManagerCallback = new RtkBluetoothManagerCallback() {
        @Override
        public void onBleAclConnectionStateChanged(BluetoothDevice bluetoothDevice, boolean connected) {
            super.onBleAclConnectionStateChanged(bluetoothDevice, connected);
            if (bluetoothDevice != null && bluetoothDevice.equals(mSelectedDevice)) {
                ZLogger.v(
                        String.format(
                                "onBleAclConnectionStateChanged, isOtaProcessing=%b",
                                isOtaProcessing()
                        )
                );
                if (!isOtaProcessing()) {
                    notifyBankLink();
                }
            } else {
                ZLogger.v("onBleAclConnectionStateChanged, device is null or not equal");
            }
        }

        @Override
        public void onAclConnectionStateChanged(BluetoothDevice bluetoothDevice, boolean connected) {
            super.onAclConnectionStateChanged(bluetoothDevice, connected);
            if (bluetoothDevice != null && bluetoothDevice.equals(mSelectedDevice)) {
                ZLogger.v(
                        String.format(
                                "onBleAclConnectionStateChanged, isOtaProcessing=%b",
                                isOtaProcessing()
                        )
                );
                if (!isOtaProcessing()) {
                    notifyBankLink();
                }
            }
        }
        @Override
        public void onBondStateChanged(BluetoothDevice var1, int var2) {
            super.onBondStateChanged(var1, var2);
            ZLogger.v("onBondStateChanged: " + var1 + ", " + var2);
            mBondedDevice = var1;
        }
        @Override
        public void onBluetoothStateChanged(int i) {
            super.onBluetoothStateChanged(i);
        }

    };

    public void onResume() {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
//        if (!isBLEEnabled()) {
//            redirect2EnableBT();
//        }

        boolean needForceRefresh = false;
        if (versionCheckEnabled != SettingsHelper.getInstance().isDfuVersionCheckEnabled()) {
            needForceRefresh = true;
        } else {
            if (versionCheckMode != SettingsHelper.getInstance().getDfuVersionCheckMode()) {
                needForceRefresh = true;
            }
        }
        versionCheckEnabled = SettingsHelper.getInstance().isDfuVersionCheckEnabled();
        versionCheckMode = SettingsHelper.getInstance().getDfuVersionCheckMode();

        refresh(needForceRefresh);
    }


    public void onDestroy() {
        WriteLog.getInstance().stopLog();
    }

    public void onBackPressed() {
        if (isOtaProcessing()) {
        } else {
        }
    }

    public void onEnableBluetoothCompleted(boolean enabled) {
        // When the request to enable Bluetooth returns
        if (enabled) {
            //do nothing
            initialize();
        } else {
            // User did not enable Bluetooth or an error occured
//            finish();
        }
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
    }

    public void redirect2SettingsOptions() {
    }

    public int getSettingsIndicator() {
        return SettingsHelper.PREF_DFU | SettingsHelper.PREF_DFU_DEV;
    }

    @Override
    public void initialize() {
//        initBbluetooth();
//        if (!isBLEEnabled()) {
//            redirect2EnableBT();
//        }
    }

    protected void checkFileContent() {
        if (!AppSettingsHelper.Companion.getInstance().isUploadFilePromptEnabled()) {
            getDfuConfig().setFileIndicator(BinIndicator.INDICATOR_FULL);
            startOtaProcess();
            return;
        }

        final List<FileTypeInfo> fileContentTypeInfos = BluetoothDfuAdapter.getSupportedFileContents(mBinInfo);
        if (fileContentTypeInfos.size() <= 0) {
            getDfuConfig().setFileIndicator(BinIndicator.INDICATOR_FULL);
            startOtaProcess();
            return;
        }

        if (fileContentTypeInfos.size() == 1) {
            FileTypeInfo fileTypeInfo = fileContentTypeInfos.get(0);
            getDfuConfig().setFileIndicator((1 << fileTypeInfo.getBitNumber()));
            startOtaProcess();
            return;
        }

        selectFileContentType(fileContentTypeInfos);
    }

    public void onBtScannerCallback(@NonNull BluetoothDevice bluetoothDevice, SpecScanRecord specScanRecord) {
        if (specScanRecord != null) {
            getDfuConfig().setLocalName(specScanRecord.getDeviceName());
            boolean isHid = false;
            if (specScanRecord.getServiceUuids() != null) {
                isHid = specScanRecord.getServiceUuids().contains(BluetoothUuid.HOGP);
            }
            connectRemoteDevice(bluetoothDevice, isHid);
        } else {
            getDfuConfig().setLocalName(null);
            connectRemoteDevice(bluetoothDevice, false);
        }
    }

    protected void configureDevOps() {
        // Optional, used for BLE(GATT)
        String otaServiceUuid = SettingsHelper.Companion.getInstance().getOtaServiceUUID();
        if (!TextUtils.isEmpty(otaServiceUuid)) {
            getDfuConfig().setOtaServiceUuid(otaServiceUuid);
        }

        // Optional
        String aesKey = SettingsHelper.Companion.getInstance().getDfuAesKey();
        if (!TextUtils.isEmpty(aesKey)) {
            getDfuConfig().setSecretKey(DataConverter.hex2Bytes(aesKey));
        }
        getDfuConfig().setBreakpointResumeEnabled(SettingsHelper.Companion.getInstance().isDfuBreakpointResumeEnabled());
        getDfuConfig().setAutomaticActiveEnabled(SettingsHelper.Companion.getInstance().isDfuAutomaticActiveEnabled());
//        getDfuConfig().setBatteryCheckEnabled(SettingsHelper.Companion.getInstance().isDfuBatteryCheckEnabled());
        getDfuConfig().setBatteryCheckEnabled(false);
        getDfuConfig().setLowBatteryThreshold(SettingsHelper.Companion.getInstance().getDfuLowBatteryThreshold());
        getDfuConfig().setBatteryLevelFormat(SettingsHelper.Companion.getInstance().getDfuBatteryLevelFormat());

//        getDfuConfig().setVersionCheckEnabled(SettingsHelper.Companion.getInstance().isDfuVersionCheckEnabled());
        getDfuConfig().setVersionCheckEnabled(false);
        getDfuConfig().setVersionCheckMode(SettingsHelper.Companion.getInstance().getDfuVersionCheckMode());

        getDfuConfig().setIcCheckEnabled(SettingsHelper.Companion.getInstance().isDfuChipTypeCheckEnabled());
        getDfuConfig().setSectionSizeCheckEnabled(SettingsHelper.Companion.getInstance().isDfuImageSectionSizeCheckEnabled());
        getDfuConfig().setThroughputEnabled(SettingsHelper.Companion.getInstance().isDfuThroughputEnabled());
        getDfuConfig().setMtuUpdateEnabled(SettingsHelper.Companion.getInstance().isDfuMtuUpdateEnabled());
        getDfuConfig().setWaitActiveCmdAckEnabled(SettingsHelper.Companion.getInstance().isDfuActiveAndResetAckEnabled());

        // only used for bee1
        getDfuConfig().setConParamUpdateLatencyEnabled(SettingsHelper.Companion.getInstance().isDfuConnectionParameterLatencyEnabled());
        getDfuConfig().setLatencyTimeout(SettingsHelper.Companion.getInstance().getDfuConnectionParameterLatencyTimeout());

        //optional for RWS
        getDfuConfig().setHandoverTimeout(SettingsHelper.Companion.getInstance().getDfuHandoverTimeout());

        getDfuConfig().setFileLocation(AppSettingsHelper.Companion.getInstance().getFileLocation());
        getDfuConfig().setFileSuffix(SettingsHelper.Companion.getInstance().getFileSuffix());
        if (SettingsHelper.Companion.getInstance().isDfuErrorActionDisconnectEnabled()) {
            getDfuConfig().addErrorAction(DfuConfig.ERROR_ACTION_DISCONNECT);
        } else {
            getDfuConfig().removeErrorAction(DfuConfig.ERROR_ACTION_DISCONNECT);
        }
        //true: enable refresh service cache, used for GATT
        if (SettingsHelper.Companion.getInstance().isDfuErrorActionRefreshDeviceEnabled()) {
            getDfuConfig().addErrorAction(DfuConfig.ERROR_ACTION_REFRESH_DEVICE);
        } else {
            getDfuConfig().removeErrorAction(DfuConfig.ERROR_ACTION_REFRESH_DEVICE);
        }
        if (SettingsHelper.Companion.getInstance().isDfuErrorActionCloseGattEnabled()) {
            getDfuConfig().addErrorAction(DfuConfig.EA_CLOSE_GATT);
            GlobalGatt.CLOSE_GATT_ENABLED = true;
        } else {
            getDfuConfig().removeErrorAction(DfuConfig.EA_CLOSE_GATT);
            GlobalGatt.CLOSE_GATT_ENABLED = false;
        }
        if (SettingsHelper.Companion.getInstance().isDfuCompleteActionRemoveBondEnabled()) {
            getDfuConfig().addCompleteAction(DfuConfig.COMPLETE_ACTION_REMOVE_BOND);
        } else {
            getDfuConfig().removeCompleteAction(DfuConfig.COMPLETE_ACTION_REMOVE_BOND);
        }

        getDfuConfig().setPhy(SettingsHelper.Companion.getInstance().getPreferredPhy());

        // optional, for log to debug
//        getDfuConfig().setLogLevel(DebuggerSettings.getInstance().isDebugEnabled() ? 1 : 0);
//        getDfuConfig().setPrimaryMtuSize(40);

        if (getDfuConfig().getOtaWorkMode() == DfuConstants.OTA_MODE_NORMAL_FUNCTION) {
            getDfuConfig().setWaitDisconnectWhenEnterOtaMode(SettingsHelper.Companion.getInstance().isDfuWaitDisconnectWhenEnterOtaModeEnabled());
        }

        // optinal, notification timeout
        getDfuConfig().setNotificationTimeout(SettingsHelper.Companion.getInstance().getDfuDataNotificationTimeout());

        // flow control
        getDfuConfig().setFlowControlEnabled(SettingsHelper.Companion.getInstance().isDfuFlowControlEnabled());
        getDfuConfig().setFlowControlInterval(SettingsHelper.Companion.getInstance().getDfuFlowControlInterval());
    }

    /**
     * start ota process
     */
    @Override
    public void startOtaProcess() {
        if (mSelectedDevice == null) {
            return;
        }
        notifyProcessStateChanged(STATE_OTA_PROCESSING);
//        WriteLog.getInstance().restartLog();

        // Mandatory
        getDfuConfig().setAddress(mSelectedDevice.getAddress());

        configureDevOps();
        getDfuConfig().setFilePath(mFilePath);
        ZLogger.v("startOtaProcess DfuConfig: " + getDfuConfig().toString());

        boolean ret = getDfuAdapter().startOtaProcedure(getDfuConfig(), mOtaDeviceInfo, true);
        if (!ret) {
            notifyProcessStateChanged(STATE_OTA_ERROR);
            ZLogger.v("startOtaProcess failed!!!!!");
        } else {
            ZLogger.v("startOtaProcess!!!!!");
        }


    }

    @Override
    public void onPendingActiveImage() {
    }

    /**
     * show Bin type select.
     */
    protected void selectFileContentType(List<FileTypeInfo> fileContentTypeInfos) {

    }

    public void changeWorkMode(int workMode) {
        getDfuConfig().setOtaWorkMode(workMode);
        refresh(true);
    }

    protected void selectWorkMode(boolean promptEnabled) {
        changeWorkMode(DfuConstants.OTA_MODE_NORMAL_FUNCTION);
//        OtaModeInfo modeInfo = null;
//        if (!promptEnabled) {
//            if (!AppSettingsHelper.Companion.getInstance().isWorkModePromptEnabled()) {
//                modeInfo = getDfuAdapter().getPriorityWorkMode(DfuConstants.OTA_MODE_SILENT_FUNCTION);
//            }
//            if (modeInfo != null) {
//                ZLogger.v(String.valueOf(modeInfo.getWorkmode()));
//                changeWorkMode(modeInfo.getWorkmode());
//                return;
//            }
//        }
//
//        List<OtaModeInfo> otaModeInfos = getDfuAdapter().getSupportedModes();
//        if (otaModeInfos == null || otaModeInfos.size() <= 0) {
//            changeWorkMode(DfuConstants.OTA_MODE_NORMAL_FUNCTION);
//            return;
//        }
//
//        modeInfo = otaModeInfos.get(0);
//        if (otaModeInfos.size() == 1) {
//            changeWorkMode(modeInfo.getWorkmode());
//            return;
//        }
//
//        OtaModeInfo finalModeInfo = modeInfo;
//        try {
//            // java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
//
//        } catch (Exception e) {
//            ZLogger.w(e.toString());
//
//            changeWorkMode(modeInfo.getWorkmode());
//        }
    }


}
