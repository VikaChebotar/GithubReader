package com.example.viktoria.githubreader.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.viktoria.githubreader.view.MainActivity;
import com.example.viktoria.githubreader.R;
import com.example.viktoria.githubreader.model.Repository;
import com.example.viktoria.githubreader.model.User;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Util class that consists of static methods and constants.
 */
public class ConnectionUtil {
    //constants used in async tasks to pass result code of api call
    public static final int OK = 1;
    public static final int NO_INTERNET_CONNECTION = 2;
    public static final int EXCEPTION = 3;
    public static final int NOT_FOUND = 4;

    /**
     * Checks if any internet connection exists
     * @param mContext Context, is used to get System Service
     * @return true if connection active, false if device is not connected to Internet
     */
    public static boolean isConnected(Context mContext) {
        ConnectivityManager connMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    /**
     * Executes call to Github API to get user info by its username. Return null if no user with such username found.
     * @param username Unique username to find this user
     * @param context Context to get some strings from resources
     * @return User object that match username
     * @throws Exception IOEception, JSONException and other exceptions that can be thrown while executing method.
     */
    public static User getUserInfo(String username, Context context) throws Exception {
        String u = context.getString(R.string.url_get_users) + username;
        User user;
        Log.d(MainActivity.TAG, u);
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(u);
        HttpResponse response = httpclient.execute(httpget);
        int status = response.getStatusLine().getStatusCode();
        Log.d(MainActivity.TAG, context.getString(R.string.status) + status);
        HttpEntity entity = response.getEntity();
        String resp = EntityUtils.toString(entity);
        Log.d(MainActivity.TAG, context.getString(R.string.response) + resp);
        if (status >= 200 && status < 300) {
            JSONObject obj = new JSONObject(resp);
            user = new User();
            user.setLogin(obj.getString("login"));
            user.setCompany(obj.optString("company", ""));
            user.setAvatar_url(obj.optString("avatar_url", ""));
            user.setHtml_url(obj.optString("html_url", ""));
            user.setRepos_url(obj.optString("repos_url", ""));
            user.setFollowers(obj.getInt("followers"));
            user.setFollowing(obj.getInt("following"));
            return user;
        } else if (status == 404) {
            return null;
        } else {
            throw new Exception(resp);
        }
    }

    /**
     * Executes call to Github API to get user repositories. If no repositories found - empy list will be returned. Never return null.
     * @param url string url from user object to get repositories
     * @param context Context to get some strings from resources
     * @return list of repositories
     * @throws Exception IOEception, JSONException and other exceptions that can be thrown while executing method.
     */
    public static ArrayList<Repository> getRepositories(String url, Context context) throws Exception {
        String u = url + "?sort=updated";
        Log.d(MainActivity.TAG, u);
        ArrayList<Repository> repositories;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(u);
        HttpResponse response = httpclient.execute(httpget);
        int status = response.getStatusLine().getStatusCode();
        Log.d(MainActivity.TAG, context.getString(R.string.status) + status);
        HttpEntity entity = response.getEntity();
        String resp = EntityUtils.toString(entity);
        Log.d(MainActivity.TAG, context.getString(R.string.response) + resp);
        if (status >= 200 && status < 300) {
            JSONArray array = new JSONArray(resp);
            repositories = new ArrayList<Repository>(array.length());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Repository r = new Repository();
                r.setName(obj.getString("name"));
                r.setLanguage(obj.optString("language", ""));
                if(r.getLanguage().equals("null")){
                    r.setLanguage("");
                }
                r.setForks(obj.getInt("forks_count"));
                r.setWatchers(obj.getInt("watchers_count"));
                repositories.add(r);
            }
            return repositories;
        } else {
            throw new Exception(resp);
        }

    }
}
