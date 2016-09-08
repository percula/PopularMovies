package com.perculacreative.peter.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by peter on 7/1/16.
 */
public class VideoItem implements Parcelable {
    private String mTitle;
    private String mURLKey;

    public VideoItem(String title, String url_key) {
        mTitle = title;
        mURLKey = url_key;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmURLKey() {
        return mURLKey;
    }


    // Code to save instance state, with help from http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
    private VideoItem(Parcel parcel) {
        mTitle = parcel.readString();
        mURLKey = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mTitle);
        parcel.writeString(mURLKey);
    }

    public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
        public VideoItem createFromParcel(Parcel parcel) {
            return new VideoItem(parcel);
        }

        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };
}
