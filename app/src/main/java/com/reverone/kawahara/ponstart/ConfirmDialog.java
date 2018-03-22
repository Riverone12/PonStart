package com.reverone.kawahara.ponstart;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

/**
 * 確認ダイアログ
 * Created by kawahara on 2017/05/16.
 */

public class ConfirmDialog extends DialogFragment {
    interface Listener {
        void onConfirmDialogPositiveButtonClick();
        void onConfirmDialogNegativeButtonClick();
    }
    private static final String ARG_KEY_MESSAGE = "ArgKeyMessage";

    static ConfirmDialog newInstance(final String message, Listener listener) {
        if (listener == null || !(listener instanceof Fragment)) {
            throw new RuntimeException("listener is not Fragment");
        }

        ConfirmDialog dialog = new ConfirmDialog();

        Bundle args = new Bundle();
        args.putString(ARG_KEY_MESSAGE, message);
        dialog.setArguments(args);

        dialog.setTargetFragment((Fragment) listener, 1);
        return dialog;
    }
    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getString(ARG_KEY_MESSAGE));
        builder.setPositiveButton(
                getString(R.string.dialog_button_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getListener().onConfirmDialogPositiveButtonClick();
                    }
                });
        builder.setNegativeButton(
                getString(R.string.dialog_button_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getListener().onConfirmDialogNegativeButtonClick();
                    }
                });

        return builder.create();
    }

    private Listener getListener() {
        return ((Listener) getTargetFragment());
    }
}
