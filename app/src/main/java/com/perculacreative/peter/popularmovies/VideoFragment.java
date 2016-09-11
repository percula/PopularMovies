package com.perculacreative.peter.popularmovies;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
public class VideoFragment extends Fragment {

    private MovieItem mSelectedMovie;
    private ArrayList<VideoItem> mVideoList = new ArrayList<>();
    private VideoAdapter mVideoAdapter;

    private String API_KEY_LABEL = "api_key";
    private String API_KEY = BuildConfig.API_KEY;

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

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

        // Set video list
        mVideoAdapter = new VideoAdapter(getActivity(), mVideoList);
        ListView videoListView = (ListView) view.findViewById(R.id.video_list);
        videoListView.setAdapter(mVideoAdapter);
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                VideoItem selectedVideo = mVideoAdapter.getItem(i);
                final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
                final String VIDEO_KEY = "v";
                Uri videoURI = Uri.parse(YOUTUBE_BASE_URL)
                        .buildUpon().appendQueryParameter(VIDEO_KEY, selectedVideo.getmURLKey())
                        .build();
                Log.v("YoutubeURL", videoURI.toString());
                startActivity(new Intent(Intent.ACTION_VIEW, videoURI));
            }
        });

        // Check if a movie has been selected
        if (mSelectedMovie == null) {
            Log.v("NO MOVIE", "OH NO!");
            // Do something if no movie provided yet
        } else {
            if (mSelectedMovie.hasVideos()) {
                Log.v("Already Has Videos", "Yup");
                mVideoList.addAll(mSelectedMovie.getmVideos());
                mVideoAdapter.notifyDataSetChanged();
            } else {
                Log.v("Getting Videos", "GO");
                FetchVideosTask fetchVideosTask = new FetchVideosTask();
                fetchVideosTask.execute();
            }
        }
        return view;
    }

    public MovieItem getSelectedMovie() {
        return mSelectedMovie;
    }

    public class FetchVideosTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchVideosTask.class.getSimpleName();
        private ProgressDialog progressDialog;


        /**
         * Parses JSON data and returns a string array for the list adapter
         */
        private void getVideoAndReviewDataFromJson(String videosJSONStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MV_RESULTS = "results";
            final String VIDEO_TITLE = "name";
            final String VIDEO_URL_KEY = "key";

            JSONObject videosJson = new JSONObject(videosJSONStr);

            JSONArray videosArray = videosJson.getJSONArray(MV_RESULTS);

            for (int i = 0; i < videosArray.length(); i++) {
                // Get the JSON object representing the current movie
                JSONObject currentVideo = videosArray.getJSONObject(i);

                VideoItem video = new VideoItem(currentVideo.getString(VIDEO_TITLE), currentVideo.getString(VIDEO_URL_KEY));

                // Get video info
                mVideoList.add(video);
            }
            mSelectedMovie.setmVideos(mVideoList);
        }

        // I used help from http://stackoverflow.com/a/9170457 for this progress bar code
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();

            mVideoList.clear();
        }

        @Override
        protected String[] doInBackground(Void... params) {

            HttpURLConnection videosURLConnection = null;

            BufferedReader videosReader = null;

            String videosJsonStr = null;

            try {
                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/movie/";
                final String MOVIE_VIDEOS = "videos";

                Uri vidoesUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(mSelectedMovie.getmID())
                        .appendPath(MOVIE_VIDEOS)
                        .appendQueryParameter(API_KEY_LABEL, API_KEY)
                        .build();

                URL videosURL = new URL(vidoesUri.toString());

                Log.v(LOG_TAG, "Videos URI " + vidoesUri.toString());

                // Create the request to API, and open the connection
                videosURLConnection = (HttpURLConnection) videosURL.openConnection();
                videosURLConnection.setRequestMethod("GET");
                videosURLConnection.connect();

                // Read the input stream into a String
                InputStream videosInputStream = videosURLConnection.getInputStream();
                StringBuffer videosBuffer = new StringBuffer();
                if (videosInputStream == null) {
                    return null;
                }

                videosReader = new BufferedReader(new InputStreamReader(videosInputStream));

                String videosLine;
                while ((videosLine = videosReader.readLine()) != null) {
                    videosBuffer.append(videosLine + "\n");
                }

                if (videosBuffer.length() == 0) {
                    return null;
                }

                videosJsonStr = videosBuffer.toString();

                Log.v(LOG_TAG, "Videos String: " + videosJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (videosURLConnection != null) {
                    videosURLConnection.disconnect();
                }
                if (videosReader != null) {
                    try {
                        videosReader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                getVideoAndReviewDataFromJson(videosJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mVideoAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
            Log.v("done", "done");
        }
    }
}
