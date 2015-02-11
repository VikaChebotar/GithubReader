package com.example.viktoria.githubreader;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by viktoria on 11.02.15.
 */
public class UserProfileFragment extends Fragment {
    private View rootView;
    public static final int OK = 1;
    public static final int NO_INTERNET_CONNECTION = 2;
    public static final int SERVER_ERROR = 3;
    private User user;
    private TextView usernameCompany, followers, following;
    private CircleImageView avatar;
    private ImageButton openBrowserBtn, saveBtn, shareBtn;
    private ListView repList;
    private OnShareFbClickListener mCallback;

    // interface to communicate with other fragment through activity, fragment shouldn't know about parent activity
    public interface OnShareFbClickListener {
        public void onShareClicked(User u);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.user_profile, container, false);
        String username = getArguments().getString("username");
        new GetUserInfoAsyncTask().execute(username);
        usernameCompany = (TextView) rootView.findViewById(R.id.usernameCompany);
        followers = (TextView) rootView.findViewById(R.id.followers);
        following = (TextView) rootView.findViewById(R.id.following);
        avatar = (CircleImageView) rootView.findViewById(R.id.profile_image);
        openBrowserBtn = (ImageButton) rootView.findViewById(R.id.openBrowserBtn);
        saveBtn = (ImageButton) rootView.findViewById(R.id.saveBtn);
        shareBtn = (ImageButton) rootView.findViewById(R.id.shareBtn);
        repList = (ListView) rootView.findViewById(R.id.repositoriesList);

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
                    + " " + getActivity().getString(R.string.castExc) + " " + OnShareFbClickListener.class);
        }
    }

    class GetUserInfoAsyncTask extends AsyncTask<String, Void, Integer> {
        RelativeLayout progressBar1cont;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar1cont = (RelativeLayout) rootView.findViewById(R.id.progressBar1cont);
            progressBar1cont.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            if (isConnected(getActivity())) {
                String u = "https://api.github.com/users/" + params[0];
                Log.d(MainActivity.TAG, u);
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet(u);
                    //httpget.addHeader();
                    HttpResponse response = httpclient.execute(httpget);
                    int status = response.getStatusLine().getStatusCode();
                    Log.d(MainActivity.TAG, "status: " + status);
                    HttpEntity entity = response.getEntity();
                    String resp = EntityUtils.toString(entity);
                    Log.d(MainActivity.TAG, "resp: " + resp);
                    if (status >= 200 && status < 300) {
                        try {
                            JSONObject obj = new JSONObject(resp);
                            user = new User();
                            user.setLogin(obj.getString("login"));
                            user.setCompany(obj.optString("company", ""));
                            user.setAvatar_url(obj.optString("avatar_url", ""));
                            user.setHtml_url(obj.optString("html_url", ""));
                            user.setRepos_url(obj.optString("repos_url", ""));
                            user.setFollowers(obj.getInt("followers"));
                            user.setFollowing(obj.getInt("following"));
                            return OK;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        //TODO
                    }

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                return NO_INTERNET_CONNECTION;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            super.onPostExecute(resultCode);
            switch (resultCode) {
                case OK:
                    if (user.getCompany() != null & !user.getCompany().isEmpty()) {
                        usernameCompany.setText(user.getLogin() + ", " + user.getCompany());
                    } else {
                        usernameCompany.setText(user.getLogin());
                    }
                    followers.setText(user.getFollowers() + System.getProperty("line.separator") + " followers" + System.getProperty("line.separator"));
                    following.setText(user.getFollowing() + System.getProperty("line.separator") + " following" + System.getProperty("line.separator"));
                    if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
                        new DownloadImageTask(avatar)
                                .execute(user.getAvatar_url());
                    }
                    progressBar1cont.setVisibility(View.GONE);
                    if (user.getRepos_url() != null && !user.getRepos_url().isEmpty()) {
                        new GetRepositoriesAsyncTask().execute(user.getRepos_url());
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
                                @Override
                                protected Object doInBackground(Object[] params) {
                                    DatabaseHandler dh = DatabaseHandler.getInstance(getActivity());
                                    if (!dh.checkIfExists(user)) {
                                        dh.addUsers(user);
                                    } else {
                                        //TODO
                                    }

                                    return null;
                                }
                            }.execute();

                        }
                    });
                    shareBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCallback.onShareClicked(user);
                        }
                    });
                    break;
            }


        }
    }

    class GetRepositoriesAsyncTask extends AsyncTask<String, Void, Integer> {
        RelativeLayout progressBar2cont;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar2cont = (RelativeLayout) rootView.findViewById(R.id.progressBar1cont);
            progressBar2cont.setVisibility(View.VISIBLE);
            repList.setVisibility(View.GONE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            if (isConnected(getActivity())) {
                String u = params[0] + "?sort=updated";
                Log.d(MainActivity.TAG, u);
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet(u);
                    HttpResponse response = httpclient.execute(httpget);
                    int status = response.getStatusLine().getStatusCode();
                    Log.d(MainActivity.TAG, "status: " + status);
                    HttpEntity entity = response.getEntity();
                    String resp = EntityUtils.toString(entity);
                    Log.d(MainActivity.TAG, "resp: " + resp);
                    if (status >= 200 && status < 300) {
                        try {
                            JSONArray array = new JSONArray(resp);
                            ArrayList<Repository> repositories = new ArrayList<Repository>(array.length());
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                Repository r = new Repository();
                                r.setName(obj.getString("name"));
                                r.setLanguage(obj.optString("language", ""));
                                r.setForks(obj.getInt("forks_count"));
                                r.setWatchers(obj.getInt("watchers_count"));
                                repositories.add(r);
                            }
                            user.setRepositories(repositories);
                            return OK;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        //TODO
                    }

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                return NO_INTERNET_CONNECTION;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            super.onPostExecute(resultCode);

            switch (resultCode) {
                case OK:
                    RepositoriesListAdapter adapter = new RepositoriesListAdapter(getActivity(), R.layout.repository_item, user.getRepositories());
                    repList.setVisibility(View.VISIBLE);
                    repList.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(repList);

                    final ScrollView sView = (ScrollView) rootView.findViewById(R.id.mainProfileCont);
                    sView.post(new Runnable() {
                        @Override
                        public void run() {
                            sView.scrollTo(0, 0);
                        }
                    });
                    progressBar2cont.setVisibility(View.GONE);
                    break;
            }


        }
    }

    public static boolean isConnected(Context mContext) {
        ConnectivityManager connMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

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
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            pb.setVisibility(View.GONE);
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
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
