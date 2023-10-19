package com.skaiwalk.ble_ota.settings

import android.content.Context
import android.text.TextUtils
import com.realsil.sdk.core.logger.ZLogger
import com.realsil.sdk.core.utility.DataConverter
import com.realsil.sdk.dfu.DfuConstants
import com.realsil.sdk.dfu.model.DfuConfig
import com.realtek.sdk.support.toolbox.preference.BaseSharedPrefes
import java.util.*
import java.util.regex.Pattern

class SettingsHelper private constructor(context: Context) : BaseSharedPrefes(context) {

    val otaServiceUUID: String
        get() {
            var value = getString(KEY_DFU_OTA_SERVICE_UUID, null)

            if (TextUtils.isEmpty(value)) {
                value = OTA_SERVICE.toString()
                set(KEY_DFU_OTA_SERVICE_UUID, value)
            }

            return if (checkUuid(value)) {
                value
            } else {
                ""
            }
        }

    val dfuUsbEpInAddr: Int
        get() {
            val value = getString(KEY_DFU_USB_EP_IN_ADDR, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_USB_EP_IN_ADDR, "0")
                return 0
            } else {
                return Integer.parseInt(value)
            }
        }

    val dfuUsbEpOutAddr: Int
        get() {
            val value = getString(KEY_DFU_USB_EP_OUT_ADDR, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_USB_EP_OUT_ADDR, "0")
                return 0
            } else {
                return Integer.parseInt(value)
            }
        }

    private val SECRET_KEY = byteArrayOf(
        0x4E.toByte(),
        0x46.toByte(),
        0xF8.toByte(),
        0xC5.toByte(),
        0x09.toByte(),
        0x2B.toByte(),
        0x29.toByte(),
        0xE2.toByte(),
        0x9A.toByte(),
        0x97.toByte(),
        0x1A.toByte(),
        0x0C.toByte(),
        0xD1.toByte(),
        0xF6.toByte(),
        0x10.toByte(),
        0xFB.toByte(),
        0x1F.toByte(),
        0x67.toByte(),
        0x63.toByte(),
        0xDF.toByte(),
        0x80.toByte(),
        0x7A.toByte(),
        0x7E.toByte(),
        0x70.toByte(),
        0x96.toByte(),
        0x0D.toByte(),
        0x4C.toByte(),
        0xD3.toByte(),
        0x11.toByte(),
        0x8E.toByte(),
        0x60.toByte(),
        0x1A.toByte()
    )

    val dfuAesKey: String
        get() = getString(KEY_DFU_AES_KEY, DataConverter.bytes2Hex(SECRET_KEY))


    val isDfuVersionCheckEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_VERSION_CHECK)) {
                set(KEY_DFU_VERSION_CHECK, true)
                return true
            }

            return getBoolean(KEY_DFU_VERSION_CHECK, true)
        }
    val dfuVersionCheckMode: Int
        get() {
            val value = getString(KEY_DFU_VERSION_CHECK_MODE, null)
            if (TextUtils.isEmpty(value)) {
                set(
                    KEY_DFU_VERSION_CHECK_MODE,
                    "0"
                )
                return 0
            } else {
                return Integer.parseInt(value)
            }
        }
    val isDfuChipTypeCheckEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_CONFIG_CHIP_TYPE_CHECK)) {
                set(KEY_DFU_CONFIG_CHIP_TYPE_CHECK, true)
                return true
            }

            return getBoolean(KEY_DFU_CONFIG_CHIP_TYPE_CHECK, true)
        }

    val isDfuImageSectionSizeCheckEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_IMAGE_SECTION_SIZE_CHECK)) {
                set(KEY_DFU_IMAGE_SECTION_SIZE_CHECK, true)
                return true
            }

            return getBoolean(KEY_DFU_IMAGE_SECTION_SIZE_CHECK, true)
        }

    val isDfuBatteryCheckEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_BATTERY_CHECK)) {
                set(KEY_DFU_BATTERY_CHECK, true)
                return true
            }

            return getBoolean(KEY_DFU_BATTERY_CHECK, true)
        }

    val dfuBatteryLevelFormat: Int
        get() {
            val value = getString(KEY_DFU_BATTERY_LEVEL_FORMAT, null)
            if (TextUtils.isEmpty(value)) {
                set(
                    KEY_DFU_BATTERY_LEVEL_FORMAT,
                    DfuConfig.BATTERY_LEVEL_FORMAT_PERCENTAGE.toString()
                )
                return DfuConfig.BATTERY_LEVEL_FORMAT_PERCENTAGE
            } else {
                return Integer.parseInt(value)
            }
        }

    val dfuLowBatteryThreshold: Int
        get() {
            val value = getString(KEY_DFU_BATTERY_LOW_THRESHOLD, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_BATTERY_LOW_THRESHOLD, DfuConfig.MIN_POWER_LEVER.toString())
                return DfuConfig.MIN_POWER_LEVER
            } else {
                return Integer.parseInt(value)
            }
        }

    val isDfuAutomaticActiveEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_AUTOMATIC_ACTIVE)) {
                set(KEY_DFU_AUTOMATIC_ACTIVE, true)
                return true
            }

            return getBoolean(KEY_DFU_AUTOMATIC_ACTIVE, true)
        }

    val isDfuBreakpointResumeEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_BREAKPOINT_RESUME)) {
                set(KEY_DFU_BREAKPOINT_RESUME, false)
                return false
            }

            return getBoolean(KEY_DFU_BREAKPOINT_RESUME, false)
        }

    val isDfuActiveAndResetAckEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_ACTIVE_AND_RESET_ACK)) {
                set(KEY_DFU_ACTIVE_AND_RESET_ACK, false)
                return false
            }

            return getBoolean(KEY_DFU_ACTIVE_AND_RESET_ACK, false)
        }

    val isDfuErrorActionDisconnectEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_ERROR_ACTION_DISCONNECT)) {
                set(KEY_DFU_ERROR_ACTION_DISCONNECT, true)
                return true
            }

            return getBoolean(KEY_DFU_ERROR_ACTION_DISCONNECT, true)
        }

    val isDfuErrorActionRefreshDeviceEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_ERROR_ACTION_REFRESH_DEVICE)) {
                set(KEY_DFU_ERROR_ACTION_REFRESH_DEVICE, true)
                return true
            }

            return getBoolean(KEY_DFU_ERROR_ACTION_REFRESH_DEVICE, true)
        }

    val isDfuErrorActionCloseGattEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_ERROR_ACTION_CLOSE_GATT)) {
                set(KEY_DFU_ERROR_ACTION_CLOSE_GATT, true)
                return true
            }

            return getBoolean(KEY_DFU_ERROR_ACTION_CLOSE_GATT, true)
        }

    val isDfuCompleteActionRemoveBondEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_COMPLETE_ACTION_REMOVE_BOND)) {
                set(KEY_DFU_COMPLETE_ACTION_REMOVE_BOND, false)
                return false
            }

            return getBoolean(KEY_DFU_COMPLETE_ACTION_REMOVE_BOND, false)
        }


    val dfuSpeedControlLevel: Int
        get() {
            val value = getString(KEY_DFU_SPEED_CONTROL_LEVEL, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_SPEED_CONTROL_LEVEL, DfuConstants.SPEED_LEVEL_AUTOMATIC.toString())
                return DfuConstants.SPEED_LEVEL_AUTOMATIC
            } else {
                return Integer.parseInt(value)
            }
        }

    val dfuMaxReconnectTimes: Int
        get() {
            val value = getString(KEY_DFU_MAX_RECONNECT_TIMES, null)

            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_MAX_RECONNECT_TIMES, "3")
                return 3
            }

            try {
                return Integer.parseInt(value)
            } catch (e: Exception) {
                set(KEY_DFU_MAX_RECONNECT_TIMES, "3")
                return 3
            }

        }

    val isDfuHidDeviceEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_HID_AUTO_PAIR)) {
                set(KEY_DFU_HID_AUTO_PAIR, false)
                return false
            }

            return getBoolean(KEY_DFU_HID_AUTO_PAIR, false)
        }

    val isDfuThroughputEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_THROUGHPUT)) {
                set(KEY_DFU_THROUGHPUT, false)
                return false
            }

            return getBoolean(KEY_DFU_THROUGHPUT, false)
        }

    val isDfuMtuUpdateEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_MTU_UPDATE)) {
                set(KEY_DFU_MTU_UPDATE, false)
                return false
            }

            return getBoolean(KEY_DFU_MTU_UPDATE, false)
        }

    val isDfuFlowControlEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_FLOW_CONTROL)) {
                set(KEY_DFU_FLOW_CONTROL, true)
                return true
            }

            return getBoolean(KEY_DFU_FLOW_CONTROL, true)
        }

    val dfuFlowControlInterval: Int
        get() {
            val value = getString(KEY_DFU_FLOW_CONTROL_INTERVAL, null)

            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_FLOW_CONTROL_INTERVAL, "1")
                return 1
            }

            try {
                return Integer.parseInt(value)
            } catch (e: Exception) {
                set(KEY_DFU_FLOW_CONTROL_INTERVAL, "1")
                return 1
            }
        }

    val isFixedImageFileEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_FIXED_IMAGE_FILE)) {
                set(KEY_DFU_FIXED_IMAGE_FILE, false)
                return false
            }

            return getBoolean(KEY_DFU_FIXED_IMAGE_FILE, false)
        }

    val isDfuProductionPhoneBanklinkEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_PRODUCTION_PHONE_BANKLINK)) {
                set(KEY_DFU_PRODUCTION_PHONE_BANKLINK, false)
                return false
            }

            return getBoolean(KEY_DFU_PRODUCTION_PHONE_BANKLINK, false)
        }

    val dfuProductionPriorityWorkMode: Int
        get() {
            val value = getString(KEY_DFU_PRODUCTION_PRIORITY_WORK_MODE, null)
            if (TextUtils.isEmpty(value)) {
                set(
                    KEY_DFU_PRODUCTION_PRIORITY_WORK_MODE,
                    DfuConstants.OTA_MODE_SILENT_FUNCTION.toString()
                )
                return DfuConstants.OTA_MODE_SILENT_FUNCTION
            } else {
                return Integer.parseInt(value)
            }
        }

    val isDfuProductionSuccessInspectionEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_PRODUCTION_SUCCESS_INSPECTION)) {
                set(KEY_DFU_PRODUCTION_SUCCESS_INSPECTION, false)
                return false
            }

            return getBoolean(KEY_DFU_PRODUCTION_SUCCESS_INSPECTION, false)
        }

    val isDfuProductionSuccessAutoScanEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_PRODUCTION_SUCCESS_AUTO_SCAN)) {
                set(KEY_DFU_PRODUCTION_SUCCESS_AUTO_SCAN, false)
                return false
            }

            return getBoolean(KEY_DFU_PRODUCTION_SUCCESS_AUTO_SCAN, false)
        }

    val fileSuffix: String
        get() {
            var value = getString(KEY_DFU_FILE_SUFFIX, null)

            if (TextUtils.isEmpty(value)) {
                value = "bin"
                set(KEY_DFU_FILE_SUFFIX, value)
            }

            return value
        }


    val dfuHandoverTimeout: Int
        get() {
            val value = getString(KEY_DFU_CONFIG_RWS_HANDOVER_TIMEOUT, null)

            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_CONFIG_RWS_HANDOVER_TIMEOUT, "6")
                return 6
            }

            try {
                return Integer.parseInt(value)
            } catch (e: Exception) {
                set(KEY_DFU_CONFIG_RWS_HANDOVER_TIMEOUT, "6")
                return 6
            }

        }

    val dfuDataNotificationTimeout: Int
        get() {
            val value = getString(KEY_DFU_CONFIG_DATA_NOTIFICATION_TIMEOUT, null)

            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_CONFIG_DATA_NOTIFICATION_TIMEOUT, "10000")
                return 10000
            }

            try {
                var timeout = Integer.parseInt(value)
                if (timeout > 60000) {
                    set(KEY_DFU_CONFIG_DATA_NOTIFICATION_TIMEOUT, "60000")
                    return 60000
                } else {
                    return timeout
                }
            } catch (e: Exception) {
                set(KEY_DFU_CONFIG_DATA_NOTIFICATION_TIMEOUT, "10000")
                return 10000
            }
        }

    val isDfuWaitDisconnectWhenEnterOtaModeEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_CONFIG_WAIT_DISCONNECT_WHEN_ENTER_OTA_MODE)) {
                set(KEY_DFU_CONFIG_WAIT_DISCONNECT_WHEN_ENTER_OTA_MODE, true)
                return true
            }

            return getBoolean(KEY_DFU_CONFIG_WAIT_DISCONNECT_WHEN_ENTER_OTA_MODE, true)
        }


    val dfuUsbGattEndpoint: Int
        get() {
            val value = getString(KEY_DFU_USB_GATT_ENDPOINT, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_USB_GATT_ENDPOINT, USB_GATT_ENDPOINT_BULK.toString())
                return USB_GATT_ENDPOINT_BULK
            } else {
                return Integer.parseInt(value)
            }
        }

    val preferredPhy: Int
        get() {
            val value = getString(KEY_DFU_BLE_PREFERRED_PHY, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_BLE_PREFERRED_PHY, "0")
                return 0
            } else {
                return Integer.parseInt(value)
            }
        }


    val isTestParamsAesEncryptEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_TEST_PARAMS_AES_ENCRYPT)) {
                set(KEY_DFU_TEST_PARAMS_AES_ENCRYPT, false)
                return false
            }

            return getBoolean(KEY_DFU_TEST_PARAMS_AES_ENCRYPT, false)
        }
    val isTestParamsStressTestEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_TEST_PARAMS_STRESS_TEST)) {
                set(KEY_DFU_TEST_PARAMS_STRESS_TEST, false)
                return false
            }

            return getBoolean(KEY_DFU_TEST_PARAMS_STRESS_TEST, false)
        }
    val isTestParamsBufferCheckEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_TEST_PARAMS_BUFFER_CHECK)) {
                set(KEY_DFU_TEST_PARAMS_BUFFER_CHECK, false)
                return false
            }

            return getBoolean(KEY_DFU_TEST_PARAMS_BUFFER_CHECK, false)
        }
    val isTestParamsSkipFailEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_TEST_PARAMS_SKIP_FAIL)) {
                set(KEY_DFU_TEST_PARAMS_SKIP_FAIL, false)
                return false
            }

            return getBoolean(KEY_DFU_TEST_PARAMS_SKIP_FAIL, false)
        }
    val isTestParamsCopyFailEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_TEST_PARAMS_COPY_FAIL)) {
                set(KEY_DFU_TEST_PARAMS_COPY_FAIL, false)
                return false
            }

            return getBoolean(KEY_DFU_TEST_PARAMS_COPY_FAIL, false)
        }


    val isSppConnectionPairEnabled: Boolean
        get() {

            return getBoolean(KEY_DFU_SPP_CONNECTION_PAIR, false)
        }


    val isDfuConnectionParameterLatencyEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_CONNECTION_PARAMETER_LATENCY_ENABLED)) {
                set(KEY_DFU_CONNECTION_PARAMETER_LATENCY_ENABLED, true)
                return true
            }

            return getBoolean(KEY_DFU_CONNECTION_PARAMETER_LATENCY_ENABLED, true)
        }

    val dfuConnectionParameterLatencyTimeout: Int
        get() {
            val value = getString(KEY_DFU_CONNECTION_PARAMETER_LATENCY_TIMEOUT, null)

            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_CONNECTION_PARAMETER_LATENCY_TIMEOUT, "10")
                return 10
            }

            try {
                return Integer.parseInt(value)
            } catch (e: Exception) {
                set(KEY_DFU_CONNECTION_PARAMETER_LATENCY_TIMEOUT, "10")
                return 10
            }
        }

    val dfuActiveImageDelayTime: Long
        get() {
            val value = getString(KEY_DFU_ACTIVE_IMAGE_DELAY_TIME, null)

            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_ACTIVE_IMAGE_DELAY_TIME, "0")
                return 0
            }

            try {
                return Integer.parseInt(value).toLong()
            } catch (e: Exception) {
                set(KEY_DFU_ACTIVE_IMAGE_DELAY_TIME, "0")
                return 0
            }
        }

    val dfuConnectionParameterMaxInterval: Int
        get() {
            val value = getString(KEY_DFU_CONNECTION_PARAMETER_MAX_INTERVAL, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_CONNECTION_PARAMETER_MAX_INTERVAL, "17")
                return 17
            } else {
                return Integer.parseInt(value)
            }
        }
    val dfuConnectionParameterMinInterval: Int
        get() {
            val value = getString(KEY_DFU_CONNECTION_PARAMETER_MIN_INTERVAL, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_CONNECTION_PARAMETER_MIN_INTERVAL, "6")
                return 6
            } else {
                return Integer.parseInt(value)
            }
        }

    val dfuConnectionParameterLatency: Int
        get() {
            val value = getString(KEY_DFU_CONNECTION_PARAMETER_LATENCY, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_CONNECTION_PARAMETER_LATENCY, "0")
                return 0
            } else {
                return Integer.parseInt(value)
            }
        }


    val dfuConnectionParameterTimeout: Int
        get() {
            val value = getString(KEY_DFU_CONNECTION_PARAMETER_TIMEOUT, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_DFU_CONNECTION_PARAMETER_TIMEOUT, "500")
                return 500
            } else {
                return Integer.parseInt(value)
            }
        }


    init {
        ZLogger.v("getOtaServiceUUID:$otaServiceUUID")

        ZLogger.v("Usb: EpInAddr=$dfuUsbEpInAddr, EpOutAddr:$dfuUsbEpOutAddr")

        ZLogger.v("getDfuAesKey:$dfuAesKey")
        ZLogger.v(
            "Dfu: VersionCheck=($isDfuVersionCheckEnabled,mode=$dfuVersionCheckMode), ChipTypeCheck=$isDfuChipTypeCheckEnabled," +
                    "ImageSectionSizeCheck:$isDfuImageSectionSizeCheckEnabled,BatteryCheckEnabled:$isDfuBatteryCheckEnabled, BatteryThreshold:$dfuLowBatteryThreshold"
        )
        ZLogger.v("isDfuAutomaticActive:$isDfuAutomaticActiveEnabled, isDfuBreakpointResume:$isDfuBreakpointResumeEnabled, ActiveAndResetAck:$isDfuActiveAndResetAckEnabled")
        ZLogger.v(
            "errorAction:disconnect=$isDfuErrorActionDisconnectEnabled, " +
                    "refresh=$isDfuErrorActionRefreshDeviceEnabled, closeGatt=$isDfuErrorActionCloseGattEnabled"
        )
        ZLogger.v("isDfuCompleteActionRemoveBondEnabled:$isDfuCompleteActionRemoveBondEnabled")
        ZLogger.v("getDfuSpeedControlLevel:$dfuSpeedControlLevel")
        ZLogger.v("getDfuMaxReconnectTimes:$dfuMaxReconnectTimes")
        ZLogger.v("isDfuHidDeviceEnabled:$isDfuHidDeviceEnabled")
        ZLogger.v("isDfuThroughput:$isDfuThroughputEnabled, isDfuMtuUpdateEnabled:$isDfuMtuUpdateEnabled")

        ZLogger.v(
            "Production: isDfuPhoneBanklink:$isDfuProductionPhoneBanklinkEnabled, getDfuPriorityWorkMode:$dfuProductionPriorityWorkMode," +
                    "isDfuSuccessAutoScanEnabled:$isDfuProductionSuccessAutoScanEnabled, isDfuSuccessInspectionEnabled:$isDfuProductionSuccessInspectionEnabled"
        )

        ZLogger.v("SPP: isSppConnectionPairEnabled=$isSppConnectionPairEnabled")

        ZLogger.v("dfuActiveImageDelayTime:$dfuActiveImageDelayTime")
        ZLogger.v("ConnectionParameterLatency, enabled:$isDfuConnectionParameterLatencyEnabled, timeout:$dfuConnectionParameterLatencyTimeout")
        ZLogger.v(
            "ConnectionParameter:maxInterval=$dfuConnectionParameterMaxInterval,minInterval=$dfuConnectionParameterMinInterval," +
                    "latency=$dfuConnectionParameterLatency,timeout=$dfuConnectionParameterTimeout"
        )
        ZLogger.v("FlowControl, enabled:$isDfuFlowControlEnabled, interval:$dfuFlowControlInterval")

        ZLogger.v("fileSuffix:$fileSuffix")
        ZLogger.v("dfuHandoverTimeout:$dfuHandoverTimeout, dfuDataNotificationTimeout=$dfuDataNotificationTimeout")
        ZLogger.v("isDfuWaitDisconnectWhenEnterOtaModeEnabled:$isDfuWaitDisconnectWhenEnterOtaModeEnabled")
        ZLogger.v("dfuUsbGattEndpoint:$dfuUsbGattEndpoint")
        ZLogger.v("preferredPhy:$preferredPhy")

        ZLogger.v(
            "testParams:aes=$isTestParamsAesEncryptEnabled,stress=$isTestParamsStressTestEnabled," +
                    "buffercheck=$isTestParamsBufferCheckEnabled,copyFail=$isTestParamsCopyFailEnabled," +
                    "skipFail=$isTestParamsSkipFailEnabled"
        )
    }

    companion object {
        const val PREF_MASK = 0x0000

        const val PREF_DFU = 0x0001

        const val PREF_DFU_DEV = 0x0002

        const val PREF_DFU_GATT = 0x0004


        const val PREF_DFU_SPP = 0x0008

        const val PREF_DFU_USB_GATT = 0x0010

        const val PREF_KEY_GENERAL = 0x0100
        const val KEY_DFU_VERSION_CHECK = "switch_dfu_version_check"
        const val KEY_DFU_VERSION_CHECK_MODE = "dfu_version_check_mode"
        private const val KEY_DFU_CONFIG_CHIP_TYPE_CHECK = "switch_dfu_config_chip_check"
        private const val KEY_DFU_IMAGE_SECTION_SIZE_CHECK = "switch_dfu_image_section_size_check"

        const val KEY_DFU_BATTERY_CHECK = "switch_dfu_battery_check"
        private const val KEY_DFU_BATTERY_LEVEL_FORMAT = "dfu_battery_check_format"

        const val KEY_DFU_BATTERY_LOW_THRESHOLD = "dfu_battery_low_threshold"
        private const val KEY_DFU_AUTOMATIC_ACTIVE = "switch_dfu_automatic_active"
        private const val KEY_DFU_BREAKPOINT_RESUME = "switch_dfu_breakpoint_resume"
        private const val KEY_DFU_ACTIVE_AND_RESET_ACK = "switch_dfu_active_and_reset_ack"
        private const val KEY_DFU_ERROR_ACTION_DISCONNECT = "switch_dfu_error_action_disconnect"
        private const val KEY_DFU_ERROR_ACTION_REFRESH_DEVICE =
            "switch_dfu_error_action_refresh_device"
        const val KEY_DFU_ERROR_ACTION_CLOSE_GATT = "switch_dfu_error_action_close_gatt"

        private const val KEY_DFU_COMPLETE_ACTION_REMOVE_BOND =
            "switch_dfu_complete_action_remove_bond"
        private const val KEY_DFU_SPEED_CONTROL_LEVEL = "dfu_speed_control_level_v2"

        const val KEY_DFU_MAX_RECONNECT_TIMES = "edittext_max_reconnect_times"
        private const val KEY_DFU_THROUGHPUT = "switch_dfu_throughput"
        private const val KEY_DFU_MTU_UPDATE = "switch_dfu_mtu_update"

        const val KEY_DFU_CONNECTION_PARAMETER_MAX_INTERVAL =
            "et_dfu_connection_parameter_max_interval"
        const val KEY_DFU_CONNECTION_PARAMETER_MIN_INTERVAL =
            "et_dfu_connection_parameter_min_interval"
        const val KEY_DFU_CONNECTION_PARAMETER_LATENCY =
            "et_dfu_connection_parameter_latency"
        const val KEY_DFU_CONNECTION_PARAMETER_TIMEOUT =
            "et_dfu_connection_parameter_timeout"

        const val KEY_DFU_CONNECTION_PARAMETER_LATENCY_ENABLED =
            "switch_dfu_connection_params_latency"
        const val KEY_DFU_CONNECTION_PARAMETER_LATENCY_TIMEOUT =
            "et_dfu_connection_params_latency_timeout"
        const val KEY_DFU_ACTIVE_IMAGE_DELAY_TIME =
            "KEY_DFU_ACTIVE_IMAGE_DELAY_TIME"

        const val KEY_DFU_FLOW_CONTROL = "switch_dfu_flow_control"

        const val KEY_DFU_FLOW_CONTROL_INTERVAL =
            "et_dfu_flow_control_interval"

        private const val KEY_DFU_HID_AUTO_PAIR = "switch_hid_auto_pair"
        private const val KEY_DFU_FIXED_IMAGE_FILE = "switch_dfu_fixed_image_file"

        private const val KEY_DFU_PRODUCTION_PHONE_BANKLINK = "switch_dfu_production_phone_banklink"
        private const val KEY_DFU_PRODUCTION_PRIORITY_WORK_MODE =
            "dfu_production_priotiry_work_mode"
        private const val KEY_DFU_PRODUCTION_SUCCESS_INSPECTION =
            "switch_dfu_production_success_inspection"
        private const val KEY_DFU_PRODUCTION_SUCCESS_AUTO_SCAN =
            "switch_dfu_production_success_auto_scan"

        const val KEY_DFU_FILE_SUFFIX = "rtk_dfu_file_suffix"

        //GATT
        const val KEY_DFU_OTA_SERVICE_UUID = "rtk_dfu_ota_service_uuid"

        //SPP
        const val KEY_DFU_SPP_CONNECTION_PAIR = "switch_dfu_spp_connection_pair"

        //USB
        const val KEY_DFU_USB_EP_IN_ADDR = "rtk_dfu_usb_ep_in_addr_1"

        const val KEY_DFU_USB_EP_OUT_ADDR = "rtk_dfu_usb_ep_out_addr_1"

        const val KEY_DFU_AES_KEY = "rtk_dfu_aes_key"

        const val KEY_DFU_CONFIG_RWS_HANDOVER_TIMEOUT = "switch_dfu_config_handover_timeout"
        const val KEY_DFU_CONFIG_DATA_NOTIFICATION_TIMEOUT =
            "KEY_DFU_CONFIG_DATA_NOTIFICATION_TIMEOUT"

        const val KEY_DFU_CONFIG_WAIT_DISCONNECT_WHEN_ENTER_OTA_MODE =
            "switch_dfu_wait_disconnect_when_enter_ota_mode"

        const val KEY_DFU_USB_GATT_ENDPOINT = "dfu_usb_gatt_endpoint"
        const val USB_GATT_ENDPOINT_BULK = 0x02
        const val USB_GATT_ENDPOINT_CONTROL = 0x00

        private const val KEY_DFU_BLE_PREFERRED_PHY = "dfu_ble_preferred_phy"


        private const val KEY_DFU_TEST_PARAMS_AES_ENCRYPT = "switch_dfu_test_params_aes_encrypt"
        private const val KEY_DFU_TEST_PARAMS_STRESS_TEST = "switch_dfu_test_params_stress_test"
        private const val KEY_DFU_TEST_PARAMS_BUFFER_CHECK = "switch_dfu_test_params_buffer_check"
        private const val KEY_DFU_TEST_PARAMS_COPY_FAIL = "switch_dfu_test_params_copy_fail"
        private const val KEY_DFU_TEST_PARAMS_SKIP_FAIL = "switch_dfu_test_params_skip_fail"

        @Volatile
        private var instance: SettingsHelper? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(SettingsHelper::class.java) {
                    if (instance == null) {
                        instance =
                            SettingsHelper(context.applicationContext)
                    }
                }
            }
        }

        @JvmStatic
        fun getInstance(): SettingsHelper? {
            if (instance == null) {
                ZLogger.w("not initialized, please call initialize(Context context) first")
            }
            return instance
        }

        fun checkUuid(uuid: String): Boolean {
            if (!TextUtils.isEmpty(uuid) && uuid.matches("(\\w{8}(-\\w{4}){3}-\\w{12}?)".toRegex())) {
                try {
                    UUID.fromString(uuid)
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            return false
        }

        private val OTA_SERVICE = UUID.fromString("0000d0ff-3c17-d293-8e48-14fe2e4da212")

        private val AES_KEY_PATTERN = Pattern.compile("([a-zA-Z0-9]+)")

        fun checkAesKey(key: String): Boolean {
            if (!TextUtils.isEmpty(key)) {
                if (key.length == 64) {
                    return AES_KEY_PATTERN.matcher(key).matches()
                } else {
                    ZLogger.w("aes key length is invalid")
                    return false
                }
            }

            return false
        }
    }

}
