package com.reverone.kawahara.ponstart;

import java.util.Locale;

/**
 * デモ音源データ
 * Created by kawahara on 2017/06/01.
 * 2017.10.8 J.Kawahara ローカライズ化。英語に対応。
 */

class DemoSoundData {

    private static final String[] _titleList = {
            "Correct",
            "Incorrect",
            "Question",
            "Broadcasting",
            "Vibraslap",
            "Wooden clappers",
            "Appearance",
            "Triangle"
    };

    private static final String[] _titleList_ja = {
            "正解!",
            "不正解",
            "問題!ジャジャン!",
            "ピンポンパンポン",
            "カ～ッ",
            "拍子木",
            "シャキーン!",
            "ち～ん"
    };

    private static final String[] _fileList = {
            "correct1",
            "incorrect1",
            "question1",
            "broadcasting_start1",
            "costume_drama1",
            "hyoushigi2",
            "shakin1",
            "tin1"
    };

    private static int getCount() {
        return _titleList.length;
    }

    static SoundData getSoundData(int id) {
        if (id < 0 || id >= getCount()) {
            return null;
        }
        String title = getTitleList()[id];

        SoundData soundData = new SoundData();
        soundData.setFileType(SoundData.FILE_TYPE_DEMO);
        soundData.setTitle(title);
        soundData.setFileName(_fileList[id]);
        return soundData;
    }

    static String[] getTitleList() {
        if (Locale.getDefault().equals(Locale.JAPAN)) {
            return _titleList_ja;
        }
        return _titleList;
    }

    static String[] getFileList() {
        return _fileList;
    }
}
