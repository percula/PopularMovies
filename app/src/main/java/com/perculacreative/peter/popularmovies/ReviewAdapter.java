package com.perculacreative.peter.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by peter on 7/1/16.
 */
public class ReviewAdapter extends ArrayAdapter<ReviewItem> {

    //Note: I adapted some of this code from the Miwok application.

    /**
     * This is the constructor
     * @param context       The current context. Used to inflate the layout file.
     * @param reviews        A List of objects to display in a list
     */
    public ReviewAdapter(Activity context, ArrayList<ReviewItem> reviews) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, reviews);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position The position in the list of data that should be displayed in the
     *                 list item view.
     * @param convertView The recycled view to populate.
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent, false);
        }

        // Get the location object located at this position in the list
        ReviewItem currentReview = getItem(position);

        String review = currentReview.getmContent();
//        if (review.length() > 140) {
//            review = currentReview.getmContent()
//                    .substring(0, 140) + "...";
//        }

        // Find the textview and set the text
        ((TextView) listItemView.findViewById(R.id.review_list_author)).setText(currentReview.getmAuthor());
        ((TextView) listItemView.findViewById(R.id.review_list_content)).setText(review);


        return listItemView;
    }
}
