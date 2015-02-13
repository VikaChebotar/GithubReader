package com.example.viktoria.githubreader.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.viktoria.githubreader.R;

/**
 * DialogFragment that prompts user that no Github user found with entered username
 */
public class NotFoundUserDialogFragment extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setMessage(getString(R.string.user_not_found_dialog))
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
