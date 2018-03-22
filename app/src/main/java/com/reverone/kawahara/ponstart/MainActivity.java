package com.reverone.kawahara.ponstart;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.reverone.kawahara.ponstart.common.FontSizeAdjuster;
import com.reverone.kawahara.ponstart.common.MyBroadcastReceiver;

/**
 * PonStart 効果音ぽんダシ
 * Copyright (C) 2017-2018 J.Kawahara
 * 2017.5.11 J.Kawahara 新規作成
 * 2017.6.3 J.Kawahara ver.1.0
 * 2017.6.15 J.Kawahara ver.1.1 デバイスの音量ボタンで音量を調節できるようにした
 * 2017.6.16 J.Kawahara         画面の向きの設定項目を追加
 * 2017.7.1 J.Kawahara ver.1.2 起動時間を少しだけ短縮（SoundPoolの最大登録数を64→32に変更）
 *          J.Kawahara         対応機種をAPI19 → API16 に変更
 * 2017.7.15 J.Kawahara ver.1.3 onActivityResult() 内でクラッシュするバグを修正
 * 2017.7.20 J.Kawahara ver.1.4 曲の選択ができなくなる不具合を修正
 * 2017.7.30 J.Kawahara ver.1.5 試聴機能を削除
 * 2017.10.9 J.Kawahara ver.1.8 ローカライズ化。英語に対応。
 * 2017.10.25 J.Kawahara ver.1.9 Crashlytics 対応
 *                               外部ライブラリcommonlib を削除
 *                               不要なログ出力を削除
 *                               Kotlin対応
 * 2017.10.26 J.Kawahara ver.1.92 STOP 時のフェードアウト機能を追加
 * 2017.11.4 J.Kawahara ver.2.01 PlaySoundEffectFragment のクラッシュ対策
 * 2018.2.16 J.Kawahara ver.2.03 丸形アイコンを作成
 * 2018.3.22 J.Kawahara ver.2.04 起動時に外部ストレージ読み込みのパーミッションチェックを行うよう変更
*/

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ViewPager _pager;
    private SongController _songController;
    private MyFragmentPagerAdapter _pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        assert drawer != null;
        drawer.addDrawerListener(toggle);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        FontSizeAdjuster.adjustment(navigationView, true);

        if (AppPreference.getOrientation() == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        // 画面の向きが一致する場合のみ処理を継続する
        // ボタンフラグメントを準備
        PagerTabStrip tabStrip = (PagerTabStrip) findViewById(R.id.pagerStrip);
        tabStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        FontSizeAdjuster.adjustment(tabStrip, true);

        _pager = (ViewPager) findViewById(R.id.pager);
        _pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        _pager.setAdapter(_pagerAdapter);
        prepareFragments();
        switchContent(ContentType.SOUND_EFFECT, 0);

        // メディアプレーヤーのコントローラを準備
        View controllerView = findViewById(R.id.song_controller_layout);
        _songController = new SongController();
        _songController.initialize(controllerView);
        FontSizeAdjuster.adjustment((ViewGroup) controllerView, true);

        // デバイスのコントローラで音量を調節できるようにする
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // AdMob
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1882812461462801~7583348975");
        AdView adView = (AdView) findViewById(R.id.adView);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    // フラグメントを準備
    private void prepareFragments() {

        final int[] bgColorList = {
                Color.rgb(0xe4, 0xe4, 0xff),
                Color.rgb(0xdc, 0xf6, 0xdc),
                Color.rgb(0xfa, 0xe1, 0xc6),
                Color.rgb(0xff, 0xe4, 0xff)
        };

        // 効果音ページを準備
        final int soundEffectPages = AppPreference.getSoundEffectPages();
        for (int i = 0; i < soundEffectPages; ++i) {
            PageItem pageItem = null;
            if (_pagerAdapter.getCount() > i) {
                pageItem = _pagerAdapter.getItem(i);
                if (pageItem.contentType != PageItem.ContentType.SOUND_EFFECT) {
                    while (_pagerAdapter.getCount() > i) {
                        _pagerAdapter.remove(i);
                    }
                }
            }
            if (pageItem == null
                    || pageItem.contentType != PageItem.ContentType.SOUND_EFFECT) {
                PageItem p = new PageItem();
                p.contentType = PageItem.ContentType.SOUND_EFFECT;
                p.page = i;
                p.color = bgColorList[i];
                p.title =
                        getString(R.string.buttons_se_page_pre_string)
                                + String.valueOf(i + 1);
                _pagerAdapter.add(p);
            }
        }
        if (_pagerAdapter.getCount() > soundEffectPages) {
            final PageItem pageItem = _pagerAdapter.getItem(soundEffectPages);
            if (pageItem.contentType != PageItem.ContentType.SONG
                    || pageItem.page != 0) {
                while (_pagerAdapter.getCount() > soundEffectPages) {
                    _pagerAdapter.remove(soundEffectPages);
                }
            }
        }

        // ソングページを準備
        final int songPages = AppPreference.getSongPages();
        for (int i = 0; i < songPages; ++i) {
            final int idx = soundEffectPages + i;
            if (_pagerAdapter.getCount() <= idx) {
                PageItem p = new PageItem();
                p.contentType = PageItem.ContentType.SONG;
                p.page = i;
                p.color = bgColorList[i];
                p.title =
                        getString(R.string.buttons_song_page_pre_string)
                                + String.valueOf(i + 1);
                _pagerAdapter.add(p);
            }
        }
        while (_pagerAdapter.getCount() > soundEffectPages + songPages) {
            _pagerAdapter.remove(_pagerAdapter.getCount() - 1);
        }

        // ドロアのナビゲーションメニューを変更
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) drawer.findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        for (int i = 0; i < soundEffectPages; ++i) {
            menu.getItem(i).setVisible(true);
        }
        for (int i = soundEffectPages; i < 4; ++i) {
            menu.getItem(i).setVisible(false);
        }
        for (int i = 4; i < 4 + songPages; ++i) {
            menu.getItem(i).setVisible(true);
        }
        for (int i = 4 + songPages; i < 4 + 4; ++i) {
            menu.getItem(i).setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private static final int RESULT_CODE_PREFERENCE = 999;
    private ContentType _currentContentType;
    private int _currentPageNumber;
    private int _oldSoundEffectPages;
    private int _oldSongPages;
    private int _oldOrientation;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // 現在表示中のページを保存する
            _oldSoundEffectPages = AppPreference.getSoundEffectPages();
            _oldSongPages = AppPreference.getSongPages();
            _oldOrientation = AppPreference.getOrientation();

            _currentPageNumber = _pager.getCurrentItem();
            if (_currentPageNumber < _oldSoundEffectPages) {
                _currentContentType = ContentType.SOUND_EFFECT;
            }
            else {
                _currentPageNumber -= _oldSoundEffectPages;
                _currentContentType = ContentType.SONG;
            }

            // 設定画面を表示する
            Intent intent = new Intent(this, PreferenceActivity.class);
            startActivityForResult(intent, RESULT_CODE_PREFERENCE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_CODE_PREFERENCE) {
            // 設定画面から戻ってきた

            // リピート再生の有無を設定する
            final int repeatMode = AppPreference.getIsLoopMode()
                    ? MediaPlayerService.ACTION_SET_LOOPING_TRUE
                    : MediaPlayerService.ACTION_SET_LOOPING_FALSE;
            MediaPlayerBinder binder = MediaPlayerBinder.getInstance();
            binder.sendMessage(repeatMode);

            // ページ数に変更がある場合は、それを適用する
            if (AppPreference.getSoundEffectPages() != _oldSoundEffectPages
                    || AppPreference.getSongPages() != _oldSongPages) {
                prepareFragments();

                // 開いていたページを再表示する
                int oldCurrentPageNumber = _currentPageNumber;
                int nPage = _currentPageNumber;
                int maxPages = _currentContentType == ContentType.SOUND_EFFECT
                        ? AppPreference.getSoundEffectPages()
                        : AppPreference.getSongPages();
                if (nPage >= maxPages) {
                    nPage = maxPages - 1;
                }
                if (nPage != oldCurrentPageNumber) {
                    switchContent(_currentContentType, nPage);
                }
            }

            // 画面の向きに変更がある場合は、それを適用する
            if (AppPreference.getOrientation() != _oldOrientation) {
                if (AppPreference.getOrientation() == 0) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        }
    }

    private enum ContentType {
        NONE,
        SOUND_EFFECT,
        SONG
    }

    private void switchContent(ContentType contentType, int page) {
        final int offset = contentType ==
                ContentType.SOUND_EFFECT
                ? 0
                : AppPreference.getSoundEffectPages();
        final int index = offset + page;
        _pager.setCurrentItem(index);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        NavigationItemId itemId = parseNavigationItemId(item.getItemId());
        switchContent(itemId.contentType, itemId.page);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean mRequestPermission = false;

    @Override
    protected void onResume() {
        super.onResume();

        Context context = ApplicationControl.getContext();

        // パーミッションの確認を行う
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!mRequestPermission) {
                mRequestPermission = true;
                String[] permissions = { android.Manifest.permission.READ_EXTERNAL_STORAGE };
                ActivityCompat.requestPermissions(this, permissions, 1000);
            }
        }

        // データベースの準備
        DBOpenHelper.getInstance().initialize(context);

        // データベースから設定内容を読み込む
        SoundEffectDataList soundEffectDataList = SoundEffectDataList.getInstance();
        SongDataList songDataList = SongDataList.getInstance();
        soundEffectDataList.load();
        songDataList.load();

        // 効果音データをロードしておく
        soundEffectDataList.setOnSoundPoolListener(new SoundEffectDataList.SoundPoolListener() {
            @Override
            public void onLoadComplete(int position, SoundData soundData) {
                Bundle arg = new Bundle();
                arg.putInt(
                        PlaySoundEffectFragment.BROADCAST_ARG_KEY_LOADED_POSITION,
                        position);
                MyBroadcastReceiver.sendBroadcast(
                        MainActivity.this,
                        PlaySoundEffectFragment.BROADCAST_LOADED_SE_ID,
                        arg);
            }
        });
        soundEffectDataList.initializeSoundEffect(context);

        // メディアプレーヤーのコントローラーを準備
        _songController.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults.length <= 0 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 2000);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (_songController != null) {
            _songController.stop();

            // 効果音データのリソースを解放する
            SoundEffectDataList soundEffectDataList = SoundEffectDataList.getInstance();
            soundEffectDataList.terminateSoundEffect();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (_songController != null) {
            _songController.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (_songController != null) {
            _songController.onRestoreInstanceState(savedInstanceState);
        }
    }

    private class NavigationItemId {
        ContentType contentType;
        int page;
    }

    private NavigationItemId parseNavigationItemId(int navigationItemId)  {
        ContentType contentType;
        int page;
        switch (navigationItemId) {
            case R.id.nav_sound_effect_page1:
                contentType = ContentType.SOUND_EFFECT;
                page = 0;
                break;
            case R.id.nav_sound_effect_page2:
                contentType = ContentType.SOUND_EFFECT;
                page = 1;
                break;
            case R.id.nav_sound_effect_page3:
                contentType = ContentType.SOUND_EFFECT;
                page = 2;
                break;
            case R.id.nav_sound_effect_page4:
                contentType = ContentType.SOUND_EFFECT;
                page = 3;
                break;
            case R.id.nav_song_page1:
                contentType = ContentType.SONG;
                page = 0;
                break;
            case R.id.nav_song_page2:
                contentType = ContentType.SONG;
                page = 1;
                break;
            case R.id.nav_song_page3:
                contentType = ContentType.SONG;
                page = 2;
                break;
            case R.id.nav_song_page4:
                contentType = ContentType.SONG;
                page = 3;
                break;
            default:
                contentType = ContentType.NONE;
                page = 0;
                break;
        }
        NavigationItemId item = new NavigationItemId();
        item.contentType = contentType;
        item.page = page;
        return item;
    }
}
