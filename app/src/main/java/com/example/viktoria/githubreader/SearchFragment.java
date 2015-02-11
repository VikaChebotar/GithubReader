package com.example.viktoria.githubreader;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by viktoria on 11.02.15.
 */
public class SearchFragment extends Fragment {
    private View rootView;
    private OnSearchClickListener mCallback;

    // interface to communicate with other fragment through activity, fragment shouldn't know about parent activity
    public interface OnSearchClickListener {
        public void onSearchClicked(String username);
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
                    mCallback.onSearchClicked(usernameET.getText().toString());
                } else {
                    //TODO
                }
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // this makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSearchClickListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " " + getActivity().getString(R.string.castExc) + " " + OnSearchClickListener.class);
        }
    }
}
