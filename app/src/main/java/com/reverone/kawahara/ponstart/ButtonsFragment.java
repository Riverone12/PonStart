package com.reverone.kawahara.ponstart;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.reverone.kawahara.ponstart.common.FontSizeAdjuster;

/**
 * 音ボタンのフラグメントの基底クラス
 * Created by kawahara on 2017/05/24.
 */

abstract public class ButtonsFragment extends Fragment
        implements ButtonSettingDialog.Listener {

    abstract protected PageItem.ContentType getContentType();
    abstract protected void playSound(int page, int position, final SoundData soundData);
    abstract protected SoundDataList prepareSoundDataList();

    // 1ページあたりのボタンの数を 2x4 固定で実装する
    static final int BUTTON_COUNT_BY_PAGE = 8;
    private int _currentPage;       // 0 ～ MAX_PAGE_COUNT-1

    SoundDataList _soundDataList;
    private Button[] _buttons;

    public ButtonsFragment() {
        _currentPage = 0;
        _buttons = new Button[BUTTON_COUNT_BY_PAGE];
        _soundDataList = prepareSoundDataList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_play_song, container, false);

        Activity activity = getActivity();
        Resources resources = activity.getResources();
        final String packageName = activity.getPackageName();

        _buttons = new Button[BUTTON_COUNT_BY_PAGE];
        for (int i = 0; i < BUTTON_COUNT_BY_PAGE; ++i) {
            final String strId = "btn2x4_" + String.valueOf(i);
            final int id = resources.getIdentifier(strId, "id", packageName);
            Button button = (Button) v.findViewById(id);
            button.setTag(i);
            button.setAllCaps(false);

            button.setOnClickListener(new ButtonsFragment.OnButtonClickListener());
            button.setOnLongClickListener(new ButtonsFragment.OnButtonLongClickListener());
            _buttons[i] = button;
        }

        int page = 0;
        Bundle arg = getArguments();
        if (arg != null) {
            if (arg.containsKey("page")) {
                page = arg.getInt("page");
            }
            if (arg.containsKey("color")) {
                LinearLayout layout = (LinearLayout) v.findViewById(R.id.content_main_buttons2x4_layout);
                layout.setBackgroundColor(arg.getInt("color"));
            }
        }

        setCurrentPage(page);


        FontSizeAdjuster.adjustment(
                (ViewGroup) v.findViewById(R.id.content_main_buttons2x4_layout), true);
        return v;
    }

    public void setCurrentPage(int page) {
        _currentPage = page;

        for (int i = 0; i < BUTTON_COUNT_BY_PAGE; ++i) {
            refreshButton(BUTTON_COUNT_BY_PAGE * page + i);
        }
    }

    // ボタンをタップした時のイベントハンドラ
    private class OnButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int tag = (int) v.getTag();
            final int position = BUTTON_COUNT_BY_PAGE * _currentPage + tag;
            SoundData soundData = _soundDataList.getSoundData(position);
            playSound(_currentPage, position, soundData);
        }
    }

    private static final String SETTING_DIALOG_TAG =  "Button_Setting_Dialog";

    // ボタンを長押しした時のイベントハンドラ
    private class OnButtonLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            final int tag = (int) v.getTag();
            final int position = BUTTON_COUNT_BY_PAGE * _currentPage + tag;

            // ボタン設定画面を表示する
            SoundData soundData = _soundDataList.getSoundData(position);
            ButtonSettingDialog dialog = ButtonSettingDialog.newInstance(
                    getContentType(),
                    soundData,
                    ButtonsFragment.this);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            dialog.show(fragmentManager, SETTING_DIALOG_TAG);

            return true;
        }
    }

    // ボタン編集ダイアログで[登録]ボタンをタップされた時のハンドラ
    public void onDialogPositiveClick(DialogFragment dialog, final SoundData editedData) {
        final int position = editedData.getPosition();
        _soundDataList.modify(editedData);
        refreshButton(position);
    }

    // ボタン編集ダイアログで[キャンセル]ボタンをタップされた時のハンドラ
    public void onDialogNegativeClick(DialogFragment dialog , final SoundData originalData) {

    }

    // ボタン編集ダイアログで[クリア]ボタンをタップされた時のハンドラ
    public void onDialogClearClick(DialogFragment dialog, final SoundData originalData) {
        _soundDataList.erase(originalData);
        refreshButton(originalData.getPosition());
    }

    void refreshButton(int position) {
        final int page = position / BUTTON_COUNT_BY_PAGE;
        if (page != _currentPage) {
            // 現在表示中のページのボタンではないため、何もしない
            return;
        }
        Button btn = _buttons[position % BUTTON_COUNT_BY_PAGE];
        if (btn == null) {
            return;
        }
        final SoundData soundData = _soundDataList.getSoundData(position);
        if (soundData != null) {
            displayButton(btn, soundData);
        }
        else {
            btn.setText("");
        }
    }

    protected void displayButton(Button button, @NonNull final SoundData soundData) {
        button.setText(soundData.getTitle());
    }
}
