package com.example.viktoria.githubreader.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * This is class represents user. Contains list of Repository objects,
 * Implements Parcelable to enable putting User objects in bundle to save instanse state or pass it between fragments in Intent
 */
public class User implements Parcelable {
    private String login;
    private String company;
    private String avatar_url;
    private String html_url;
    private String repos_url;
    private int followers;
    private int following;
    private ArrayList<Repository> repositories = new ArrayList<Repository>();

    public User() {

    }

    private User(Parcel parcel) {
        login = parcel.readString();
        company = parcel.readString();
        avatar_url = parcel.readString();
        html_url = parcel.readString();
        repos_url = parcel.readString();
        followers = parcel.readInt();
        following = parcel.readInt();
        parcel.readTypedList(repositories, Repository.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(login);
        parcel.writeString(company);
        parcel.writeString(avatar_url);
        parcel.writeString(html_url);
        parcel.writeString(repos_url);
        parcel.writeInt(followers);
        parcel.writeInt(following);
        parcel.writeTypedList(repositories);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        // get object from parcel
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public ArrayList<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(ArrayList<Repository> repositories) {
        this.repositories = repositories;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public String getRepos_url() {
        return repos_url;
    }

    public void setRepos_url(String repos_url) {
        this.repos_url = repos_url;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }
}
