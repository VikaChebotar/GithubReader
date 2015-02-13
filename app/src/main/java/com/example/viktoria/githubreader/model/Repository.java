package com.example.viktoria.githubreader.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This is class represents repository.
 * Implements Parcelable to enable putting User objects in bundle to save instanse state or pass it between fragments in Intent
 */
public class Repository implements Parcelable {
    private String name;
    private String language;
    private int forks;
    private int watchers;

    public Repository() {
    }


    private Repository(Parcel parcel) {
        name = parcel.readString();
        language = parcel.readString();
        forks = parcel.readInt();
        watchers = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(language);
        parcel.writeInt(forks);
        parcel.writeInt(watchers);
    }

    public static final Parcelable.Creator<Repository> CREATOR = new Parcelable.Creator<Repository>() {
        // get object from parcel
        public Repository createFromParcel(Parcel in) {
            return new Repository(in);
        }

        public Repository[] newArray(int size) {
            return new Repository[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getForks() {
        return forks;
    }

    public void setForks(int forks) {
        this.forks = forks;
    }

    public int getWatchers() {
        return watchers;
    }

    public void setWatchers(int watchers) {
        this.watchers = watchers;
    }
}
