package com.reverone.kawahara.ponstart;

/**
 * ソングデータの配列
 * Created by kawahara on 2017/05/24.
 */

class SongDataList extends SoundDataList {
    // シングルトンとして実装する
    private static SongDataList _instance;
    static SongDataList getInstance() {
        if (_instance == null) {
            _instance = new SongDataList();
        }
        return _instance;
    }

    private SongDataList() {
        super(SoundData.SONG_TABLE_NAME);
    }
}
