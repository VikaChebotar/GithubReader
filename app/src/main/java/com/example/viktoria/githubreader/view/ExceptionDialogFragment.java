package com.example.viktoria.githubreader.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.viktoria.githubreader.R;

/**
 * This DialogFragment prompts that some exception occured during http request.
 */
public class ExceptionDialogFragment  extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setMessage(getString(R.string.exc_dialog))
                .setCancelable(true)
                .setNegativeButton(getString(R.string.btn_dialog),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        return builder.create();
    }

}