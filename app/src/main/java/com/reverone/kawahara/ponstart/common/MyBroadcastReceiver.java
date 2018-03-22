package com.reverone.kawahara.ponstart.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * ブロードキャストメッセージの送受信
 * Created by kawahara on 2017/05/28.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {

    private static String ACTION_INVOKED;
    private static final String ID = "ID";
    private static final String ARG = "ARG";

    public static void initialize(String actionInvokedKey) {
        ACTION_INVOKED = actionInvokedKey;
    }

    public interface Callback {
        void onEventInvoked(int id, final Bundle arg);
    }

    private Callback _callback;
    private LocalBroadcastManager _manager;

    // コンストラクタ
    private MyBroadcastReceiver(Context context, Callback callback) {
        super();
        _callback = callback;
        _manager = LocalBroadcastManager.getInstance(context.getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INVOKED);

        _manager.registerReceiver(this, filter);
    }

    public static MyBroadcastReceiver register(Context context, Callback callback) {
        return new MyBroadcastReceiver(context, callback);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_INVOKED.equals(action)) {
            int id = intent.getIntExtra(ID, 0);
            Bundle arg = intent.getBundleExtra(ARG);
            _callback.onEventInvoked(id, arg);
        }
    }

    public static void sendBroadcast(Context context, int itemId, final Bundle arg) {
        Intent intent = new Intent(ACTION_INVOKED);
        intent.putExtra(ID, itemId);
        intent.putExtra(ARG, arg);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context.getApplicationContext());
        manager.sendBroadcast(intent);
    }

    public void unregister() {
        _manager.unregisterReceiver(this);
    }
}
