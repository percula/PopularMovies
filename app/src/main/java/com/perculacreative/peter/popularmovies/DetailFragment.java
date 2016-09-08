package com.perculacreative.peter.popularmovies;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

    private boolean isMultipane;

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        // Get selected movie
        Bundle bundle = this.getArguments();

        // Restore selected movie on screen rotation or from fragment creation
        if (savedInstanceState != null && savedInstanceState.containsKey(MainActivity.SELECTED_MOVIE_KEY)) {
            mSelectedMovie = savedInstanceState.getParcelable(MainActivity.SELECTED_MOVIE_KEY);
            if (mSelectedMovie != null) {
                Log.v("Restore Saved Movie", mSelectedMovie.getmTitle());
            }
        } else if (bundle != null) {
            mSelectedMovie = bundle.getParcelable(MainActivity.SELECTED_MOVIE_KEY);
            Log.v("SavedInstanceState", "DoesntHaveParcelable");
        }

        // Check if a movie has been selected
        if (mSelectedMovie == null) {
            // Do something if no movie provided yet
        } else {
            // Create variable for poster imageview for later use
            ImageView backgroundImageView;

            // Determine if in multi-pane mode or not
            if (getActivity().findViewById(R.id.toolbar_layout) != null) {
                isMultipane = false;

                // Set title for non-multipane mode
                ((CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout)).setTitle(mSelectedMovie.getmTitle());

                // Find poster imageview
                backgroundImageView = (ImageView) getActivity().findViewById(R.id.poster_background_activity);
            } else {
                isMultipane = true;

                // Set fragment title to be visible (it is hidden when in activity mode)
                ((RelativeLayout) view.findViewById(R.id.detail_fragment_title)).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.title)).setText(mSelectedMovie.getmTitle());

                // Find poster imageview
                backgroundImageView = (ImageView) view.findViewById(R.id.poster_background_fragment);
            }


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

            // Set video list
            final VideoAdapter videoAdapter = new VideoAdapter(getActivity(), mSelectedMovie.getmVideos());
            ListView videoListView = (ListView) view.findViewById(R.id.video_list);
            videoListView.setAdapter(videoAdapter);
            videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    VideoItem selectedVideo = videoAdapter.getItem(i);
                    final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
                    final String VIDEO_KEY = "v";
                    Uri videoURI = Uri.parse(YOUTUBE_BASE_URL)
                            .buildUpon().appendQueryParameter(VIDEO_KEY, selectedVideo.getmURLKey())
                            .build();
                    Log.v("YoutubeURL", videoURI.toString());
                    startActivity(new Intent(Intent.ACTION_VIEW, videoURI));
                }
            });

            // Set review list
            final ReviewAdapter reviewAdapter = new ReviewAdapter(getActivity(), mSelectedMovie.getmReviews());
            ListView reviewListView = (ListView) view.findViewById(R.id.review_list);
            reviewListView.setAdapter(reviewAdapter);

            ListUtils.setDynamicHeight(videoListView);
            ListUtils.setDynamicHeight(reviewListView);


            // Set poster
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185/" + mSelectedMovie.getmPoster()).into(imageView);

            // Add image to toolbar background
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185/" + mSelectedMovie.getmPoster()).transform(new BlurTransformation(getContext())).into(backgroundImageView);
        }
        return view;
    }

    public MovieItem getSelectedMovie() {
        return mSelectedMovie;
    }

    // In order to re-size the listviews to fit all the content, since they can't scroll within an already
    // scrolling activity, I used some nice code from: http://stackoverflow.com/a/28713754.
    // Is there a better, cleaner way to do this than setting the layoutParams?
    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }

}
