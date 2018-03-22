package com.reverone.kawahara.ponstart;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * MedidaPlayerSevice との通信関連
 * Created by kawahara on 2017/05/24.
 * 2017.10.25 J.Kawahara 不要なログ出力を削除
 */

class MediaPlayerBinder implements ServiceConnection {
    private static MediaPlayerBinder _instance;
    static MediaPlayerBinder getInstance() {
        if (_instance == null) {
            _instance = new MediaPlayerBinder();
        }
        return _instance;
    }

    private Messenger _messenger;
    private Messenger _replyMessenger;
    private static final String packageName = "com.reverone.kawahara.ponstart";
    private SoundData _reservedSoundData;

    interface ReplyListener {
        void onReceive(Message message);
    }
    private List<ReplyListener> _replyListenerList;

    private MediaPlayerBinder() {
        _replyListenerList = new ArrayList<>();
        _reservedSoundData = null;
    }

    // 曲の準備を予約しておく
    void reservePrepare(final SoundData soundData) {
        _reservedSoundData = soundData;
    }

    // サービスとの接続が確立できたときの処理
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        _messenger = new Messenger(service);
        _replyMessenger = new Messenger(new ReplyHandler(this));

        // リピート再生モードの有無を設定する
        final int repeatMode = AppPreference.getIsLoopMode()
                ? MediaPlayerService.ACTION_SET_LOOPING_TRUE
                : MediaPlayerService.ACTION_SET_LOOPING_FALSE;
        sendMessage(repeatMode);

        // 予約済みの曲の準備をリクエストする
        if (_reservedSoundData != null) {
            sendMessage(
                    MediaPlayerService.ACTION_PREPARE,
                    MediaPlayerService.KEY_SOUND_DATA,
                    _reservedSoundData);
            _reservedSoundData = null;
        }
        else {
            sendMessage(MediaPlayerService.ACTION_GET_STATUS);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        _messenger = null;
        _replyMessenger = null;
    }

    private static class ReplyHandler extends Handler {
        private MediaPlayerBinder _handler;

        ReplyHandler(MediaPlayerBinder handler) {
            _handler = handler;
        }

        @Override
        public void handleMessage(Message message) {
            for (ReplyListener listener : _handler._replyListenerList) {
                listener.onReceive(message);
            }
        }
    }

    // サービスからの応答を監視するオブジェクトを登録する
    void addReplyListener(ReplyListener replyListener) {
        _replyListenerList.add(replyListener);
    }

    // 接続を開始する
    void connect(Context context) {
        // すでにサービスが起動中かどうかを確認する
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final String className = MediaPlayerService.class.getName();
        boolean isRunning = false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (className.equals(service.service.getClassName())) {
                isRunning = true;
                break;
            }
        }

        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setPackage(packageName);
        if (!isRunning) {
            // サービスが起動していない場合は、サービスを開始する
            context.startService(intent);
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    // 接続を切断する
    void disconnect(Context context) {
        if (_messenger != null) {
            context.unbindService(this);
            _messenger = null;
            _replyMessenger = null;
        }
        // サービスを停止する
        Intent intent = new Intent(context, MediaPlayerService.class);
        context.stopService(intent);
    }

    // メッセージを送信する
    void sendMessage(int actionId) {
        sendMessage(Message.obtain(null, actionId));
    }

    void sendMessage(int actionId, String key, Parcelable parcelable) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(key, parcelable);
        Message message = Message.obtain(null, actionId);
        message.setData(bundle);
        sendMessage(message);
    }

    void sendMessage(Message message) {
        if (_messenger != null) {
            if (_replyListenerList.size() > 0) {
                message.replyTo = _replyMessenger;
            }

            try {
                _messenger.send(message);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
