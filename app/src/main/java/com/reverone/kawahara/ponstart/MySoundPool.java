package com.reverone.kawahara.ponstart;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

/**
 * SoundPool のラッパークラス
 * Created by kawahara on 2017/05/16.
 * 2017.10.25 J.Kawahara 不要なログ出力を削除
 */

class MySoundPool {
    private SoundPool _soundPool;
    private SoundPool.OnLoadCompleteListener _listener;
    private boolean _initialized;
    private int _maxSoundCount;
    private int _registeredSoundCount;

    int load(Context context, final int fileType, final String filename) {
        if (!_initialized) {
            return 0;
        }
        if (_registeredSoundCount >= _maxSoundCount) {
            return -1;
        }
        int soundId = 0;
        if (fileType == SoundData.FILE_TYPE_DEMO) {
            // デモデータ リソースファイルから効果音データを取得する
            Resources resources = context.getResources();
            final String packageName = context.getPackageName();
            final int id = resources.getIdentifier(filename, "raw", packageName);
            if (id > 0) {
                soundId = _soundPool.load(context, id, 1);
            }
        }
        else if (fileType == SoundData.FILE_TYPE_FILE) {
            // メディアファイルをロードする
            soundId = _soundPool.load(filename, 1);
        }
        ++_registeredSoundCount;
        return soundId;
    }

    void unload(int soundId) {
        if (_initialized) {
            _soundPool.unload(soundId);
        }
    }

    void play(int soundId, int leftVolume, int rightVolume) {
        if (_soundPool == null || !_initialized) {
            return;
        }
        final float leftVol = (float) leftVolume / 100.0f;
        final float rightVol = (float) rightVolume / 100.0f;
        _soundPool.play(
                soundId,
                leftVol,
                rightVol,
                0,
                0,
                1.0f);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void initialize(int maxSoundCount) {
        if (_initialized) {
            return;
        }
        _maxSoundCount = maxSoundCount;
        _registeredSoundCount = 0;

        // SoundPool の初期化
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            _soundPool = new SoundPool(_maxSoundCount, AudioManager.STREAM_MUSIC, 0);
        }
        else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            _soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(_maxSoundCount)
                    .build();
        }
        // 効果音データの読み込みが完了した後の処理
        _soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (_listener != null) {
                    _listener.onLoadComplete(soundPool, sampleId, status);
                }
            }
        });

        _initialized = true;
    }

    void release() {
        _soundPool.release();
        _registeredSoundCount = 0;
        _initialized = false;
    }

    // 効果音データの読み込みが完了した後の処理を登録する
    void setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener listener) {
        _listener = listener;
    }
}
