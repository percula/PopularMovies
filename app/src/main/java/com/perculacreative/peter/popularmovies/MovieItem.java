package com.perculacreative.peter.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by peter on 7/1/16.
 */
public class MovieItem implements Parcelable {
    private String mID;
    private String mTitle;
    private String mPoster;
    private String mRelease;
    private String mPlot;
    private String mVote;
    private ArrayList<VideoItem> mVideos = new ArrayList<>();
    private ArrayList<ReviewItem> mReviews = new ArrayList<>();

    public MovieItem(String id, String title, String poster, String release, String vote, String plot) {
        mID = id;
        mTitle = title;
        mPoster = poster;
        mRelease = release;
        mVote = vote;
        mPlot = plot;
    }

    public String getmID() {
        return mID;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmPoster() {
        return mPoster;
    }

    public String getmPlot() {
        return mPlot;
    }

    public String getmRelease() {
        return mRelease;
    }

    public String getmVote() {
        return mVote;
    }

    // Got help on how to save parcelable arraylists from: http://stackoverflow.com/questions/4704747/arraylist-in-parcelable-object
    public ArrayList<VideoItem> getmVideos() {
        return mVideos;
    }

    public ArrayList<ReviewItem> getmReviews() {
        return mReviews;
    }

    public void setmVideos(ArrayList<VideoItem> videos) {
        mVideos = videos;
    }

    public void setmReviews(ArrayList<ReviewItem> reviews) {
        mReviews = reviews;
    }

    public boolean hasVideos() {
        return (mVideos.size() > 0);
    }

    public boolean hasReviews() {
        return (mReviews.size() > 0);
    }

    // Code to save instance state, with help from http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
    private MovieItem(Parcel parcel) {
        mID = parcel.readString();
        mTitle = parcel.readString();
        mPoster = parcel.readString();
        mRelease = parcel.readString();
        mVote = parcel.readString();
        mPlot = parcel.readString();
        mVideos = new ArrayList<VideoItem>();
        parcel.readTypedList(mVideos, VideoItem.CREATOR);
        mReviews = new ArrayList<ReviewItem>();
        parcel.readTypedList(mReviews, ReviewItem.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mID);
        parcel.writeString(mTitle);
        parcel.writeString(mPoster);
        parcel.writeString(mRelease);
        parcel.writeString(mVote);
        parcel.writeString(mPlot);
        parcel.writeTypedList(mVideos);
        parcel.writeTypedList(mReviews);
    }

    public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>() {
        public MovieItem createFromParcel(Parcel parcel) {
            return new MovieItem(parcel);
        }

        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };
}
