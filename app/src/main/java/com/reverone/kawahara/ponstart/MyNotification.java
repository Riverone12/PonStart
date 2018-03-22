package com.reverone.kawahara.ponstart;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.reverone.kawahara.ponstart.common.MyBroadcastReceiver;

/**
 * 通知領域への状態表示
 * Created by kawahara on 2017/05/24.
 */

class MyNotification {
    private static final String FROM_NOTIFICATION_INTENT_TAG = "FromNotificationIntentTAG";
    private static final int NOTIFICATION_ID = R.id.song_controller_layout;

    private NotificationManagerCompat _notificationManager;
    private Notification _notification;
    private NotificationCompat.Builder _notificationBuilder;
    private PendingIntent _fromNotifyIntent;
    private Bitmap _notifyLargeIcon;
    private String _notifyTextPlaying;
    private String _notifyTextPausing;

    // メディアプレーヤーサービスからのメッセージ受信用
    private MyBroadcastReceiver _receiver;

    MyNotification()  {
        prepareNotify();
    }

    void onStart() {
        // プレーヤーからのブロードキャストを受け取る準備
        _receiver = MyBroadcastReceiver.register(ApplicationControl.getContext(), new MyBroadcastReceiver.Callback() {
            @Override
            public void onEventInvoked(int id, final Bundle arg) {
                final SoundInformation info = MediaPlayerService.extractSoundInformation(arg);
                if (info != null) {
                    updateNotification(info);
                }
            }
        });
    }

    void onTerminate() {
        _receiver.unregister();
        _receiver = null;

        cancelNotification();
    }

    private void cancelNotification() {
        _notificationManager.cancel(NOTIFICATION_ID);
    }

    private void updateNotification(SoundInformation info) {
        // ビューを更新する
        final int status = info.getPayerStatus();
        if (status != MediaPlayerService.STATUS_STARTED
                && status != MediaPlayerService.STATUS_PAUSED) {
            cancelNotification();
            return;
        }

        final String contentText = (status == MediaPlayerService.STATUS_STARTED)
                ? _notifyTextPlaying
                : _notifyTextPausing;

        Context context = ApplicationControl.getContext();
        final String current = info.getCurrentTimeString(context);
        final String total = info.getTotalTimeString(context);

        _notificationBuilder.setContentIntent(_fromNotifyIntent)
                .setContentTitle(info.getTitle())
                .setContentText(contentText)
                .setContentInfo(current + " / " + total)
                .setLargeIcon(_notifyLargeIcon)
                .setSmallIcon(R.drawable.ic_action_headphones_d);

        _notification = _notificationBuilder.build();
        _notification.flags = Notification.FLAG_NO_CLEAR;
        _notificationManager.notify(NOTIFICATION_ID, _notification);
    }

    // Notification のリソースをあらかじめ用意しておく
    private void prepareNotify() {
        if (_notification == null) {
            Context context = ApplicationControl.getContext();

            //_notifyLargeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_headphones);

            _notificationManager = (NotificationManagerCompat.from(context));
            _notificationBuilder = new NotificationCompat.Builder(context);

            _notifyTextPlaying = context.getString(R.string.notify_text_playing);
            _notifyTextPausing = context.getString(R.string.notify_text_pausing);

            // タップされた時に発行するIntent を準備する
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(FROM_NOTIFICATION_INTENT_TAG, 123);
            _fromNotifyIntent = PendingIntent.getActivity(
                    context, 1, intent, 0);
        }
    }
}
