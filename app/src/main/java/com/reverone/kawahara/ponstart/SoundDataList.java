package com.reverone.kawahara.ponstart;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 曲データの配列
 * Created by kawahara on 2017/05/24.
 */

class SoundDataList {
    private static final int MAX_DATA_COUNT = 32;
    // 2x4/page x 4page = 32

    private SoundData[] _soundDataList;

    // データベースにおけるテーブル名
    private String _tableName;

    SoundDataList(String tableName) {
        _tableName = tableName;
        _soundDataList = new SoundData[MAX_DATA_COUNT];
        for (int i = 0; i < MAX_DATA_COUNT; ++i) {
            _soundDataList[i] = new SoundData();
            _soundDataList[i].setPosition(i);
        }
    }

    int size() {
        return _soundDataList.length;
    }

    SoundData getSoundData(int position) {
        if (position < 0 || position >= MAX_DATA_COUNT) {
            return null;
        }
        return _soundDataList[position];
    }

    void erase(final SoundData soundData) {
        final int position = soundData.getPosition();
        SoundData eraseData = getSoundData(position);
        eraseData.setStandBy(false);
        eraseData.erase();
        modify(eraseData);
    }

    void modify(final SoundData soundData) {
        final ContentValues val = soundData.getContentValues();
        DBOpenHelper dbOpenHelper = DBOpenHelper.getInstance();
        SQLiteDatabase database = dbOpenHelper.openWritable();
        database.beginTransaction();
        boolean status = true;
        try {
            final long updatedRecords = database.update(
                    _tableName,
                    val,
                    "_position = ?",
                    new String[] { "" + soundData.getPosition() }
            );
            if (updatedRecords <= 0) {
                final long insertedId = database.insert(
                        _tableName,
                        null,
                        val
                );
                status = (insertedId == soundData.getPosition());
            }
            if (status) {
                database.setTransactionSuccessful();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            status = false;
        }
        finally {
            database.endTransaction();
            database.close();
            dbOpenHelper.close();
        }
        if (status) {
            SoundData data = getSoundData(soundData.getPosition());
            data.copy(soundData);
        }
    }

    void load() {
        for (int i = 0; i < MAX_DATA_COUNT; ++i) {
            SoundData soundData = _soundDataList[i];
            soundData.clear();
            soundData.setPosition(i);
        }

        DBOpenHelper dbOpenHelper = DBOpenHelper.getInstance();
        SQLiteDatabase database = dbOpenHelper.openReadable();
        Cursor cursor = database.query(_tableName, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            final int[] columnIndex =  {
                    cursor.getColumnIndex(SoundData.COL_POSITION),
                    cursor.getColumnIndex(SoundData.COL_FILE_TYPE),
                    cursor.getColumnIndex(SoundData.COL_TITLE),
                    cursor.getColumnIndex(SoundData.COL_FILE_NAME),
                    cursor.getColumnIndex(SoundData.COL_VOLUME),
                    cursor.getColumnIndex(SoundData.COL_PAN_LEFT),
                    cursor.getColumnIndex(SoundData.COL_PAN_RIGHT)
            };
            do {
                SoundData soundData = new SoundData(
                        cursor.getInt(columnIndex[0]),
                        cursor.getInt(columnIndex[1]),
                        cursor.getString(columnIndex[2]),
                        cursor.getString(columnIndex[3]),
                        cursor.getInt(columnIndex[4]),
                        cursor.getInt(columnIndex[5]),
                        cursor.getInt(columnIndex[6])
                );

                SoundData dest = getSoundData(soundData.getPosition());
                if (dest != null) {
                    dest.copy(soundData);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        dbOpenHelper.close();
    }
}
