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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.realsil.sdk.core.logger.ZLogger
import com.realsil.sdk.dfu.model.DfuConfig
import com.realsil.sdk.support.compat.IntentCompat.getParcelableExtraCompat


/**
 * Android 3.1+ 处于'Stopped' 状态(首次安装或者强制关闭)的应用接收不到广播
 *
 * @author nat_zhang@realsil.com.cn
 * @date 2018/4/4
 */

class RtkOtaReceiver : BroadcastReceiver() {

    private val mContext: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        ZLogger.d(D, action)

        when (action) {
            ACTION_START_OTA -> {
                val dfuConfig =
                    intent.getParcelableExtraCompat(
                        RtkUpdateService.EXTRA_DFU_CONFIG,
                        DfuConfig::class.java
                    )

                val service = Intent(context, RtkUpdateService::class.java)
                service.action = ACTION_START_OTA
                service.putExtra(RtkUpdateService.EXTRA_DFU_CONFIG, dfuConfig)
                ContextCompat.startForegroundService(context, service)
            }
            else -> {
            }
        }
    }

    companion object {

        private const val TAG = "RtkOtaReceiver"
        private const val D = true

        const val ACTION_START_OTA = "rtk_ACTION_START_OTA"
    }

}
