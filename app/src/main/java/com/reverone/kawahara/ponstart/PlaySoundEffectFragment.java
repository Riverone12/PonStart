package com.reverone.kawahara.ponstart;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Button;

import com.reverone.kawahara.ponstart.common.MyBroadcastReceiver;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaySoundEffectFragment extends ButtonsFragment {

    static final String BROADCAST_ARG_KEY_LOADED_POSITION = "BroadcastArgKeyLoadedPosition";
    static final int BROADCAST_LOADED_SE_ID = 10;

    public PlaySoundEffectFragment() {
        super();
        Context appContext = ApplicationControl.getContext();
        MyBroadcastReceiver.register(appContext, new MyBroadcastReceiver.Callback() {
            @Override
            public void onEventInvoked(int id, Bundle arg) {
                if (id == BROADCAST_LOADED_SE_ID) {
                    final int position = arg.getInt(BROADCAST_ARG_KEY_LOADED_POSITION, -1);
                    if (position >= 0) {
                        refreshButton(position);
                    }
                }
            }
        });
    }

    @Override
    protected PageItem.ContentType getContentType() {
        return PageItem.ContentType.SOUND_EFFECT;
    }

    @Override
    protected SoundDataList prepareSoundDataList() {
        return SoundEffectDataList.getInstance();
    }

    @Override
    protected void playSound(int page, int position, final SoundData soundData) {
        if (soundData != null && soundData.isStandBy()) {
            SoundEffectDataList.getInstance().playSoundEffect(soundData);
        }
    }

    @Override
    protected void displayButton(Button button, @NonNull final SoundData soundData) {
        try {
            // java.lang.IllegalStateException: Fragment XXXXX not attached to Activity
            // ↑クラッシュ(getResources でコケる)対策として、isAdded() の場合のみ以下の処理を実行する
            if (soundData.isStandBy()) {
                button.setText(soundData.getTitle());
            } else if (!soundData.isEmpty()) {
                button.setText(getString(R.string.buttons_loading_caption));
            } else {
                button.setText("");
            }
        }
        catch (Exception e) {
            Log.d("PlaySoundEffectFragment", e.getMessage());
        }
    }
}
