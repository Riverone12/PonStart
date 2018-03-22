package com.reverone.kawahara.ponstart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * ボタン設定ダイアログ
 * Created by kawahara on 2017/05/14.
 * 2017.10.25 J.Kawahara 不要なログ出力を削除
 */

public class ButtonSettingDialog extends DialogFragment
        implements ConfirmDialog.Listener {

    interface Listener {
        void onDialogPositiveClick(DialogFragment dialog, final SoundData editedData);
        void onDialogNegativeClick(DialogFragment dialog, final SoundData originalData);
        void onDialogClearClick(DialogFragment dialog, final SoundData originalData);
    }

    private EditText _editTitle;
    private Button _buttonFile;

    private static final String ARG_KEY_CONTENT_TYPE = "ArgKeyContentType";
    private static final String ARG_KEY_SOUND_DATA = "ArgKeySoundData";
    private static final String ARG_KEY_FILE_TYPE = "ArgKeyFileType";
    private static final String ARG_KEY_FILE_NAME = "ArgKeyFilename";

    // 他のダイアログへのリクエストコード
    private static final int REQUEST_AUDIO_FILE_CODE = 100;
    private static final int REQUEST_SELECT_DEMO_DATA_CODE = 111;

    public static ButtonSettingDialog newInstance(PageItem.ContentType contentType, final SoundData soundData, Listener listener) {
        // ButtonSettingDialog#Listener を実装したFragment であることが必要
        if (listener == null || !(listener instanceof Fragment)) {
            throw new RuntimeException("listener is not Fragment");
        }

        ButtonSettingDialog dialog = new ButtonSettingDialog();

        // 再生成後でも使用する値をBundle に入れておく
        SoundData editData = new SoundData();
        editData.copy(soundData);
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY_SOUND_DATA, editData);
        args.putInt(ARG_KEY_FILE_TYPE, editData.getFileType());
        args.putString(ARG_KEY_FILE_NAME, editData.getFileName());

        final int c = contentType == PageItem.ContentType.SOUND_EFFECT ? 0 : 1;
        args.putInt(ARG_KEY_CONTENT_TYPE, c);
        dialog.setArguments(args);

        // 結果を受け取るFragmentは、直接Fragmentの変数には入れずに、
        // setTargetFragment()/getTargetFragment()を使う
        dialog.setTargetFragment((Fragment) listener, 0);
        return dialog;
    }

    private PageItem.ContentType getContentType() {
        Bundle args = getArguments();
        PageItem.ContentType contentType = PageItem.ContentType.SOUND_EFFECT;
        if (args != null && args.containsKey(ARG_KEY_CONTENT_TYPE)) {
            final int c = args.getInt(ARG_KEY_CONTENT_TYPE);
            if (c != 0) {
                contentType = PageItem.ContentType.SONG;
            }
        }
        return contentType;
    }

    @Override
    @SuppressLint("InflateParams")
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // レイアウトを準備する
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.button_setting_dialog_layout, null, false);
        builder.setView(v);

        final PageItem.ContentType contentType = getContentType();
        if (contentType == PageItem.ContentType.SOUND_EFFECT) {
            builder.setTitle(R.string.button_setting_se_title);
        }
        else {
            builder.setTitle(R.string.button_setting_song_title);
        }

        // ボタンの準備を行う
        builder.setPositiveButton(
                getString(R.string.button_setting_button_positive),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                getListener().onDialogPositiveClick(
                        ButtonSettingDialog.this,
                        reflect()
                );
            }
        });
        builder.setNegativeButton(
                getString(R.string.button_setting_button_negative),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the negative button event back to the host activity
                getListener().onDialogNegativeClick(
                        ButtonSettingDialog.this,
                        getArgumentsSoundData()
                );
            }
        });

        prepareViews(contentType, v);

        SoundData editData = getArgumentsSoundData();
        editData.setFileType(getArguments().getInt(ARG_KEY_FILE_TYPE));
        editData.setFileName(getArguments().getString(ARG_KEY_FILE_NAME));
        display(editData);

        return builder.create();
    }

    // 引数として与えられたデータを取得する
    private SoundData getArgumentsSoundData() {
        SoundData soundData = new SoundData();
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_KEY_SOUND_DATA)) {
            soundData = args.getParcelable(ARG_KEY_SOUND_DATA);
        }
        return soundData;
    }

    private Listener getListener() {
        return ((Listener) getTargetFragment());
    }

    private static final String TAG_CONFIRM_DIALOG =  "TAG_Confirm_Dialog";

    private void prepareViews(PageItem.ContentType contentType, View v) {
        _editTitle = (EditText) v.findViewById(R.id.editTextTitle);
        _buttonFile = (Button) v.findViewById(R.id.buttonFile);
        _buttonFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });

        Button buttonDelete = (Button) v.findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 確認ダイアログを表示
                // soundData をクリア
                ConfirmDialog confirmDialog = ConfirmDialog.newInstance(
                        getString(R.string.delete_confirm_message),
                        ButtonSettingDialog.this
                );
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                confirmDialog.show(fragmentManager, TAG_CONFIRM_DIALOG);
            }
        });

        TextView alert = (TextView) v.findViewById(R.id.textViewSoundEffectAlert);
        if (contentType == PageItem.ContentType.SOUND_EFFECT) {
            alert.setVisibility(View.VISIBLE);
        }
        else {
            alert.setVisibility(View.INVISIBLE);
        }

        // 「デモ用の素材から選ぶ」リンクの準備
        TextView textViewSelectDemoData = (TextView) v.findViewById(R.id.textViewSelectDemoData);

        if (contentType == PageItem.ContentType.SOUND_EFFECT) {
            final String caption = getString(R.string.select_demo_data_text);
            SpannableString spannableString = new SpannableString(caption);
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // ここにgetActivity() を書くと、画面回転後（ダイアログ再生成後）に
                    // getActivity() がnull を返すようになってしまう。
                    // クリック時の処理はsetOnClickListener() 内に記述して、ここでは
                    // テキストの見た目をリンクっぽくするためだけに、ClickableSpan をsetすることにする。。。
                }
            }, 0, caption.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            textViewSelectDemoData.setText(spannableString);
            textViewSelectDemoData.setMovementMethod(LinkMovementMethod.getInstance());

            textViewSelectDemoData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSelectDemoDialog();
                }
            });
            textViewSelectDemoData.setVisibility(View.VISIBLE);
        }
        else {
            // 曲ボタンの編集時は表示しない
            textViewSelectDemoData.setVisibility(View.INVISIBLE);
        }

    }

    // デモ用の素材から選ぶダイアログを表示する
    private void showSelectDemoDialog() {
        final Bundle arg = getArguments();
        int defaultIndex = -1;
        if (arg.getInt(ARG_KEY_FILE_TYPE, -1) == SoundData.FILE_TYPE_DEMO) {
            final String fileName = arg.getString(ARG_KEY_FILE_NAME);
            if (fileName != null && !fileName.isEmpty()) {
                final String[] fileList = DemoSoundData.getFileList();
                for (int i = 0; i < fileList.length; ++i) {
                    if (fileName.equals(fileList[i])) {
                        defaultIndex = i;
                        break;
                    }
                }
            }
        }
        ButtonSettingSelectDemoFragment selectDemoFragment
                = ButtonSettingSelectDemoFragment.newInstance(
                ButtonSettingDialog.this,
                REQUEST_SELECT_DEMO_DATA_CODE,
                defaultIndex);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        selectDemoFragment.show(fragmentManager, "select_demo_dialog");
    }

    public void onConfirmDialogPositiveButtonClick() {
        dismiss();
        getListener().onDialogClearClick(this, getArgumentsSoundData());
    }

    public void onConfirmDialogNegativeButtonClick() {

    }

    private void display(final SoundData soundData) {
        final String fileName = pathToFilename(soundData.getFileName());
        final String title = soundData.getTitle();
        _editTitle.setText(title);
        _buttonFile.setText(fileName);
    }

    private SoundData reflect() {
        SoundData editData = new SoundData();
        editData.copy(getArgumentsSoundData());
        editData.setTitle(_editTitle.getText().toString());

        editData.setFileType(getArguments().getInt(ARG_KEY_FILE_TYPE));
        editData.setFileName(getArguments().getString(ARG_KEY_FILE_NAME));
        return editData;
    }

    private void selectFile() {
        Activity activity = getActivity();
        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(activity, permissions, 2000);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        if (activity.getIntent().resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_AUDIO_FILE_CODE);
        }
    }

    private String pathToFilename(final String path) {
        if (path == null) {
            return "";
        }
        File file = new File(path);
        return file.getName();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUDIO_FILE_CODE
                && resultCode == RESULT_OK) {
            // ファイル選択ダイアログからの結果
            Uri uri;
            try {
                uri = data.getData();
            }
            catch (RuntimeException e) {
                return;
            }
            if (uri != null) {
                final int fileType = SoundData.FILE_TYPE_FILE;
                final String filePath = getPathFromUri(getActivity(), uri);
                Bundle arg = getArguments();
                arg.putInt(ARG_KEY_FILE_TYPE, fileType);
                arg.putString(ARG_KEY_FILE_NAME, filePath);

                _buttonFile.setText(pathToFilename(filePath));
            }
        }
        else if (requestCode == REQUEST_SELECT_DEMO_DATA_CODE
                && resultCode == DialogInterface.BUTTON_POSITIVE) {
            // 「デモ用の素材から選ぶ」ダイアログからの結果
            if (data.hasExtra(ButtonSettingSelectDemoFragment.ARG_KEY_INDEX)) {
                final int idx = data.getIntExtra(ButtonSettingSelectDemoFragment.ARG_KEY_INDEX, -1);
                if (idx >= 0) {
                    final SoundData demoData = DemoSoundData.getSoundData(idx);
                    if (demoData != null) {
                        final int fileType = SoundData.FILE_TYPE_DEMO;
                        final String filePath = demoData.getFileName();
                        Bundle arg = getArguments();
                        arg.putInt(ARG_KEY_FILE_TYPE, fileType);
                        arg.putString(ARG_KEY_FILE_NAME, filePath);

                        _buttonFile.setText(demoData.getFileName());
                    }
                }
            }
        }
    }


    // コンテンツID からフルパスのファイル名を取得する
    // http://qiita.com/wakamesoba98/items/98b79bdfde19612d12b0
    // http://stackoverflow.com/questions/32661221/android-cursor-didnt-have-data-column-not-found/33930169#33930169
    private String getPathFromUri(final Context context, final Uri uri) {
        boolean isAfterKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        // Log.e(TAG,"uri:" + uri.getAuthority());

        if (isAfterKitKat && isDocumentUri(context, uri)) {
            return getPathFromUriOver19(context, uri);
        }else if ("content".equalsIgnoreCase(uri.getScheme())) {//MediaStore
            return getDataColumn(context, uri, null, null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private Boolean isDocumentUri(final Context context, final Uri uri) {
        return DocumentsContract.isDocumentUri(context, uri);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getPathFromUriOver19(final Context context, final Uri uri) {
        if ("com.android.externalstorage.documents".equals(
                uri.getAuthority())) {// ExternalStorageProvider
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            }else {
                return "/stroage/" + type +  "/" + split[1];
            }
        }else if ("com.android.providers.downloads.documents".equals(
                uri.getAuthority())) {// DownloadsProvider
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            return getDataColumn(context, contentUri, null, null);
        }else if ("com.android.providers.media.documents".equals(
                uri.getAuthority())) {// MediaProvider
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            // final String type = split[0];
            Uri contentUri = MediaStore.Files.getContentUri("external");
            final String selection = "_id=?";
            final String[] selectionArgs = new String[] {
                    split[1]
            };
            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
        return "";
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String[] projection = {
                MediaStore.Files.FileColumns.DATA
        };
        try {
            cursor = context.getContentResolver().query(
                    uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int cindex = cursor.getColumnIndexOrThrow(projection[0]);
                return cursor.getString(cindex);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}
