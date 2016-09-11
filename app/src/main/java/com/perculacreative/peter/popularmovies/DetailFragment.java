package com.perculacreative.peter.popularmovies;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    private MovieItem mSelectedMovie;
    public static final String SELECTED_MOVIE_KEY = "SELECTED_MOVIE";

    private boolean isMultipane;

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_MOVIE_KEY)) {
                mSelectedMovie = savedInstanceState.getParcelable(MainActivity.SELECTED_MOVIE_KEY);
                savedInstanceState.clear();
//                Log.v("Get Movie From Save", mSelectedMovie.getmTitle());
            } else {
                Log.v("Get Movie From Save", "null");
            }
        }

        // Get selected movie
        Bundle bundle = this.getArguments();

        // Restore selected movie on screen rotation or from fragment creation
        if (bundle != null) {
            if (bundle.containsKey(MainActivity.SELECTED_MOVIE_KEY)) {
                mSelectedMovie = bundle.getParcelable(MainActivity.SELECTED_MOVIE_KEY);
                bundle.clear();
//                Log.v("Get Movie From Bundle", mSelectedMovie.getmTitle());
            } else {
                Log.v("Get Movie From Bundle", "null");
            }

        }

        // Check if a movie has been selected
        if (mSelectedMovie == null) {
            // Do something if no movie provided yet
            Log.v("Selected Movie", "Is Null");
            return view;
        } else {

            // Set release date
            String date = mSelectedMovie.getmRelease();
            try {
                SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Date oldDate = oldFormat.parse(date);
                SimpleDateFormat newFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
                date = newFormat.format(oldDate);
            } catch (Exception e) {

            }
            String release = String.format(getString(R.string.release), date);
            ((TextView) view.findViewById(R.id.release)).setText(release);

            // Set vote average
            String vote = String.format(getString(R.string.vote), mSelectedMovie.getmVote());
            ((TextView) view.findViewById(R.id.vote)).setText(vote);

            // Set plot synopsis
            String plot = String.format(getString(R.string.plot), mSelectedMovie.getmPlot());
            ((TextView) view.findViewById(R.id.plot)).setText(plot);

            // Set poster
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185/" + mSelectedMovie.getmPoster()).into(imageView);
            return view;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SELECTED_MOVIE_KEY,mSelectedMovie);
    }
}
