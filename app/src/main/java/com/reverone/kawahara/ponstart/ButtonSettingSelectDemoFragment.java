package com.reverone.kawahara.ponstart;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/*
 * デモ用素材選択ダイアログ
 * 2017.6.2 J.Kawahara 新規作成
 */

/**
 * A simple {@link Fragment} subclass.
 */
public class ButtonSettingSelectDemoFragment extends DialogFragment {
    public static final String ARG_KEY_INDEX = "argKeyIndex";
    private ListView _listView;

    static ButtonSettingSelectDemoFragment newInstance(final Fragment fragment, int requestCode, int index) {
        if (fragment == null) {
            throw new RuntimeException("fragment is null");
        }
        ButtonSettingSelectDemoFragment dialog = new ButtonSettingSelectDemoFragment();

        // 再生成後でも使用する値をBundle に入れておく
        Bundle args = new Bundle();
        args.putInt(ARG_KEY_INDEX, index);
        dialog.setArguments(args);

        // 結果を受け取るFragmentは、直接Fragmentの変数には入れずに、
        // setTargetFragment()/getTargetFragment()を使う
        dialog.setTargetFragment(fragment, requestCode);

        return dialog;
    }

    public ButtonSettingSelectDemoFragment() {
        // Required empty public constructor
    }


    @Override
    @SuppressLint("InflateParams")
    public @NonNull
    Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // レイアウトを準備する
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_button_setting_select_demo, null, false);
        builder.setView(v);

        // ボタンの準備を行う
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
                Intent intent = new Intent();
                if (getArguments() != null) {
                    Bundle arg = getArguments();
                    arg.putInt(ARG_KEY_INDEX, _listView.getCheckedItemPosition());
                    intent.putExtras(arg);
                }
                getTargetFragment().onActivityResult(getTargetRequestCode(), whichButton, intent);
            }
        };

        builder.setPositiveButton(
                getString(R.string.dialog_button_ok),
                listener);
        builder.setNegativeButton(
                getString(R.string.dialog_button_cancel),
                listener);

        _listView = (ListView) v.findViewById(R.id.listViewSelectDemo);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_single_choice,
                DemoSoundData.getTitleList());

        _listView.setAdapter(arrayAdapter);

        // 選択の方式の設定
        _listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final int index = getArguments().getInt(ARG_KEY_INDEX, -1);
        if (index >= 0) {
            _listView.setItemChecked(index, true);
        }

        return builder.create();
    }
}
