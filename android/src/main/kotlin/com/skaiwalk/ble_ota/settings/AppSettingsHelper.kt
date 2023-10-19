/*
 * Copyright (c) 2018-2023. Skaiwalk Corporation.
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

package com.skaiwalk.ble_ota.settings

import android.content.Context
import android.text.TextUtils
import com.realsil.sdk.core.logger.ZLogger
import com.realsil.sdk.dfu.model.DfuConfig
import com.realtek.sdk.support.toolbox.preference.BaseSharedPrefes


/**
 * @author shixin_lin@skaiwalk.com
 * @date 16/10/2023
 */

class AppSettingsHelper private constructor(context: Context) : BaseSharedPrefes(context) {

    val isWorkModePromptEnabled: Boolean
        get() {
            if (!contains(KEY_WORK_MODE_PROMPT)) {
                set(KEY_WORK_MODE_PROMPT, false)
                return false
            }

            return getBoolean(KEY_WORK_MODE_PROMPT, false)
        }

    val isUploadFilePromptEnabled: Boolean
        get() {
            if (!contains(KEY_UPLOAD_FILE_PROMPT)) {
                set(KEY_UPLOAD_FILE_PROMPT, false)
                return false
            }

            return getBoolean(KEY_UPLOAD_FILE_PROMPT, false)
        }

    val isDfuBankLinkEnabled: Boolean
        get() {
            if (!contains(KEY_BANK_LINK)) {
                set(KEY_BANK_LINK, false)
                return false
            }

            return getBoolean(KEY_BANK_LINK, false)
        }

    val isDfuImageFeatureCheckEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_IMAGE_FEATURE_CHECK)) {
                set(KEY_DFU_IMAGE_FEATURE_CHECK, false)
                return false
            }

            return getBoolean(KEY_DFU_IMAGE_FEATURE_CHECK, false)
        }

    /**
     * it's recommended to turn on for bbpro ic.
     */
    val isDfuSuccessHintEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_SUCCESS_HINT)) {
                set(KEY_DFU_SUCCESS_HINT, false)
                return false
            }

            return getBoolean(KEY_DFU_SUCCESS_HINT, false)
        }

    val isFixedImageFileEnabled: Boolean
        get() {
            if (!contains(KEY_DFU_FIXED_IMAGE_FILE)) {
                set(KEY_DFU_FIXED_IMAGE_FILE, false)
                return false
            }

            return getBoolean(KEY_DFU_FIXED_IMAGE_FILE, false)
        }

    val selectFileType: String
        get() {
            val value = getString(KEY_RTK_SELECT_FILE_TYPE, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_RTK_SELECT_FILE_TYPE, "*/*")
                return "*/*"
            } else {
                return value
            }
        }

    val fileLocation: Int
        get() {
            val value = getString(KEY_RTK_FILE_LOCATION, null)
            if (TextUtils.isEmpty(value)) {
                set(KEY_RTK_FILE_LOCATION, DfuConfig.FILE_LOCATION_SDCARD.toString())
                return DfuConfig.FILE_LOCATION_SDCARD
            } else {
                return Integer.parseInt(value)
            }
        }

    val progressType: Int
        get() {
            val value = getString(KEY_RTK_PROGRESS_TYPE, null)
            return if (TextUtils.isEmpty(value)) {
                0
            } else {
                Integer.parseInt(value)
            }
        }

    init {
        ZLogger.v("isWorkModePrompt:$isWorkModePromptEnabled, isUploadFilePrompt:$isUploadFilePromptEnabled")
        ZLogger.v("isDfuBankLink:$isDfuBankLinkEnabled, isDfuSuccessHint:$isDfuSuccessHintEnabled")
        ZLogger.v("selectFileType:$selectFileType, fileLocation:$fileLocation, progressType:$progressType")
        ZLogger.v("isDfuImageFeatureCheckEnabled:$isDfuImageFeatureCheckEnabled")
    }

    companion object {
        private const val KEY_WORK_MODE_PROMPT = "switch_dfu_work_mode_prompt"
        private const val KEY_UPLOAD_FILE_PROMPT = "switch_dfu_upload_file_prompt"
        private const val KEY_BANK_LINK = "switch_dfu_backlink"
        private const val KEY_DFU_IMAGE_FEATURE_CHECK = "switch_dfu_image_feature_check"
        private const val KEY_DFU_SUCCESS_HINT = "switch_dfu_success_hint"
        private const val KEY_DFU_FIXED_IMAGE_FILE = "switch_dfu_fixed_image_file"
        private const val KEY_RTK_SELECT_FILE_TYPE = "rtk_select_file_type"
        private const val KEY_RTK_FILE_LOCATION = "rtk_file_location"
        private const val KEY_RTK_PROGRESS_TYPE = "rtk_progress_type"

        @Volatile
        private var instance: AppSettingsHelper? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(AppSettingsHelper::class.java) {
                    if (instance == null) {
                        instance =
                            AppSettingsHelper(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): AppSettingsHelper? {
            if (instance == null) {
                ZLogger.w("not initialized, please call initialize(Context context) first")
            }
            return instance
        }
    }

}
