package com.reverone.kawahara.ponstart;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.reverone.kawahara.ponstart.common.MyBroadcastReceiver;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * メディアプレーヤーのラッパークラス
 * Created by kawahara on 2017/05/24.
 * 2017.5.31 J.Kawahara リピート再生機能関連の実装
 * 2017.10.25 J.Kawahara 不要なログ出力を削除
 * 2017.10.26 J.Kawahara フェードアウト機能を実装
 */

public class MediaPlayerService extends Service {

    private MediaPlayer _mediaPlayer;
    private SoundInformation _soundInformation;
    private boolean _isLooping = false;

    private Handler _playTimeChecker;
    private static final int POLLING_TIME = 1000;

    static final String BROADCAST_ARG_KEY = "SoundInformation";

    // フェードアウト関連
    private MediaPlayerFadeOut _taskFadeout = null;

    // 状態関連
    private int _status;
    static final int STATUS_IDLE = 0;
    static final int STATUS_INITIALIZED = 1;
    static final int STATUS_PREPARING = 2;
    static final int STATUS_PREPARED = 3;
    static final int STATUS_STARTED = 4;
    static final int STATUS_PAUSED = 5;
    static final int STATUS_STOPPED = 6;
    static final int STATUS_PLAYBACK_COMPLETED = 7;
    static final int STATUS_END = 8;
    static final int STATUS_ERROR = 9;

    // 操作関連
    static final int ACTION_RESET = 100;
    static final int ACTION_PREPARE = 101;
    static final int ACTION_START = 102;
    static final int ACTION_PAUSE = 103;
    static final int ACTION_STOP = 104;
    static final int ACTION_SEEK_TO = 105;
    static final int ACTION_RELEASE = 106;
    static final int ACTION_FADE_OUT = 107;
    static final int ACTION_SET_LOOPING_FALSE = 110;
    static final int ACTION_SET_LOOPING_TRUE = 111;
    static final int ACTION_GET_STATUS = 198;

    // 応答・通知関連
    static final int NOTIFY_CHANGE_STATUS = 200;
    static final int NOTIFY_CHANGE_SETTING = 201;

    public MediaPlayerService() {
        _status = STATUS_IDLE;
        _soundInformation = new SoundInformation();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _messenger = new Messenger(new MessageHandler(this));
        initializeMediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // START_NOT_STICKY
        // サービスを起動するペンディングインテントが存在しない限りサービスは再起動されない
        // 強制終了によりサービスが終了した場合、勝手な再起動を防ぐ
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        cancelFadeOut();
        release();
        super.onDestroy();
    }

    // 操作関連
    private void reset() {
        cancelFadeOut();
        if (_mediaPlayer != null) {
            _mediaPlayer.reset();
            changeTo(STATUS_IDLE);
        }
        _soundInformation.clear();
    }

    // 曲の準備
    private boolean setDataSource(SoundData soundData) {
        if (_status != STATUS_IDLE) {
            reset();
        }
        if (_mediaPlayer == null
                || soundData == null) {
            return false;
        }
        File file = new File(soundData.getFileName());
        if (!file.exists()) {
            return false;
        }

        try {
            _mediaPlayer.setDataSource(soundData.getFileName());
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        _soundInformation.setSoundData(soundData);
        _soundInformation.setPlayerStatus(STATUS_INITIALIZED);
        _status = STATUS_INITIALIZED;
        return true;
    }

    private void prepare(SoundData soundData) {
        if (!setDataSource(soundData)) {
            return;
        }
        prepare();
    }

    private void prepare() {
        cancelFadeOut();
        if (_status == STATUS_INITIALIZED
                || _status == STATUS_STOPPED) {

            changeTo(STATUS_PREPARING);
            if (_mediaPlayer != null) {
                _mediaPlayer.prepareAsync();
            }
        }
    }

    // 再生開始
    private void start() {
        if (_mediaPlayer == null) {
            return;
        }
        if (_taskFadeout != null) {
            cancelFadeOut();
        }
        if (_status == STATUS_PREPARED
                || _status == STATUS_PAUSED
                || _status == STATUS_PLAYBACK_COMPLETED) {

            _mediaPlayer.start();
            changeTo(STATUS_STARTED);

            // 再生時間を通知する
            if (_playTimeChecker == null) {
                _playTimeChecker = new Handler();
            }
            else {
                _playTimeChecker.removeCallbacksAndMessages(null);
            }
            _playTimeChecker.postDelayed(new Runnable() {
                @Override
                public void run() {
                    broadcastStatus();
                    //updateNotification();
                    if (_status == STATUS_STARTED) {
                        _playTimeChecker.postDelayed(this, POLLING_TIME);
                    }
                }
            }, POLLING_TIME);
        }
    }

    // 一時停止
    private void pause() {
        if (_taskFadeout != null) {
            // フェードアウトの途中では無視する
            return;
        }
        if (_status == STATUS_STARTED) {
            if (_mediaPlayer != null) {
                _mediaPlayer.pause();
            }
            changeTo(STATUS_PAUSED);
        }
    }

    // フェードアウト
    private void fadeOut(int fadeMilliSec) {
        if (_taskFadeout != null) {
            // フェードアウト中の場合、即停止する
            stop();
        } else if (_status == STATUS_PAUSED) {
            // 一時中止中の場合、即停止する
            stop();
        } else if (_status == STATUS_PREPARED
                || _status == STATUS_STARTED
                || _status == STATUS_PLAYBACK_COMPLETED) {

            if (fadeMilliSec <= 0) {
                stop();
            } else {
                // フェードアウトを開始する
                _taskFadeout =
                        (MediaPlayerFadeOut) new MediaPlayerFadeOut(this, fadeMilliSec).execute();
            }
        }
    }

    // フェードアウトをキャンセルする
    private void cancelFadeOut() {
        if (_taskFadeout != null) {
            _taskFadeout.cancel(true);
            _taskFadeout = null;
        }
    }

    // 停止
    private void stop() {
        cancelFadeOut();
        if (_status == STATUS_PREPARED
                || _status == STATUS_STARTED
                || _status == STATUS_PAUSED
                || _status == STATUS_PLAYBACK_COMPLETED) {
            if (_mediaPlayer != null) {
                _mediaPlayer.stop();
            }
            changeTo(STATUS_STOPPED);

            try {
                Thread.sleep(300);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 次の再生のためにprepareをしておく
            // STOPPED → STARTED へは直接移行できない
            // _mediaPlayer.reset();
            prepare();
        }
    }

    // シーク
    private void seekTo(int msec) {
        cancelFadeOut();
        if (_status == STATUS_PREPARED
                || _status == STATUS_STARTED
                || _status == STATUS_PAUSED
                || _status == STATUS_STOPPED
                || _status == STATUS_PLAYBACK_COMPLETED) {
            if (_mediaPlayer != null) {
                _mediaPlayer.seekTo(msec);
            }
        }
    }

    // リピート再生の有無を設定する
    private void setLooping(boolean looping) {
        _isLooping = looping;

        if (_mediaPlayer != null) {
            _mediaPlayer.setLooping(looping);
            broadcastChangedPreference();
        }
    }

    // リソースを解放する
    private void release() {
        cancelFadeOut();
        if (_status == STATUS_STARTED) {
            stop();
        }
        changeTo(STATUS_END);
        if (_mediaPlayer != null) {
            _mediaPlayer.release();
            _mediaPlayer = null;
        }
    }

    // メディアプレーヤーの準備
    private void initializeMediaPlayer() {
        _mediaPlayer = new MediaPlayer();

        // 曲データ準備完了時の処理
        _mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                _soundInformation.setTotalTime(mp.getDuration());

                float volume = _soundInformation.getVolume() / 100.0f;
                _mediaPlayer.setVolume(volume, volume);
                _mediaPlayer.setLooping(_isLooping);

                changeTo(STATUS_PREPARED);
                seekTo(0);
            }
        });

        // 演奏終了後の処理
        _mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!mp.isLooping()) {
                    changeTo(STATUS_PLAYBACK_COMPLETED);
                }
            }
        });

        // エラー発生時の処理
        _mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                changeTo(STATUS_ERROR);
                return false;
            }
        });

        // シークが完了したときの処理
        _mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                broadcastStatus();
            }
        });

    }

    private void changeTo(int status) {
        if (status != _status) {
            _status = status;
            _soundInformation.setPlayerStatus(_status);
            broadcastStatus();
        }
    }

    // 現在の状態をブロードキャストする
    private void broadcastStatus() {
        _soundInformation.setCurrentTime(getCurrentPosition());
        Bundle arg = new Bundle();
        arg.putParcelable(BROADCAST_ARG_KEY, _soundInformation);
        MyBroadcastReceiver.sendBroadcast(getApplicationContext(), NOTIFY_CHANGE_STATUS, arg);
    }

    // 設定の変更をブロードキャストする
    private void broadcastChangedPreference() {
        MyBroadcastReceiver.sendBroadcast(getApplicationContext(), NOTIFY_CHANGE_SETTING, null);
    }

    static SoundInformation extractSoundInformation(final Bundle arg) {
        if (arg == null || !arg.containsKey(BROADCAST_ARG_KEY)) {
            return null;
        }
        return arg.getParcelable(BROADCAST_ARG_KEY);
    }

    private int getCurrentPosition() {
        int currentPosition = 0;
        if (_mediaPlayer != null
                && (_status >= STATUS_PREPARED && _status <= STATUS_PLAYBACK_COMPLETED)) {
            currentPosition = _mediaPlayer.getCurrentPosition();
        }
        return currentPosition;
    }

    // クライアントとの通信関連
    private Messenger _messenger;
    static final String KEY_SOUND_DATA = "KeySoundData";

    private static class MessageHandler extends Handler {
        private MediaPlayerService _service;

        MessageHandler(MediaPlayerService service) {
            _service = service;
        }

        @Override
        public void handleMessage(Message message) {
            switch(message.what) {
                case ACTION_RESET:
                    _service.reset();
                    break;
                case ACTION_PREPARE:
                    Bundle bundle = message.getData();
                    if (bundle != null && bundle.containsKey(KEY_SOUND_DATA)) {
                        SoundData soundData = bundle.getParcelable(KEY_SOUND_DATA);
                        _service.prepare(soundData);
                    }
                    break;
                case ACTION_START:
                    _service.start();
                    break;
                case ACTION_PAUSE:
                    _service.pause();
                    break;
                case ACTION_STOP:
                    _service.stop();
                    break;
                case ACTION_SEEK_TO:
                    int msec = message.arg1;
                    _service.seekTo(msec);
                    break;
                case ACTION_SET_LOOPING_FALSE:
                    _service.setLooping(false);
                    break;
                case ACTION_SET_LOOPING_TRUE:
                    _service.setLooping(true);
                    break;
                case ACTION_RELEASE:
                    _service.release();
                    break;
                case ACTION_FADE_OUT:
                    int fadeMilliSec = message.arg1;
                    _service.fadeOut(fadeMilliSec);
                    break;
                case ACTION_GET_STATUS:
                    _service.broadcastStatus();
                    break;
                default:
                    super.handleMessage(message);
                    break;
            }

            Messenger reply = message.replyTo;
            if (reply != null) {
                try {
                    reply.send(Message.obtain(null, ACTION_GET_STATUS, _service._status, 0));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // フェードアウト
    static private class MediaPlayerFadeOut extends AsyncTask<String, Integer, String> {

        private final WeakReference<MediaPlayerService> _service;

        private int _fadeMilliSec;

        MediaPlayerFadeOut(MediaPlayerService service, int fadeMilliSec) {
            _service = new WeakReference<>(service);
            _fadeMilliSec = fadeMilliSec;
        }

        @Override
        protected String doInBackground(String... args) {
            MediaPlayerService mp = _service.get();
            int loopTime = 30;

            if (mp != null) {
                float level = mp._soundInformation.getVolume() / 100.0f;
                int waitTime = _fadeMilliSec / loopTime;

                float per = 0.9f;

                int i = 1;
                while (i < loopTime) {
                    i++;

                    if (isCancelled()) {
                        break;
                    }

                    if (mp._mediaPlayer != null) {
                        level = level * per;
                        mp._mediaPlayer.setVolume(level, level);
                    }
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!isCancelled() && mp._mediaPlayer != null) {
                    mp._mediaPlayer.setVolume(0.0f, 0.0f);
                }
            }
            return "dummy";
        }

        @Override
        protected void onPostExecute(String dummy) {
            MediaPlayerService mp = _service.get();
            if (mp != null) {
                mp.stop();
            }
        }
    }
}
