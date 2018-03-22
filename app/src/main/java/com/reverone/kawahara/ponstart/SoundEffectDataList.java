package com.reverone.kawahara.ponstart;

import android.content.Context;
import android.media.SoundPool;

/**
 * 効果音データの配列
 * Created by kawahara on 2017/05/15.
 */

class SoundEffectDataList extends SoundDataList {
    private MySoundPool _soundPool;

    // シングルトンとして実装する
    private static SoundEffectDataList _instance;
    static SoundEffectDataList getInstance() {
        if (_instance == null) {
            _instance = new SoundEffectDataList();
        }
        return _instance;
    }

    private SoundEffectDataList() {
        super(SoundData.SE_TABLE_NAME);
        _soundPool = new MySoundPool();
    }

    @Override
    void erase(final SoundData soundData) {
        final int position = soundData.getPosition();
        SoundData eraseData = getSoundData(position);
        eraseData.setStandBy(false);
        if (eraseData.getSoundId() != 0) {
            _soundPool.unload(eraseData.getSoundId());
        }
        eraseData.erase();
        modify(eraseData);
    }

    @Override
    void modify(final SoundData soundData) {
        super.modify(soundData);
        SoundEffectDataList dataList = SoundEffectDataList.getInstance();
        dataList.reloadSoundEffect(ApplicationControl.getContext(), soundData.getPosition());
    }

    // 効果音のプレーヤー
    private static final int MAX_SOUND_COUNT = 32;
    //private static int _registeredSoundCount;
    //private boolean _initializedSoundEffect = false;

    private void reloadSoundEffect(Context context, int position) {
        SoundData soundData = getSoundData(position);
        soundData.setStandBy(false);
        if (soundData.getSoundId() != 0) {
            _soundPool.unload(soundData.getSoundId());
            soundData.setSoundId(0);
        }
        if (soundData.isEmpty()) {
            return;
        }
        int soundId = _soundPool.load(context, soundData.getFileType(), soundData.getFileName());
        if (soundId < 0) {
            // SoundPool に登録できる最大数を超えた
            // SoundPool をいったん破棄して、最初から登録をやり直す
            terminateSoundEffect();
            initializeSoundEffect(context);
        }
        else {
            soundData.setSoundId(soundId);
        }
    }

    void initializeSoundEffect(Context context) {
        _soundPool.initialize(MAX_SOUND_COUNT);

        // 効果音データの読み込みが完了した後の処理を登録する
        _soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                for (int i = 0; i < size(); ++i) {
                    SoundData soundData = getSoundData(i);
                    if (soundData.getSoundId() == sampleId) {
                        soundData.setStandBy(status == 0);
                        if (_soundPoolListener != null) {
                            _soundPoolListener.onLoadComplete(soundData.getPosition(), soundData);
                        }
                        break;
                    }
                }
            }
        });

        // 効果音データをメモリ上にロードする
        for (int i = 0; i < size(); ++i) {
            SoundData soundData = getSoundData(i);
            soundData.setStandBy(false);
            final int soundId = _soundPool.load(context, soundData.getFileType(), soundData.getFileName());
            soundData.setSoundId(soundId);
        }
    }

    void terminateSoundEffect() {
        for (int i = 0; i < size(); ++i) {
            SoundData soundData = getSoundData(i);
            soundData.setSoundId(0);
            soundData.setStandBy(false);
        }
        _soundPool.release();
    }

    void playSoundEffect(final SoundData soundData) {
        _soundPool.play(
                soundData.getSoundId(),
                soundData.getPanLeft(),
                soundData.getPanRight()
        );
    }

    // 効果音プレイヤーのイベントリスナ
    interface SoundPoolListener {
        void onLoadComplete(int position, SoundData soundData);
    }
    private SoundPoolListener _soundPoolListener = null;

    void setOnSoundPoolListener(SoundPoolListener listener) {
        _soundPoolListener = listener;
    }
}
