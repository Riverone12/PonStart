package com.reverone.kawahara.ponstart.common;

import android.app.Application;
import android.content.Context;

/**
 * Application 開始時と終了時の処理を定義
 * Created by kawahara on 2017/05/28.
 */

public class MyApplication extends Application {
    private static MyApplication _instance;

    public static Context getContext() {
        if (_instance == null) {
            throw new RuntimeException("MyApplication should be initialized.");
        }
        return _instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
    }
}
