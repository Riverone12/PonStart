package com.reverone.kawahara.ponstart;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.reverone.kawahara.ponstart.common.MyBroadcastReceiver;

/**
 * メディアプレーヤーのコントローラー
 * Created by kawahara on 2017/05/25.
 * 2017.10.26 J.Kawahara フェードアウト機能に対応
 */

class SongController {
    private Button _buttonPlayPause;

    private TextView _textViewTitle;
    private TextView _textViewRemainTime;
    private TextView _textViewCurrentTime;

    private TextView _textViewRepeatMode;
    private TextView _textViewQuickMode;

    private static final int BUTTON_MODE_PLAY = 0;
    private static final int BUTTON_MODE_PAUSE = 1;

    private SoundInformation _soundInformation = null;
    private static final String KEY_SOUND_INFORMATION = "SongController_SoundInformation";

    void initialize(View view) {
        _buttonPlayPause = (Button) view.findViewById(R.id.controller_button_play);
        Button buttonStop = (Button) view.findViewById(R.id.controller_button_stop);
        Button buttonRewind = (Button) view.findViewById(R.id.controller_button_previous);

        _textViewTitle = (TextView) view.findViewById(R.id.display_title);
        _textViewCurrentTime = (TextView) view.findViewById(R.id.display_currentTime);
        _textViewRemainTime = (TextView) view.findViewById(R.id.display_remainTime);

        _textViewRepeatMode = (TextView) view.findViewById(R.id.display_repeatMode);
        _textViewQuickMode = (TextView) view.findViewById(R.id.display_quickMode);

        _buttonPlayPause.setTag(BUTTON_MODE_PLAY);

        _buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals(BUTTON_MODE_PLAY)) {
                    sendMessage(MediaPlayerService.ACTION_START);
                }
                else {
                    sendMessage(MediaPlayerService.ACTION_PAUSE);
                }
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sendMessage(MediaPlayerService.ACTION_STOP);
                sendMessage(MediaPlayerService.ACTION_FADE_OUT, AppPreference.getFadeoutSec());
            }
        });

        buttonRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(MediaPlayerService.ACTION_SEEK_TO);
            }
        });
    }

    // コントロールの状態を変化させる
    private void setControlStatus(final SoundInformation soundInformation) {
        Context context = ApplicationControl.getContext();
        _soundInformation = soundInformation;

        final int status = soundInformation.getPayerStatus();

        // 再生時間の表示を変更する
        updateCurrentTime(soundInformation);

        // タイトルの表示を変更する
        String caption;
        if (status == MediaPlayerService.STATUS_IDLE) {
            caption = context.getString(R.string.display_title_no_data);
        }
        else if (status == MediaPlayerService.STATUS_PREPARING
                || status == MediaPlayerService.STATUS_INITIALIZED) {
            caption = context.getString(R.string.display_title_loading);
        }
        else {
            caption = soundInformation.getTitle();
            if (caption == null) {
                caption = context.getString(R.string.display_title_no_title);
            }
        }
        if (!caption.equals(_textViewTitle.getText())) {
            _textViewTitle.setText(caption);
        }

        // 再生ボタンの表示を変更する
        final int buttonMode =
                (status == MediaPlayerService.STATUS_STARTED)
                        ? BUTTON_MODE_PAUSE
                        : BUTTON_MODE_PLAY;
        if (!_buttonPlayPause.getTag().equals(buttonMode)) {
            if (buttonMode == BUTTON_MODE_PLAY) {
                _buttonPlayPause.setTag(BUTTON_MODE_PLAY);
                _buttonPlayPause.setText(context.getString(R.string.controller_button_play));
                _buttonPlayPause.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_action_play, 0, 0);
            }
            else {
                _buttonPlayPause.setTag(BUTTON_MODE_PAUSE);
                _buttonPlayPause.setText(context.getString(R.string.controller_button_pause));
                _buttonPlayPause.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_action_pause, 0, 0);
            }
        }
    }

    // 再生時間を表示する
    private void updateCurrentTime(final SoundInformation info) {
        Context context = ApplicationControl.getContext();
        final String current = info.getCurrentTimeString(context);
        final String total = info.getTotalTimeString(context);
        final String remain = info.getRemainTimeString(context);

        _textViewCurrentTime.setText(current + " / " + total);
        _textViewRemainTime.setText(remain);
    }

    // メディアプレーヤーサービスとの通信関連
    private MyBroadcastReceiver _receiver;

    // サービスへの接続
    void start() {
        Context context = ApplicationControl.getContext();

        // メディアプレーヤーサービスを開始
        // またはすでに起動しているサービスとの接続を開始する
        MediaPlayerBinder binder = MediaPlayerBinder.getInstance();
        binder.connect(context);

        // プレーヤーからのブロードキャストを受け取る準備
        _receiver = MyBroadcastReceiver.register(context, new MyBroadcastReceiver.Callback() {
            @Override
            public void onEventInvoked(int id, Bundle arg) {
                if (id == MediaPlayerService.NOTIFY_CHANGE_STATUS) {
                    // 状態の変更
                    final SoundInformation info = MediaPlayerService.extractSoundInformation(arg);
                    if (info != null) {
                        setControlStatus(info);
                    }
                }
                else if (id == MediaPlayerService.NOTIFY_CHANGE_SETTING) {
                    // 設定値の変更
                    displaySettingMode();
                }
            }
        });
        // メディアプレーヤーの現在の状況をリクエストする
        binder.sendMessage(MediaPlayerService.ACTION_GET_STATUS);

        displaySettingMode();
    }

    private void displaySettingMode() {
        final int repeatModeVisible = AppPreference.getIsLoopMode()
                ? View.VISIBLE
                : View.INVISIBLE;
        _textViewRepeatMode.setVisibility(repeatModeVisible);

        final int quickModeVisible = AppPreference.getIsQuickStartMode()
                ? View.VISIBLE
                : View.INVISIBLE;
        _textViewQuickMode.setVisibility(quickModeVisible);
    }

    private boolean isRunning() {
        if (_soundInformation == null) {
            return false;
        }
        final int status = _soundInformation.getPayerStatus();
        return (status == MediaPlayerService.STATUS_STARTED
                || status == MediaPlayerService.STATUS_PAUSED);
    }

    void onRestoreInstanceState(Bundle savedInstanceState) {
        // 前回のアプリケーション停止時にロード済みのデータが存在する場合はロードし直す
        if (savedInstanceState != null
                && savedInstanceState.containsKey(KEY_SOUND_INFORMATION)) {
            SoundInformation soundInformation = savedInstanceState.getParcelable(KEY_SOUND_INFORMATION);
            if (soundInformation != null) {
                MediaPlayerBinder binder = MediaPlayerBinder.getInstance();
                binder.reservePrepare(soundInformation.toSoundData());
            }
        }
    }

    void onSaveInstanceState(Bundle outState) {
        if (_soundInformation != null && !isRunning()) {
            final String fileName = _soundInformation.toSoundData().getFileName();
            if (fileName != null && !fileName.isEmpty()) {
                // メディアプレーヤーサービスが再生中でも一時停止中でもない
                // ロード済みのソングデータを保存する
                outState.putParcelable(KEY_SOUND_INFORMATION, _soundInformation);
            }
        }
    }

    // サービスからの切断
    void stop() {
        if (_receiver != null) {
            _receiver.unregister();
            _receiver = null;
        }

        if (_soundInformation != null && !isRunning()) {
            // メディアプレーヤーサービスが再生中でも一時停止中でもない
            // サービスを停止する
            MediaPlayerBinder binder = MediaPlayerBinder.getInstance();
            binder.disconnect(ApplicationControl.getContext());
        }
    }

    // メッセージを送信する
    private void sendMessage(int actionId) {
        MediaPlayerBinder binder = MediaPlayerBinder.getInstance();
        binder.sendMessage(actionId);
    }

    private void sendMessage(int actionId, int value) {
        MediaPlayerBinder binder = MediaPlayerBinder.getInstance();
        Message message = Message.obtain(null, actionId);
        message.arg1 = value;
        binder.sendMessage(message);
    }
}
