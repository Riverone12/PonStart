package com.reverone.kawahara.ponstart;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.reverone.kawahara.ponstart.common.MyBroadcastReceiver;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaySongFragment extends ButtonsFragment {

    private boolean _startAfterPrepared;

    public PlaySongFragment() {
        super();
        _startAfterPrepared = false;

        // プレーヤーからのブロードキャストを受け取る準備
        MyBroadcastReceiver.register(ApplicationControl.getContext(), new MyBroadcastReceiver.Callback() {
            @Override
            public void onEventInvoked(int id, Bundle arg) {
                // クイック再生モードの場合、曲の準備ができ次第再生を開始する
                if (_startAfterPrepared) {
                    final SoundInformation info = MediaPlayerService.extractSoundInformation(arg);
                    if (info != null
                            && info.getPayerStatus() == MediaPlayerService.STATUS_PREPARED) {
                        _startAfterPrepared = false;
                        MediaPlayerBinder binder = MediaPlayerBinder.getInstance();
                        binder.sendMessage(MediaPlayerService.ACTION_START);
                    }
                }
            }
        });
    }

    @Override
    protected PageItem.ContentType getContentType() {
        return PageItem.ContentType.SONG;
    }

    @Override
    protected void playSound(int page, int position, final SoundData soundData) {
        if (AppPreference.getIsQuickStartMode()) {
            _startAfterPrepared = true;
        }
        MediaPlayerBinder binder = MediaPlayerBinder.getInstance();
        binder.sendMessage(
                MediaPlayerService.ACTION_PREPARE,
                MediaPlayerService.KEY_SOUND_DATA,
                soundData);
    }

    @Override
    protected SoundDataList prepareSoundDataList() {
        return SongDataList.getInstance();
    }
}
