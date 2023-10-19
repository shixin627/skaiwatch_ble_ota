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

package com.skaiwalk.ble_ota;

import android.content.Context;
import android.content.res.TypedArray;

import com.realsil.sdk.dfu.DfuConstants;
import com.realsil.sdk.dfu.DfuException;
import com.realsil.sdk.dfu.internal.constants.OtaMode;
import com.realsil.sdk.dfu.model.OtaDeviceInfo;
import com.realsil.sdk.dfu.model.OtaModeInfo;
import com.realsil.sdk.dfu.utils.DfuAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * helper class
 *
 * @author nat_zhang@realsil.com.cn
 */
public class DfuHelperImpl {


    /**
     * parse error message
     *
     * @param context
     * @param type
     * @param code
     * @return
     */
    public static String parseError(Context context, int type, int code) {
        if (type == DfuException.Type.CONNECTION) {
            return parseConnectionErrorCode(context, code);
        } else {
            return parseErrorCode(context, code);
        }
    }

    /**
     * obtain the referenced arrays
     */
    public static String parseErrorCode(Context context, int code) {
        TypedArray cateDefaultArray = context.getResources().obtainTypedArray(R.array.error_code);
        for (int i = 0; i < cateDefaultArray.length(); i++) {
            String[] errorInfo = context.getResources().getStringArray(cateDefaultArray.getResourceId(i, -1));
            int error = Integer.parseInt(errorInfo[0]);
            String title = errorInfo[1];
            String detail = errorInfo[2];
            if (error == code) {
                return String.format(context.getResources().getString(R.string.rtk_dfu_toast_error_message),
                        String.format(Locale.US, "0x%04X(%d)", error, error), title, detail);
            }
        }

        cateDefaultArray.recycle();

        return String.format(context.getResources().getString(R.string.rtk_dfu_toast_error_message),
                String.format("0x%04X", code), "null", "null");
    }

    public static String parseConnectionErrorCode(Context context, int code) {
        TypedArray cateDefaultArray = context.getResources().obtainTypedArray(R.array.connection_error_code);
        for (int i = 0; i < cateDefaultArray.length(); i++) {
            String[] errorInfo = context.getResources().getStringArray(cateDefaultArray.getResourceId(i, -1));
            int error = Integer.parseInt(errorInfo[0]);
            String title = errorInfo[1];
            String detail = errorInfo[2];
            if (error == code) {
                return String.format(context.getResources().getString(R.string.rtk_dfu_toast_error_message),
                        String.format("0x%04X", error), title, detail);
            }
        }

        cateDefaultArray.recycle();

        return String.format(context.getResources().getString(R.string.rtk_dfu_toast_error_message),
                String.format("0x%04X", code), "null", "null");
    }

    public static String[] getErrorMessageByCode(Context context, int code) {
        String[] errorInfo = null;
        //obtain the referenced arrays
        TypedArray cateDefaultArray = context.getResources().obtainTypedArray(R.array.error_code);
        for (int i = 0; i < cateDefaultArray.length(); i++) {
            String[] tmp = context.getResources().getStringArray(cateDefaultArray.getResourceId(i, -1));
            int error = Integer.parseInt(tmp[0]);
//            String title = tmp[1];
//            String detail = tmp[2];
            if (error == code) {
                errorInfo = tmp;
                break;
            }
        }

        cateDefaultArray.recycle();

        return errorInfo;
    }

    public static int parseImageVersionIndicator(int value) {
        switch (value) {
            case 0x00:
                return R.string.rtk_dfu_bin_indicator_00;
            case 0x01:
                return R.string.rtk_dfu_bin_indicator_01;
            case 0x02:
                return R.string.rtk_dfu_bin_indicator_10;
            case 0x03:
                return R.string.rtk_dfu_bin_indicator_11;
            default:
                return R.string.rtk_dfu_bin_indicator_undefined;
        }
    }

    /**
     * @param context
     * @param code    refer to {@link com.realsil.sdk.dfu.exception.LoadFileException}
     * @return
     */
    public static String parseBinInfoError(@NotNull Context context, int code) {
        TypedArray cateDefaultArray = context.getResources().obtainTypedArray(R.array.load_file_error_code);
        for (int i = 0; i < cateDefaultArray.length(); i++) {
            String[] errorInfo = context.getResources().getStringArray(cateDefaultArray.getResourceId(i, -1));
            int error = Integer.parseInt(errorInfo[0]);
            String title = errorInfo[1];
            String detail = errorInfo[2];
            if (error == code) {
                return title;
            }
        }

        cateDefaultArray.recycle();
        return "";
    }


    /**
     * @param mode
     * @return
     */
    public static String parseOtaMode(int mode) {
        switch (mode) {
            case OtaMode.OTA_MODE_NORMAL_FUNCTION:
                return "NORMAL_FUNCTION";
            case OtaMode.OTA_MODE_SILENT_FUNCTION:
                return "SILENT_FUNCTION";
            case OtaMode.OTA_MODE_SILENT_EXTEND_FLASH:
                return "SILENT_EXTEND_FLASH";
            case OtaMode.OTA_MODE_SILENT_NO_TEMP:
                return "SILENT_NO_TEMP";
            case OtaMode.OTA_MODE_SILENT_DUALBANK_FORCE_TEMP:
                return "SILENT_FORCE_TEMP";
            case OtaMode.OTA_MODE_SILENT_DUALBANK_FORCE_COPY_DATA_IMAGE:
                return "SILENT_DUALBANK_FORCE_COPY_DATA_IMAGE";
            case OtaMode.OTA_MODE_SILENT_RWS:
                return "SILENT_RWS";
            default:
                return "Unknown (" + mode + ")";
        }
    }

    public static int getWorkModeNameResId(int type) {
        switch (type) {
            case OtaMode.OTA_MODE_NORMAL_FUNCTION:
                return R.string.rtk_dfu_work_mode_normal;
            case DfuConstants.OTA_MODE_SILENT_FUNCTION:
                return R.string.rtk_dfu_work_mode_slient;
            case OtaMode.OTA_MODE_SILENT_EXTEND_FLASH:
                return R.string.rtk_dfu_work_mode_extension;
            case OtaMode.OTA_MODE_SILENT_NO_TEMP:
                return R.string.rtk_dfu_work_mode_silent_no_temp;
            case OtaMode.OTA_MODE_SILENT_DUALBANK_FORCE_TEMP:
                return R.string.rtk_dfu_work_mode_silent_force_temp;
            case OtaMode.OTA_MODE_SILENT_DUALBANK_FORCE_COPY_DATA_IMAGE:
                return R.string.rtk_dfu_work_mode_silent_dualbank_force_copy_data_image;
            case OtaMode.OTA_MODE_SILENT_RWS:
                return R.string.rtk_dfu_work_mode_silent_rws;
            case OtaMode.OTA_MODE_NORMAL_SEQ:
                return R.string.rtk_dfu_work_mode_normal_vp;
            case OtaMode.OTA_MODE_SILENT_SEQ:
                return R.string.rtk_dfu_work_mode_silent_vp;
            case OtaMode.OTA_MODE_SILENT_VP_ID:
                return R.string.rtk_dfu_work_mode_vp_id;
            default:
                return R.string.rtk_dfu_work_mode_unknown;
        }
    }

    public static List<OtaModeInfo> getSupportedWorkModeInfos() {
        List<OtaModeInfo> otaModeInfos = new ArrayList<>();
        otaModeInfos.add(new OtaModeInfo(OtaMode.OTA_MODE_NORMAL_FUNCTION, "NORMAL_FUNCTION"));
        otaModeInfos.add(new OtaModeInfo(OtaMode.OTA_MODE_SILENT_FUNCTION, "SILENT_FUNCTION"));
        return otaModeInfos;
    }


    private void test1(int a) {
        int b = 0;
        if (a < 1) {
            return;
        }

        b += 1;
        if (a < 2) {
            return;
        }
        b += 1;
        if (a < 3) {
            return;
        }
    }

    /**
     * @param state state
     * @return
     */
    public static int getAdapterStateResId(int state) {
        switch (state) {
            case DfuAdapter.STATE_INIT_BINDING_SERVICE:
                return R.string.rtk_ota_state_bind_service;
            case DfuAdapter.STATE_INIT_OK:
                return R.string.rtk_ota_state_init_ok;
            case DfuAdapter.STATE_PREPARE_CONNECTING:
                return R.string.rtk_ota_state_connecting;
            case DfuAdapter.STATE_PENDDING_DISCOVERY_SERVICE:
                return R.string.rtk_ota_state_pending_discover_service;
            case DfuAdapter.STATE_DISCOVERY_SERVICE:
                return R.string.rtk_ota_state_discover_service;
            case DfuAdapter.STATE_READ_DEVICE_INFO:
                return R.string.rtk_ota_state_read_device_info;
            case DfuAdapter.STATE_READ_PROTOCOL_TYPE:
                return R.string.rtk_ota_state_read_protocol_type;
            case DfuAdapter.STATE_READ_IMAGE_INFO:
                return R.string.rtk_ota_state_read_image_info;
            case DfuAdapter.STATE_READ_BATTERY_INFO:
                return R.string.rtk_ota_state_read_battery_info;
            case DfuAdapter.STATE_PREPARED:
                return R.string.rtk_ota_state_prepared;
            case DfuAdapter.STATE_OTA_PROCESSING:
                return R.string.rtk_ota_state_ota_processing;
            case DfuAdapter.STATE_DISCONNECTING:
                return R.string.rtk_dfu_connection_state_disconnecting;
            case DfuAdapter.STATE_DISCONNECTED:
                return R.string.rtk_dfu_connection_state_disconnected;
            case DfuAdapter.STATE_CONNECT_FAILED:
                return R.string.rtk_dfu_connection_state_disconnected;
            case DfuAdapter.STATE_PENDING_ABORT:
                return R.string.rtk_dfu_state_abort_processing;
            case DfuAdapter.STATE_ABORTED:
                return R.string.rtk_dfu_state_aborted;
            default:
                return R.string.rtk_dfu_state_known;
        }
    }

    /**
     * @param state state
     * @return
     */
    public static int getProgressStateResId(int state) {
        switch (state) {
            case DfuConstants.PROGRESS_ORIGIN:
                return R.string.rtk_dfu_progress_state_origin;
            case DfuConstants.PROGRESS_INITIALIZE:
                return R.string.rtk_dfu_state_initialize;
            case DfuConstants.PROGRESS_STARTED:
                return R.string.rtk_dfu_state_start;
            case DfuConstants.PROGRESS_REMOTE_ENTER_OTA:
                return R.string.rtk_dfu_state_remote_enter_ota;
            case DfuConstants.PROGRESS_SCAN_REMOTE:
            case DfuConstants.PROGRESS_SCAN_OTA_REMOTE:
                return R.string.rtk_dfu_state_find_ota_remote;
            case DfuConstants.PROGRESS_CONNECT_REMOTE:
            case DfuConstants.PROGRESS_CONNECT_OTA_REMOTE:
                return R.string.rtk_dfu_state_connect_ota_remote;
            case DfuConstants.PROGRESS_PREPARE_OTA_ENVIRONMENT:
                return R.string.rtk_dfu_state_prepare_dfu_processing;
            case DfuConstants.PROGRESS_START_DFU_PROCESS:
                return R.string.rtk_dfu_state_start_ota_processing;
            case DfuConstants.PROGRESS_HAND_OVER_PROCESSING:
                return R.string.rtk_dfu_state_hand_over_processing;
            case DfuConstants.PROGRESS_PENDING_ACTIVE_IMAGE:
                return R.string.rtk_dfu_state_pending_active_image;
            case DfuConstants.PROGRESS_ACTIVE_IMAGE_AND_RESET:
                return R.string.rtk_dfu_state_start_active_image;
            case DfuConstants.PROGRESS_IMAGE_ACTIVE_SUCCESS:
                return R.string.rtk_dfu_state_image_active_success;
            case DfuConstants.PROGRESS_ABORT_PROCESSING:
                return R.string.rtk_dfu_state_abort_processing;
            case DfuConstants.PROGRESS_PROCESSING_ERROR:
                return R.string.rtk_dfu_state_error_processing;
            case DfuConstants.PROGRESS_ABORTED:
                return R.string.rtk_dfu_state_aborted;
            case DfuConstants.PROGRESS_SCAN_SECONDARY_BUD:
                return R.string.rtk_dfu_state_scan_secondary_bud;
            default:
                return R.string.rtk_dfu_state_known;
        }
    }

    public static int parseUpdateMechanism(int value) {
        switch (value) {
            case OtaDeviceInfo.MECHANISM_ONE_BY_ONE:
                return R.string.rtk_dfu_update_mechanism_one_by_one;
            case OtaDeviceInfo.MECHANISM_ALL_IN_ONE:
                return R.string.rtk_dfu_update_mechanism_all_in_one;
            case OtaDeviceInfo.MECHANISM_ALL_IN_ONE_WITH_BUFFER:
                return R.string.rtk_dfu_update_mechanism_all_in_one_with_buffer;
            default:
                return R.string.rtk_dfu_update_mechanism_one_by_one;
        }
    }
}
