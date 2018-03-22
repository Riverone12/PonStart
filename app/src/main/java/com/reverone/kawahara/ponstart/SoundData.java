package com.reverone.kawahara.ponstart;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 効果音データ
 * Created by kawahara on 2017/05/12.
 */

class SoundData implements Parcelable {
    static final int FILE_TYPE_NONE = 0;
    static final int FILE_TYPE_DEMO = 1;
    static final int FILE_TYPE_FILE = 2;

    static final String SE_TABLE_NAME = "se_data";
    static final String SONG_TABLE_NAME = "song_data";

    static final String COL_POSITION = "_position";
    static final String COL_FILE_TYPE = "_file_type";
    static final String COL_TITLE = "_title";
    static final String COL_FILE_NAME = "_file_name";
    static final String COL_VOLUME = "_volume";
    static final String COL_PAN_LEFT = "_pan_left";
    static final String COL_PAN_RIGHT = "_pan_right";

    // Fields
    private int _position;
    private int _fileType;
    private String _title;
    private String _fileName;
    private int _volume;        // 0 ～ 100
    private int _panLeft;       // 0 ～ 100
    private int _panRight;      // 0 ～ 100

    private int _soundId = 0;
    private boolean _standBy = false;

    void clear() {
        _position = 0;
        _fileType = FILE_TYPE_NONE;
        _title = "";
        _fileName = "";
        _volume = 100;
        _panLeft = 100;
        _panRight = 100;

        _soundId = 0;
        _standBy = false;
    }

    // position は維持する
    void erase() {
        _fileType = FILE_TYPE_NONE;
        _title = "";
        _fileName = "";
        _volume = 100;
        _panLeft = 100;
        _panRight = 100;

        _soundId = 0;
        _standBy = false;
    }

    void copy(final SoundData src) {
        this._position = src._position;
        this._fileType = src._fileType;
        this._title = src._title;
        this._fileName = src._fileName;
        this._volume = src._volume;
        this._panLeft = src._panLeft;
        this._panRight = src._panRight;

        this._soundId = src._soundId;
        this._standBy = src._standBy;
    }

    int getSoundId() { return _soundId; }
    boolean isStandBy() { return _standBy; }
    void setSoundId(int soundId) { _soundId = soundId; }
    void setStandBy(boolean status) { _standBy = status; }

    // Getter
    int getPosition() { return _position; }
    int getFileType() {
        return _fileType;
    }
    String getTitle() { return _title; }
    String getFileName() { return _fileName; }
    int getVolume() { return _volume; }
    int getPanLeft() { return _panLeft; }
    int getPanRight() { return _panRight; }

    boolean isEmpty() {
        return (
                _fileName == null
                        || _fileName.isEmpty()
                        || _fileType == FILE_TYPE_NONE
        );
    }

    // Setter
    void setPosition(int position) { _position = position; }
    void setFileType(int fileType) {
        _fileType = fileType;
    }
    void setTitle(String title) { _title = title; }
    void setFileName(String fileName) { _fileName = fileName; }
    void setVolume(int volume) { _volume = volume; }
    void setPanLeft(int panLeft) { _panLeft = panLeft; }
    void setPanRight(int panRight) { _panRight = panRight; }

    // データベースアクセス関連の補助関数
    ContentValues getContentValues() {
        ContentValues val = new ContentValues();
        val.put(COL_POSITION, _position);
        val.put(COL_FILE_TYPE, _fileType);
        val.put(COL_TITLE, _title);
        val.put(COL_FILE_NAME, _fileName);
        val.put(COL_VOLUME, _volume);
        val.put(COL_PAN_LEFT, _panLeft);
        val.put(COL_PAN_RIGHT, _panRight);
        return val;
    }

    SoundData(
            final int position,
            final int fileType,
            final String title,
            final String fileName,
            final int volume,
            final int panLeft,
            final int panRight) {
        _position = position;
        _fileType = fileType;
        _title = title;
        _fileName = fileName;
        _volume = volume;
        _panLeft = panLeft;
        _panRight = panRight;

        _soundId = 0;
        _standBy = false;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this._position);
        dest.writeInt(this._fileType);
        dest.writeString(this._title);
        dest.writeString(this._fileName);
        dest.writeInt(this._volume);
        dest.writeInt(this._panLeft);
        dest.writeInt(this._panRight);

        dest.writeInt(this._soundId);
        dest.writeInt(this._standBy ? 1 : 0);
    }

    SoundData() {
    }

    protected SoundData(Parcel in) {
        this._position = in.readInt();
        this._fileType = in.readInt();
        this._title = in.readString();
        this._fileName = in.readString();
        this._volume = in.readInt();
        this._panLeft = in.readInt();
        this._panRight = in.readInt();

        this._soundId = in.readInt();
        this._standBy = (in.readInt() != 0);
    }

    public static final Parcelable.Creator<SoundData> CREATOR = new Parcelable.Creator<SoundData>() {
        @Override
        public SoundData createFromParcel(Parcel source) {
            return new SoundData(source);
        }

        @Override
        public SoundData[] newArray(int size) {
            return new SoundData[size];
        }
    };
}
