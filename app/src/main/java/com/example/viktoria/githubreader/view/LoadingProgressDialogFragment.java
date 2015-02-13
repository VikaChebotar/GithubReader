package com.example.viktoria.githubreader.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.example.viktoria.githubreader.R;

/**
 * Progress Dialog Fragment.
 */
public class LoadingProgressDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage(getString(R.string.loading_dialog));
        pd.setCancelable(false);
        return pd;
    }
}
