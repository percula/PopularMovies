package com.perculacreative.peter.popularmovies;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends Fragment {

    private MovieItem mSelectedMovie;
    private ArrayList<ReviewItem> mReviewList = new ArrayList<>();
    private ReviewAdapter mReviewAdapter;

    private String API_KEY_LABEL = "api_key";
    private String API_KEY = BuildConfig.API_KEY;

    public ReviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        // Get selected movie
        Bundle bundle = this.getArguments();

        // Restore selected movie on screen rotation or from fragment creation
        if (savedInstanceState != null && savedInstanceState.containsKey(MainActivity.SELECTED_MOVIE_KEY)) {
            mSelectedMovie = savedInstanceState.getParcelable(MainActivity.SELECTED_MOVIE_KEY);
            if (mSelectedMovie != null) {
            }
        } else if (bundle != null) {
            mSelectedMovie = bundle.getParcelable(MainActivity.SELECTED_MOVIE_KEY);
        }

        // Set review list
        mReviewAdapter = new ReviewAdapter(getActivity(), mReviewList);
        ListView reviewListView = (ListView) view.findViewById(R.id.review_list);
        reviewListView.setAdapter(mReviewAdapter);

        // Check if a movie has been selected
        if (mSelectedMovie == null) {
            // Do something if no movie provided yet
        } else {
            if (mSelectedMovie.hasReviews()) {
                mReviewList.addAll(mSelectedMovie.getmReviews());
                mReviewAdapter.notifyDataSetChanged();
            } else {
                getReviewData(view);
            }
        }
        return view;
    }

    private void getReviewData(View view) {
        if (isOnline()) {
            view.findViewById(R.id.network_error).setVisibility(View.GONE);
            FetchReviewsTask fetchReviewsTask = new FetchReviewsTask();
            fetchReviewsTask.execute();
        } else {
            view.findViewById(R.id.network_error).setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This checks if there is an internet connection. This is from http://stackoverflow.com/a/4009133.
     *
     * @return boolean whether the user has an internet connection.
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class FetchReviewsTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        /**
         * Parses JSON data and returns a string array for the list adapter
         */
        private void getReviewDataFromJson(String reviewsJSONStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MV_RESULTS = "results";
            final String REVIEW_AUTHOR = "author";
            final String REVIEW_CONTENT = "content";

            JSONObject reviewsJson = new JSONObject(reviewsJSONStr);

            JSONArray reviewsArray = reviewsJson.getJSONArray(MV_RESULTS);

            ArrayList<ReviewItem> reviews = new ArrayList<>();

            for (int i = 0; i < reviewsArray.length(); i++) {
                // Get the JSON object representing the current movie
                JSONObject currentReview = reviewsArray.getJSONObject(i);

                // Get video info
                mReviewList.add(new ReviewItem(currentReview.getString(REVIEW_AUTHOR), currentReview.getString(REVIEW_CONTENT)));
            }
            mSelectedMovie.setmReviews(mReviewList);
        }

        @Override
        protected String[] doInBackground(Void... params) {

            HttpURLConnection reviewsURLConnection = null;

            BufferedReader reviewsReader = null;

            String reviewsJsonStr = null;

            try {
                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/movie/";
                final String MOVIE_REVIEWS = "reviews";

                Uri reviewsUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(mSelectedMovie.getmID())
                        .appendPath(MOVIE_REVIEWS)
                        .appendQueryParameter(API_KEY_LABEL, API_KEY)
                        .build();

                URL reviewsURL = new URL(reviewsUri.toString());

                Log.v(LOG_TAG, "Reviews URI " + reviewsUri.toString());

                // Create the request to API, and open the connection
                reviewsURLConnection = (HttpURLConnection) reviewsURL.openConnection();
                reviewsURLConnection.setRequestMethod("GET");
                reviewsURLConnection.connect();

                // Read the input stream into a String
                InputStream reviewsInputStream = reviewsURLConnection.getInputStream();
                StringBuffer reviewsBuffer = new StringBuffer();
                if (reviewsInputStream == null) {
                    return null;
                }

                reviewsReader = new BufferedReader(new InputStreamReader(reviewsInputStream));

                String reviewsLine;
                while ((reviewsLine = reviewsReader.readLine()) != null) {
                    reviewsBuffer.append(reviewsLine + "\n");
                }

                if (reviewsBuffer.length() == 0) {
                    return null;
                }

                reviewsJsonStr = reviewsBuffer.toString();

                Log.v(LOG_TAG, "Reviews String: " + reviewsJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (reviewsURLConnection != null) {
                    reviewsURLConnection.disconnect();
                }
                if (reviewsReader != null) {
                    try {
                        reviewsReader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                getReviewDataFromJson(reviewsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mReviewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MainActivity.SELECTED_MOVIE_KEY,mSelectedMovie);
    }

}
