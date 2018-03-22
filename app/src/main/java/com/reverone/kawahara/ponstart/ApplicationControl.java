package com.reverone.kawahara.ponstart;

import com.reverone.kawahara.ponstart.common.FontSizeAdjuster;
import com.reverone.kawahara.ponstart.common.MyApplication;
import com.reverone.kawahara.ponstart.common.MyBroadcastReceiver;

/**
 * Application 開始時と終了時の処理を定義
 * Created by kawahara on 2017/05/24.
 */

public class ApplicationControl extends MyApplication {
    private MyNotification _notification;

    @Override
    public void onCreate() {
        super.onCreate();

        // 設定項目を初期化
        AppPreference.initialize(getContext());

        FontSizeAdjuster.initialize(getContext());

        // ブロードキャストの送受信の準備
        MyBroadcastReceiver.initialize("com.reverone.kawahara.ponstart");

        // Notification の準備
        _notification = new MyNotification();
        _notification.onStart();
    }

    @Override
    public void onTerminate() {
        _notification.onTerminate();
        super.onTerminate();
    }
}
