package com.example.viktoria.githubreader.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.viktoria.githubreader.R;
import com.example.viktoria.githubreader.model.User;
import com.example.viktoria.githubreader.util.ConnectionUtil;

/**
 * Fragment that allows user to enter Github username and search for this user.
 * Notifies activity that user exists and acitivity need to open UserProfileFragment by OnUserProfileOpenListener.
 */
public class SearchFragment extends Fragment {
    private View rootView;
    private OnUserProfileOpenListener mCallback;
    private ProgressDialog pd;

    // interface to communicate with other fragment through activity, fragment shouldn't know about parent activity
    public interface OnUserProfileOpenListener {
        public void onUserProfileOpenClicked(User user);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.search_page, container, false);
        final EditText usernameET = (EditText) rootView.findViewById(R.id.usernameET);
        Button searchBtn = (Button) rootView.findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!usernameET.getText().toString().isEmpty()) {
                    new GetUserInfoAsyncTask().execute(usernameET.getText().toString());
                } else {
                  Toast.makeText(getActivity(), getString(R.string.no_username_toast), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (pd != null && pd.isShowing()) {
            pd.cancel();
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // this makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnUserProfileOpenListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + getActivity().getString(R.string.castExc) + OnUserProfileOpenListener.class);
        }
    }

    /**
     * This async task class execute http request to get User by its username.
     * If request was successful, it notifies activity and pass user object.
     * If not - alert dialog is shown.
     */
    class GetUserInfoAsyncTask extends AsyncTask<String, Void, Integer> {
        LoadingProgressDialogFragment loadingPD;
        String[] params;
        User user;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingPD = new LoadingProgressDialogFragment();
            loadingPD.show(((Activity) mCallback).getFragmentManager(), getString(R.string.loadingPDtag));

        }

        @Override
        protected Integer doInBackground(String... params) {
            this.params = params;
            if (ConnectionUtil.isConnected(getActivity())) {
                try {
                    user = ConnectionUtil.getUserInfo(params[0], getActivity());
                    if (user == null) {
                        return ConnectionUtil.NOT_FOUND;
                    }
                    if (user.getRepos_url() != null && !user.getRepos_url().isEmpty()) {
                        user.setRepositories(ConnectionUtil.getRepositories(user.getRepos_url(), getActivity()));
                    }
                    return ConnectionUtil.OK;
                } catch (Exception e) {
                    Log.e(MainActivity.TAG, e.getMessage());
                    return ConnectionUtil.EXCEPTION;
                }
            } else {
                return ConnectionUtil.NO_INTERNET_CONNECTION;
            }
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            super.onPostExecute(resultCode);
            if (loadingPD != null && loadingPD.isAdded() && loadingPD.isResumed()) {
                loadingPD.dismiss();
            }
            switch (resultCode) {
                case ConnectionUtil.OK:
                    mCallback.onUserProfileOpenClicked(user);
                    break;
                case ConnectionUtil.NOT_FOUND:

                    new NotFoundUserDialogFragment().show(((Activity) mCallback).getFragmentManager(), getString(R.string.notFountUserDFtag));
                    break;
                case ConnectionUtil.NO_INTERNET_CONNECTION:
                    if (getFragmentManager() != null)
                        new NoInternetConDialogFragment().show(((Activity) mCallback).getFragmentManager(), getString(R.string.noIntConDFtag));
                    break;
                case ConnectionUtil.EXCEPTION:
                    if (getFragmentManager() != null)
                        new ExceptionDialogFragment().show(((Activity) mCallback).getFragmentManager(), getString(R.string.excDFtag));
                    break;
            }


        }
    }
}
