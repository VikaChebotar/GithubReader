package com.example.viktoria.githubreader;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * Created by Вика on 12.02.2015.
 */
public class LoadingProgressDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Загрузка...");
        pd.setCancelable(false);
        return pd;
    }
}
