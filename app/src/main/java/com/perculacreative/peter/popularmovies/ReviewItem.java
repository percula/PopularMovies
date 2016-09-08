package com.perculacreative.peter.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by peter on 7/1/16.
 */
public class ReviewItem implements Parcelable {
    private String mAuthor;
    private String mContent;

    public ReviewItem(String author, String content) {
        mAuthor = author;
        mContent = content;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getmContent() {
        return mContent;
    }


    // Code to save instance state, with help from http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
    private ReviewItem(Parcel parcel) {
        mAuthor = parcel.readString();
        mContent = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mAuthor);
        parcel.writeString(mContent);
    }

    public static final Creator<ReviewItem> CREATOR = new Creator<ReviewItem>() {
        public ReviewItem createFromParcel(Parcel parcel) {
            return new ReviewItem(parcel);
        }

        public ReviewItem[] newArray(int size) {
            return new ReviewItem[size];
        }
    };
}
