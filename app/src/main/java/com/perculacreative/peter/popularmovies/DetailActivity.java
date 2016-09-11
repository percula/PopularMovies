package com.perculacreative.peter.popularmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private final String LOG_TAG = DetailActivity.class.getSimpleName();

    private BottomBar mBottomBar;
    MovieItem mSelectedMovie;

    private ArrayList<MovieItem> mFavoriteMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get favorite movies
        try {
            loadFavoriteMovies();
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

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

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        mSelectedMovie = getIntent().getParcelableExtra(MainActivity.SELECTED_MOVIE_KEY);

        setFABIcon();

        ab.setTitle(mSelectedMovie.getmTitle());

        setInfoFragment();

        setBottomBar(savedInstanceState);
    }

    private void loadFavoriteMovies()
            throws JSONException {
        // Get favorites from sharedpreferences
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_KEY, 0);
        String favoriteString = prefs.getString(MainActivity.FAVORITE_STRING_KEY, null);
        if (favoriteString != null) {
            // I used help from http://stackoverflow.com/a/22019470/6591585 to save my objects
            // into sharedpreferences
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


    private void setBottomBar(Bundle savedInstanceState) {
        // Using WONDERFUL code for the bottom bar from roughike.com. Very easy to implement =)
        // Using previous version since there is a bug in the latest 2.0.2 release
        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.useDarkTheme();
        mBottomBar.setItems(R.menu.bottombar_menu);
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.movie_info) {
                    setInfoFragment();
                }
                if (menuItemId == R.id.movie_trailers) {
                    setVideoFragment();
                }
                if (menuItemId == R.id.movie_reviews) {
                    setReviewFragment();
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.movie_info) {
                    setInfoFragment();
                }
                if (menuItemId == R.id.movie_trailers) {
                    setVideoFragment();
                }
                if (menuItemId == R.id.movie_reviews) {
                    setReviewFragment();
                }
            }
        });
    }

    private void setInfoFragment() {
        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SELECTED_MOVIE_KEY, mSelectedMovie);

        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail, detailFragment, MainActivity.DETAILFRAGMENT_TAG)
                .commit();
    }

    private void setVideoFragment() {
        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SELECTED_MOVIE_KEY, mSelectedMovie);

        VideoFragment videoFragment = new VideoFragment();
        videoFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail, videoFragment, MainActivity.DETAILFRAGMENT_TAG)
                .commit();
    }

    private void setReviewFragment() {
        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SELECTED_MOVIE_KEY, mSelectedMovie);

        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail, reviewFragment, MainActivity.DETAILFRAGMENT_TAG)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mBottomBar.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveFavoriteMovies();
    }
}
