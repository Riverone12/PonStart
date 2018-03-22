package com.reverone.kawahara.ponstart;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

/**
 * 設定画面
 * Created by kawahara on 2017/05/30.
 * 2017.6.16 J.Kawahara 画面の向きの設定項目を追加
 * 2017.10.26 J.Kawahara フェードアウト機能を追加
 */

// build.gradle に以下の行を追加する必要がある
// compile 'com.android.support:preference-v7:25.3.1'

// res/values/styles.xml に以下の行を追加する必要がある
/*
<!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
            <item name="preferenceTheme">@style/PreferenceThemeOverlay</item>
    </style>
*/

public class PreferenceFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    // 現在の設定値を動的に表示するため、設定コントロールのリソースを予め取得しておく
    private String[] _pagesKeys;
    private String[] _orientationKeys;
    private String[] _fadeoutSecKeys;
    private Preference _preferenceSoundEffectPages;
    private Preference _preferenceSongPages;
    private Preference _preferenceOrientation;
    private Preference _preferenceFadeoutSec;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // リソースを取得する
        _pagesKeys = getResources().getStringArray(R.array.pref_pages_item_keys);
        _orientationKeys = getResources().getStringArray(R.array.pref_orientation_keys);
        _fadeoutSecKeys = getResources().getStringArray(R.array.pref_fadeout_sec_keys);

        _preferenceSoundEffectPages = findPreference(AppPreference.KEY_SOUND_EFFECT_PAGES);
        _preferenceSongPages = findPreference(AppPreference.KEY_SONG_PAGES);
        _preferenceOrientation = findPreference(AppPreference.KEY_ORIENTATION);
        _preferenceFadeoutSec = findPreference(AppPreference.KEY_FADEOUT_SEC);

        // 初期値を表示する
        setFadeoutTimeSummary(AppPreference.getFadeoutSec());
        setPagesSummary(AppPreference.KEY_SOUND_EFFECT_PAGES, AppPreference.getSoundEffectPages());
        setPagesSummary(AppPreference.KEY_SONG_PAGES, AppPreference.getSongPages());
        _preferenceOrientation.setSummary(_orientationKeys[AppPreference.getOrientation()]);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(AppPreference.getInstance());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(AppPreference.getInstance());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    // 設定値が変更された時に呼ばれるコールバック
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(AppPreference.KEY_SOUND_EFFECT_PAGES)
                || key.equals(AppPreference.KEY_SONG_PAGES)) {
            // 現在の設定値を表示する
            final int pageValue = Integer.parseInt(sharedPreferences.getString(key, "4"));
            setPagesSummary(key, pageValue);
        }
        else if (key.equals(AppPreference.KEY_ORIENTATION)) {
            final int value = Integer.parseInt(sharedPreferences.getString(key, "0"));
            _preferenceOrientation.setSummary(_orientationKeys[value]);
        }
        else if (key.equals(AppPreference.KEY_FADEOUT_SEC)) {
            final int value = Integer.parseInt(sharedPreferences.getString(key, "0"));
            setFadeoutTimeSummary(value);
        }
    }

    private void setPagesSummary(String key, int pagesValue) {
        if (pagesValue >= 1 && pagesValue <= 4) {
            final String summary = _pagesKeys[pagesValue - 1];

            if (key.equals(AppPreference.KEY_SOUND_EFFECT_PAGES)) {
                _preferenceSoundEffectPages.setSummary(summary);
            }
            else if (key.equals(AppPreference.KEY_SONG_PAGES)) {
                _preferenceSongPages.setSummary(summary);
            }
        }
    }

    private void setFadeoutTimeSummary(int fadeoutSec) {
        int index = 0;
        switch (fadeoutSec) {
            case 500: index = 1; break;
            case 1000: index = 2; break;
            case 1500: index = 3; break;
            case 2000: index = 4; break;
            case 3000: index = 5; break;
            case 4000: index = 6; break;
            case 5000: index = 7; break;
        }
        _preferenceFadeoutSec.setSummary(_fadeoutSecKeys[index]);
    }
}
