package com.perculacreative.peter.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GridFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String PREFS_KEY = "PREFERENCES";
    public static final String PREFS_SORT_KEY = "SORT_ORDER";

    public static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static final String VIDEOFRAGMENT_TAG = "VIDEOTAG";
    public static final String REVIEWFRAGMENT_TAG = "REVIEWTAG";

    public static final String GRIDFRAGMENT_TAG = "GRIDTAG";
    private boolean isMultipane;

    private MovieItem mSelectedMovie;

    public static final String SELECTED_MOVIE_KEY = "SELECTED_MOVIE";

    public static final String FAVORITE_STRING_KEY = "FAVORITES_STRING";
    public static final String FAVORITE_MOVIES_KEY = "FAVORITE_MOVIES";
    private ArrayList<MovieItem> mFavoriteMovies = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FragmentManager fm = getSupportFragmentManager();

        // Get favorite movies
        try {
            loadFavoriteMovies();
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        if (findViewById(R.id.fragment_detail) != null) {
            isMultipane = true;

            // Create FAB
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSelectedMovie != null && !isFavorite(mSelectedMovie)) {
                        Snackbar.make(view, getResources().getString(R.string.favorite_action), Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                        mFavoriteMovies.add(mSelectedMovie);
                        setFABIcon();
                        saveFavoriteMovies();
                    } else if (mSelectedMovie != null && isFavorite(mSelectedMovie)) {
                        Snackbar.make(view, getResources().getString(R.string.unfavorite_action), Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                        ((FloatingActionButton) view).setImageResource(R.drawable.ic_star_border_white_24dp);
                        removeFavorite(mSelectedMovie);
                        setFABIcon();
                        saveFavoriteMovies();
                    }
                }
            });

            // Set grid fragment
            GridFragment gridFragment;
            if (savedInstanceState != null) {
                gridFragment = (GridFragment) fm.getFragment(savedInstanceState, GRIDFRAGMENT_TAG);
            } else {
                gridFragment = new GridFragment();
            }
            fm.beginTransaction()
                    .replace(R.id.fragment_container, gridFragment, GRIDFRAGMENT_TAG)
                    .commit();


            // Set detail fragment
            Fragment detailFragment;
            if (savedInstanceState != null) {
                // Retrieve selected movie
                if (savedInstanceState.containsKey(MainActivity.SELECTED_MOVIE_KEY)) {
                    mSelectedMovie = savedInstanceState.getParcelable(MainActivity.SELECTED_MOVIE_KEY);
                }
                // Retrieve favorite movies
                if (savedInstanceState.containsKey(MainActivity.FAVORITE_MOVIES_KEY)) {
                    mFavoriteMovies = savedInstanceState.getParcelableArrayList(MainActivity.FAVORITE_MOVIES_KEY);
                }
            } else {
                // If no saved instance state, create a new DetailFragment
                detailFragment = new DetailFragment();
                fm.beginTransaction()
                        .replace(R.id.fragment_detail, detailFragment, DETAILFRAGMENT_TAG).commit();
            }

            // Determine which detail fragment is shown and set button clicked
            String currentTag;
            if (fm.findFragmentById(R.id.fragment_detail) != null) {
                currentTag = fm.findFragmentById(R.id.fragment_detail).getTag();
            } else {
                // If no tag, assume info tab is selected
                currentTag = DETAILFRAGMENT_TAG;
            }
            if (currentTag.equals(DETAILFRAGMENT_TAG)) {
                setSelectedButton(true, false, false);
            }
            if (currentTag.equals(VIDEOFRAGMENT_TAG)) {
                setSelectedButton(false, true, false);
            }
            if (currentTag.equals(REVIEWFRAGMENT_TAG)) {
                setSelectedButton(false, false, true);
            }

            // Setup bottom bar buttons
            findViewById(R.id.movie_info).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setInfoFragment();
                }
            });
            findViewById(R.id.movie_videos).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setVideoFragment();
                }
            });
            findViewById(R.id.movie_reviews).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setReviewFragment();
                }
            });

        } else {
            isMultipane = false;

            // Set grid fragment
            GridFragment gridFragment = new GridFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, gridFragment, GRIDFRAGMENT_TAG)
                    .commit();
        }
    }

    private void loadFavoriteMovies()
            throws JSONException {
        // Get favorites from sharedpreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_KEY, 0);
        String favoriteString = prefs.getString(FAVORITE_STRING_KEY, null);
        if (favoriteString != null) {
            Type type = new TypeToken<List<MovieItem>>() {
            }.getType();
            Gson gson = new Gson();
            mFavoriteMovies = gson.fromJson(favoriteString, type);
        }
    }

    private void saveFavoriteMovies() {
        Gson gson = new Gson();
        String favoritesList = gson.toJson(mFavoriteMovies);

        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_KEY, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(MainActivity.FAVORITE_STRING_KEY, favoritesList).apply();
    }

    private boolean isFavorite(MovieItem movieItem) {
        for (int i = 0; i < mFavoriteMovies.size(); i++) {
            if (mSelectedMovie.getmID().equals(mFavoriteMovies.get(i).getmID())) {
                return true;
            }
        }
        return false;
    }

    private void removeFavorite(MovieItem movieItem) {
        for (int i = 0; i < mFavoriteMovies.size(); i++) {
            if (mSelectedMovie.getmID().equals(mFavoriteMovies.get(i).getmID())) {
                mFavoriteMovies.remove(i);
            }
        }
    }

    private void setFABIcon() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (isFavorite(mSelectedMovie)) {
            fab.setImageResource(R.drawable.ic_star_white_24dp);
        } else {
            fab.setImageResource(R.drawable.ic_star_border_white_24dp);
        }
    }

    private void setSelectedButton(boolean left, boolean middle, boolean right) {
        ImageButton v1 = (ImageButton) findViewById(R.id.movie_info);
        ImageButton v2 = (ImageButton) findViewById(R.id.movie_videos);
        ImageButton v3 = (ImageButton) findViewById(R.id.movie_reviews);

        float pressed = 0.8f;
        float normal = 1.0f;

        if (left) {
            v1.setAlpha(pressed);
            v2.setAlpha(normal);
            v3.setAlpha(normal);
        }
        if (middle) {
            v1.setAlpha(normal);
            v2.setAlpha(pressed);
            v3.setAlpha(normal);
        }
        if (right) {
            v1.setAlpha(normal);
            v2.setAlpha(normal);
            v3.setAlpha(pressed);
        }
    }

    private void setInfoFragment() {
        setSelectedButton(true, false, false);

        DetailFragment detailFragment = new DetailFragment();

        if (mSelectedMovie != null) {
            Bundle args = new Bundle();
            args.putParcelable(MainActivity.SELECTED_MOVIE_KEY, mSelectedMovie);
            detailFragment.setArguments(args);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail, detailFragment, MainActivity.DETAILFRAGMENT_TAG)
                .commit();
    }


    private void setVideoFragment() {
        setSelectedButton(false, true, false);

        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SELECTED_MOVIE_KEY, mSelectedMovie);

        VideoFragment videoFragment = new VideoFragment();
        videoFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail, videoFragment, MainActivity.VIDEOFRAGMENT_TAG)
                .commit();
    }

    private void setReviewFragment() {
        setSelectedButton(false, false, true);

        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SELECTED_MOVIE_KEY, mSelectedMovie);

        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail, reviewFragment, MainActivity.REVIEWFRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onItemSelected(MovieItem selectedMovie) {
        if (isMultipane) {
            mSelectedMovie = selectedMovie;

            setInfoFragment();
            setFABIcon();
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(SELECTED_MOVIE_KEY, selectedMovie);

            startActivity(detailIntent);
        }
    }

    @Override
    public void moviesLoaded(MovieItem firstMovie) {
        if (isMultipane) {
            mSelectedMovie = firstMovie;

            setInfoFragment();
            setFABIcon();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if (df != null) {
            getSupportFragmentManager().putFragment(outState, DETAILFRAGMENT_TAG, df);
        }
        VideoFragment vf = (VideoFragment) getSupportFragmentManager().findFragmentByTag(VIDEOFRAGMENT_TAG);
        if (vf != null) {
            getSupportFragmentManager().putFragment(outState, VIDEOFRAGMENT_TAG, vf);
        }
        ReviewFragment rf = (ReviewFragment) getSupportFragmentManager().findFragmentByTag(REVIEWFRAGMENT_TAG);
        if (rf != null) {
            getSupportFragmentManager().putFragment(outState, REVIEWFRAGMENT_TAG, rf);
        }
        GridFragment gf = (GridFragment) getSupportFragmentManager().findFragmentByTag(GRIDFRAGMENT_TAG);
        if (gf != null) {
            getSupportFragmentManager().putFragment(outState, GRIDFRAGMENT_TAG, gf);
        }
        if (mSelectedMovie != null) {
            outState.putParcelable(SELECTED_MOVIE_KEY, mSelectedMovie);
        }
        if (mFavoriteMovies != null) {
            outState.putParcelableArrayList(FAVORITE_MOVIES_KEY, mFavoriteMovies);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveFavoriteMovies();
    }
}
