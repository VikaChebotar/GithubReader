package com.example.viktoria.githubreader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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
import java.util.ArrayList;

/**
 * Created by viktoria on 12.02.15.
 */
public class ConnectionUtil {

    public static final int OK = 1;
    public static final int NO_INTERNET_CONNECTION = 2;
    public static final int EXCEPTION = 3;
    public static final int NOT_FOUND = 4;


    public static boolean isConnected(Context mContext) {
        ConnectivityManager connMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public static User getUserInfo(String username) throws Exception {
        String u = "https://api.github.com/users/" + username;
        User user;
        Log.d(MainActivity.TAG, u);
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(u);
        HttpResponse response = httpclient.execute(httpget);
        int status = response.getStatusLine().getStatusCode();
        Log.d(MainActivity.TAG, "status: " + status);
        HttpEntity entity = response.getEntity();
        String resp = EntityUtils.toString(entity);
        Log.d(MainActivity.TAG, "resp: " + resp);
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
            throw new Exception("getUserInfo: "+resp);
        }
    }

    public static ArrayList<Repository> getRepositories(String url) throws Exception {
        String u = url + "?sort=updated";
        Log.d(MainActivity.TAG, u);
        ArrayList<Repository> repositories;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(u);
        HttpResponse response = httpclient.execute(httpget);
        int status = response.getStatusLine().getStatusCode();
        Log.d(MainActivity.TAG, "status: " + status);
        HttpEntity entity = response.getEntity();
        String resp = EntityUtils.toString(entity);
        Log.d(MainActivity.TAG, "resp: " + resp);
        if (status >= 200 && status < 300) {
            JSONArray array = new JSONArray(resp);
            repositories = new ArrayList<Repository>(array.length());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Repository r = new Repository();
                r.setName(obj.getString("name"));
                r.setLanguage(obj.optString("language", ""));
                r.setForks(obj.getInt("forks_count"));
                r.setWatchers(obj.getInt("watchers_count"));
                repositories.add(r);
            }
            return repositories;
        } else {
            throw new Exception("getRepositories: "+resp);
        }

    }
}
