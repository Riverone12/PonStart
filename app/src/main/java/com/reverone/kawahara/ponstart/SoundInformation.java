package com.reverone.kawahara.ponstart;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 再生中の曲の情報
 * Created by kawahara on 2017/05/24.
 * 2017.10.26 J.Kawahara getVolume()
 */

class SoundInformation implements Parcelable {
    private int _totalTime;
    private int _currentTime;

    private int _playerStatus;
    private boolean _isLoop;

    // SoundData のフィールドのコピー
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
        _totalTime = 0;
        _currentTime = 0;
        _playerStatus = MediaPlayerService.STATUS_IDLE;
        _isLoop = false;

        setSoundData(new SoundData());
    }

    // Getter
    int getVolume() { return _volume; }
    int getTotalTime() { return _totalTime; }
    int getCurrentTime() { return _currentTime; }
    int getPayerStatus() { return _playerStatus; }
    boolean isLoop() { return _isLoop; }
    String getTitle() { return _title; }

    SoundData toSoundData() {
        return new SoundData(
                _position,
                _fileType,
                _title,
                _fileName,
                _volume,
                _panLeft,
                _panRight
        );
    }

    // Setter
    void setTotalTime(int totalTime) { _totalTime = totalTime; }
    void setCurrentTime(int currentTime) { _currentTime = currentTime; }
    void setPlayerStatus(int playerStatus) { _playerStatus = playerStatus; }
    void setIsLoop(boolean isLoop) { _isLoop = isLoop; }

    void setSoundData(final SoundData s) {
        _position = s.getPosition();
        _fileType = s.getFileType();
        _title = s.getTitle();
        _fileName = s.getFileName();
        _volume = s.getVolume();
        _panLeft = s.getPanLeft();
        _panRight = s.getPanRight();
        _soundId = s.getSoundId();
        _standBy = s.isStandBy();
    }

    String getTotalTimeString(Context context) {
        if (_totalTime == 0) {
            return context.getString(R.string.display_time_default_string);
        }
        return milliSecondToSeString(_totalTime);
    }

    String getCurrentTimeString(Context context) {
        if (_totalTime == 0) {
            return context.getString(R.string.display_time_default_string);
        }
        return milliSecondToSeString(_currentTime);
    }

    String getRemainTimeString(Context context) {
        if (_totalTime == 0) {
            return context.getString(R.string.display_time_default_string);
        }
        final int remainTime = _totalTime -  _currentTime;
        final String prefix = context.getString(R.string.display_remain_prefix);
        return (prefix + milliSecondToSeString(remainTime));
    }

    private static String milliSecondToSeString(int millisecond) {
        final int tsec = millisecond / 1000;
        final int sec = tsec % 60;
        final int min = tsec / 60;

        String strMin = String.valueOf(min);
        if (min < 10) {
            strMin = "0" + strMin;
        }
        String strSec = String.valueOf(sec);
        if (sec < 10) {
            strSec = "0" + strSec;
        }
        return (strMin + ":" + strSec);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this._totalTime);
        dest.writeInt(this._currentTime);
        dest.writeInt(this._playerStatus);
        dest.writeByte(this._isLoop ? (byte) 1 : (byte) 0);
        dest.writeInt(this._position);
        dest.writeInt(this._fileType);
        dest.writeString(this._title);
        dest.writeString(this._fileName);
        dest.writeInt(this._volume);
        dest.writeInt(this._panLeft);
        dest.writeInt(this._panRight);
        dest.writeInt(this._soundId);
        dest.writeByte(this._standBy ? (byte) 1 : (byte) 0);
    }

    SoundInformation() {
        clear();
    }

    private SoundInformation(Parcel in) {
        this._totalTime = in.readInt();
        this._currentTime = in.readInt();
        this._playerStatus = in.readInt();
        this._isLoop = in.readByte() != 0;
        this._position = in.readInt();
        this._fileType = in.readInt();
        this._title = in.readString();
        this._fileName = in.readString();
        this._volume = in.readInt();
        this._panLeft = in.readInt();
        this._panRight = in.readInt();
        this._soundId = in.readInt();
        this._standBy = in.readByte() != 0;
    }

    public static final Parcelable.Creator<SoundInformation> CREATOR = new Parcelable.Creator<SoundInformation>() {
        @Override
        public SoundInformation createFromParcel(Parcel source) {
            return new SoundInformation(source);
        }

        @Override
        public SoundInformation[] newArray(int size) {
            return new SoundInformation[size];
        }
    };
}
