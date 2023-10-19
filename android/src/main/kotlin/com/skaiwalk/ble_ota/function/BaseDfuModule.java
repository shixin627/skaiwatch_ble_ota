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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.skaiwalk.ble_ota.settings.AppSettingsHelper;
import com.realsil.sdk.core.logger.ZLogger;
import com.realsil.sdk.dfu.model.BinInfo;
import com.realsil.sdk.dfu.model.DfuConfig;
import com.skaiwalk.ble_ota.settings.SettingsHelper;
import com.realsil.sdk.support.file.AssetsFileDialogFragment;
import com.realsil.sdk.support.file.RxFiles;
import com.realsil.sdk.support.utilities.PermissionUtil;
/**
 * @author shixin_lin@skaiwalk.com
 */
public abstract class BaseDfuModule {
    private Context context;
    protected String mTitle;
    protected String mSubTitle;
    private RxFiles rxFiles;
    protected String mFilePath;
    protected BinInfo mBinInfo;
    protected DfuConfig mDfuConfig;

    protected DfuConfig getDfuConfig() {
        if (mDfuConfig == null) {
            mDfuConfig = new DfuConfig();
        }
        return mDfuConfig;
    }

    public void refresh() {
        refresh(false);
    }

    public void refresh(boolean forceLoad) {
    }

    public boolean isOtaProcessing() {
        return false;
    }

    protected void sendMessage(Handler handler, int what) {
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(what));
        } else {
            ZLogger.d("handler is null");
        }
    }

    protected void sendMessage(Handler handler, int what, Object obj) {
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(what, obj));
        } else {
            ZLogger.d("handler is null");
        }
    }

    protected void openFileChooser() {
        if (AppSettingsHelper.Companion.getInstance().getFileLocation() == DfuConfig.FILE_LOCATION_ASSETS) {
            Bundle args = new Bundle();
            args.putString(AssetsFileDialogFragment.EXTRA_KEY_FILE_DIR, "images/");
            args.putString(AssetsFileDialogFragment.EXTRA_KEY_FILE_EXTENSION, SettingsHelper.Companion.getInstance().getFileSuffix());
        } else {
//            if (!PermissionUtil.checkStoragePermission(this)) {
//                PermissionUtil.requestStoragePermission(this);
////                return;
//            }
            if (rxFiles == null) {
                return;
            }
            try {
                //error: cannot access Observable
                //                rxFiles.request(Intent.ACTION_GET_CONTENT,
                //                               ^
                //  class file for io.reactivex.Observable not found
//                rxFiles.request(Intent.ACTION_GET_CONTENT,
//                                AppSettingsHelper.Companion.getInstance().getSelectFileType());
//                        .subscribe(s -> {
//                                    mFilePath = s;
//                                    mBinInfo = null;
//                                    refresh();
//                                },
//                                t -> ZLogger.w("onError: " + t.toString()),
//                                () -> ZLogger.v("OnComplete"));
            } catch (Exception e) {
                ZLogger.e(e.toString());
            }
        }
    }

    public void processBackconnect() {

    }

    protected Handler mBankLinkHandler = new Handler(Looper.myLooper());
    private static final int BANK_LINK_WAIT_TIME = 400;
    private Object bankLinkLock = new Object();

    protected void blockBankLink(long timeout) {
        synchronized (bankLinkLock) {
            try {
                bankLinkLock.wait(timeout);
            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
            }
        }
    }

    protected void notifyBankLink() {
        synchronized (bankLinkLock) {
            try {
                bankLinkLock.notifyAll();
            } catch (Exception e) {
                ZLogger.w(e.toString());
            }
        }
    }

    private Runnable bankLinkRunnable = () -> {
        processBackconnect();
    };

    protected void setBankLinkEnbled(boolean enbled) {
        if (mBankLinkHandler == null) {
            return;
        }

        if (enbled) {
            if (AppSettingsHelper.Companion.getInstance().isDfuBankLinkEnabled()) {
                mBankLinkHandler.postDelayed(bankLinkRunnable, BANK_LINK_WAIT_TIME);
            }
        } else {
            mBankLinkHandler.removeCallbacks(bankLinkRunnable);
        }
    }
}
