package com.reverone.kawahara.ponstart;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * このアプリの設定項目
 * Created by kawahara on 2017/05/30.
 * 2017.6.16 J.Kawahara 画面の向きの設定項目を追加
 * 2017.10.26 J.Kawahara PREFERENCE_VERSION = 2 フェードアウト機能を追加
 */

class AppPreference
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int PREFERENCE_VERSION = 2;

    private static AppPreference _instance;
    static AppPreference getInstance() {
        if (_instance == null) {
            _instance = new AppPreference();
        }
        return _instance;
    }

    private AppPreference() {

    }

    private boolean _isLoopMode;
    private boolean _isQuickStartMode;
    private int _soundEffectPages;
    private int _songPages;
    private int _orientation;

    private int _fadeoutSec;

    private static final String PREF_KEY_VERSION = "pref_version";
    private static final String KEY_IS_LOOP_MODE = "preference_is_loop_mode";
    private static final String KEY_IS_QUICK_START_MODE = "preference_is_quick_start_mode";
    static final String KEY_SOUND_EFFECT_PAGES = "preference_sound_effect_pages";
    static final String KEY_SONG_PAGES = "preference_song_pages";
    static final String KEY_ORIENTATION = "preference_orientation";

    static final String KEY_FADEOUT_SEC = "preference_fadeout_sec";

    // リピート再生の有無
    static boolean getIsLoopMode() {
        return (_instance._isLoopMode);
    }

    // クイック再生の有無
    static boolean getIsQuickStartMode() {
        return (_instance._isQuickStartMode);
    }

    // 効果音ページのページ数
    static int getSoundEffectPages() {
        return (_instance._soundEffectPages);
    }

    // 曲ページのページ数
    static int getSongPages() {
        return (_instance._songPages);
    }

    // 画面の向き
    static int getOrientation() {
        return (_instance._orientation);
    }

    // STOPボタンタップ時のフェードアウトの時間
    static int getFadeoutSec() {
        return (_instance._fadeoutSec);
    }

    static void initialize(Context applicationContext) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        AppPreference p = getInstance();
        p._isLoopMode = pref.getBoolean(KEY_IS_LOOP_MODE, false);
        p._isQuickStartMode = pref.getBoolean(KEY_IS_QUICK_START_MODE, false);
        p._soundEffectPages = getIntValue(pref, KEY_SOUND_EFFECT_PAGES, 4);
        p._songPages = getIntValue(pref, KEY_SONG_PAGES, 4);
        p._orientation = getIntValue(pref, KEY_ORIENTATION, 0);
        p._fadeoutSec = getIntValue(pref, KEY_FADEOUT_SEC, 0);

        int version = pref.getInt(PREF_KEY_VERSION, 0);
        if (version < PREFERENCE_VERSION) {
            p.saveAll(applicationContext);
        }
    }

    private void saveAll(Context applicationContext) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_KEY_VERSION, PREFERENCE_VERSION);
        editor.putBoolean(KEY_IS_LOOP_MODE, _isLoopMode);
        editor.putBoolean(KEY_IS_QUICK_START_MODE, _isQuickStartMode);
        editor.putString(KEY_SOUND_EFFECT_PAGES, String.valueOf(_soundEffectPages));
        editor.putString(KEY_SONG_PAGES, String.valueOf(_songPages));
        editor.putString(KEY_ORIENTATION, String.valueOf(_orientation));
        editor.putString(KEY_FADEOUT_SEC, String.valueOf(_fadeoutSec));
        editor.apply();
    }

    private static int getIntValue(SharedPreferences pref, String key, int defaultValue) {
        return (Integer.parseInt(pref.getString(key, String.valueOf(defaultValue))));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_IS_LOOP_MODE.equals(key)) {
            _isLoopMode = sharedPreferences.getBoolean(KEY_IS_LOOP_MODE, false);
        }
        else if (KEY_IS_QUICK_START_MODE.equals(key)) {
            _isQuickStartMode = sharedPreferences.getBoolean(KEY_IS_QUICK_START_MODE, false);
        }
        else if (KEY_SOUND_EFFECT_PAGES.equals(key)) {
            _soundEffectPages = getIntValue(sharedPreferences, KEY_SOUND_EFFECT_PAGES, 4);
        }
        else if (KEY_SONG_PAGES.equals(key)) {
            _songPages = getIntValue(sharedPreferences, KEY_SONG_PAGES, 4);
        }
        else if (KEY_ORIENTATION.equals(key)) {
            _orientation = getIntValue(sharedPreferences, KEY_ORIENTATION, 0);
        }
        else if (KEY_FADEOUT_SEC.equals(key)) {
            _fadeoutSec = getIntValue(sharedPreferences, KEY_FADEOUT_SEC, 0);
        }
    }
}
