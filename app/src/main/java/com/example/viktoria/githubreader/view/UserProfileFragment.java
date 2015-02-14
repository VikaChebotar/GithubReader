package com.example.viktoria.githubreader.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.viktoria.githubreader.R;
import com.example.viktoria.githubreader.model.User;
import com.example.viktoria.githubreader.util.DatabaseHandler;

import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This fragment shows user information and repositories.
 * The object of user is passed to it through intent arguments.
 * Fragment view has 3 buttons:
 * to open user profile in browser (activity with intent with ACTION_VIEW is called),
 * to save user information in local database (DatabaseHandler is used),
 * to share user profile link in facebook (fragment notifies activity, that is responsible for that).
 */
public class UserProfileFragment extends Fragment {
    private View rootView;
    private User user;
    private TextView usernameCompany, followers, following;
    private CircleImageView avatar;
    private ImageButton openBrowserBtn, saveBtn, shareBtn;
    private ListView repList;
    private OnShareFbClickListener mCallback;

    // interface to communicate with other fragment through activity, fragment shouldn't know about parent activity
    public interface OnShareFbClickListener {
        public void onShareClicked();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.user_profile, container, false);
        user = getArguments().getParcelable(getString(R.string.user_intent_key));
        usernameCompany = (TextView) rootView.findViewById(R.id.usernameCompany);
        followers = (TextView) rootView.findViewById(R.id.followers);
        following = (TextView) rootView.findViewById(R.id.following);
        avatar = (CircleImageView) rootView.findViewById(R.id.profile_image);
        openBrowserBtn = (ImageButton) rootView.findViewById(R.id.openBrowserBtn);
        saveBtn = (ImageButton) rootView.findViewById(R.id.saveBtn);
        shareBtn = (ImageButton) rootView.findViewById(R.id.shareBtn);
        repList = (ListView) rootView.findViewById(R.id.repositoriesList);
        if (user.getCompany() != null & !user.getCompany().isEmpty()) {
            usernameCompany.setText(user.getLogin() + ", " + user.getCompany());
        } else {
            usernameCompany.setText(user.getLogin());
        }
        followers.setText(user.getFollowers() + System.getProperty("line.separator") + getString(R.string.followers) + System.getProperty("line.separator"));
        following.setText(user.getFollowing() + System.getProperty("line.separator") + getString(R.string.following) + System.getProperty("line.separator"));
        if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
            new DownloadImageTask(avatar)
                    .execute(user.getAvatar_url());
        }
        openBrowserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = user.getHtml_url();
                if (url != null && !url.isEmpty()) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask() {
                    boolean isAlreadyExists = false;
                    @Override
                    protected Object doInBackground(Object[] params) {
                        DatabaseHandler dh = DatabaseHandler.getInstance(getActivity());
                        if (!dh.checkIfExists(user)) {
                            dh.addUsers(user);
                            isAlreadyExists=false;
                        } else {
                            isAlreadyExists=true;
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        if(isAlreadyExists){
                            Toast.makeText(getActivity(),getString(R.string.toast_save_db), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getActivity(),getString(R.string.save_db_suc), Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();

            }

        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onShareClicked();
            }
        });
        if (user.getRepositories() != null || user.getRepositories().size()==0) {
            RepositoriesListAdapter adapter = new RepositoriesListAdapter(getActivity(), R.layout.repository_item, user.getRepositories());
            repList.setVisibility(View.VISIBLE);
            repList.setAdapter(adapter);
            //adjust scroll view to show all listview
            setListViewHeightBasedOnChildren(repList);
            final ScrollView sView = (ScrollView) rootView.findViewById(R.id.mainProfileCont);
            //scroll to top
            sView.post(new Runnable() {
                @Override
                public void run() {
                    sView.scrollTo(0, 0);
                }
            });
        }
        else{
            rootView.findViewById(R.id.repositoriesLabel).setVisibility(View.GONE);
            rootView.findViewById(R.id.line).setVisibility(View.GONE);
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // this makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnShareFbClickListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + getActivity().getString(R.string.castExc) + OnShareFbClickListener.class);
        }
    }

    /**
     * Async task that downloads user avatar and sets it to ImageView passed in constructor.
     * If image is not downloaded - sets placeholder.
     * Shows progress bar while downloading.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        CircleImageView bmImage;
        ProgressBar pb;

        public DownloadImageTask(CircleImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb = (ProgressBar) rootView.findViewById(R.id.progressBar3);
            pb.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(MainActivity.TAG, e.getMessage());
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                bmImage.setImageBitmap(result);
            } else {
                bmImage.setImageDrawable(getResources().getDrawable(R.drawable.avatar_placeholder));
            }
            pb.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.user_intent_key), user);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable(getString(R.string.user_intent_key)

            );
        }
    }

    /**
     * resize listview to exactly accommodate the height of its items
     * @param listView listview that will be changed
     */
    protected static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount())) + listView.getPaddingTop() + listView.getPaddingBottom();
        listView.setLayoutParams(params);
        listView.requestLayout();

    }


}
