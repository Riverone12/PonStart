package com.reverone.kawahara.ponstart;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * 設定画面
 * Copyright (C) 2017-2018 J.Kawahara
 * 2017.5.30 J.Kawahara 新規作成
 * 2018.3.22 J.Kawahara アプリの設定画面を表示するボタンを追加
 */

public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        // 権限を設定するボタンの準備
        Button buttonSettingPermissions = (Button) findViewById(R.id.button_setting_permissions);
        buttonSettingPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // アプリの設定画面を表示する
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // AdMob
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(getString(R.string.pref_action_bar_title));

        prepareControls();
    }

    private void prepareControls() {
        Button buttonSettingPermissions = (Button) findViewById(R.id.button_setting_permissions);

        boolean permissionOk = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionOk = shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        // 「今後は確認しない」にチェック、かつ権限を拒否された場合のみ表示する
        if (permissionOk) {
            buttonSettingPermissions.setVisibility(View.GONE);
        } else {
            buttonSettingPermissions.setVisibility(View.VISIBLE);
        }
    }
}
