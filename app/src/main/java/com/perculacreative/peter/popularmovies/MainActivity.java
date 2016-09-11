package com.perculacreative.peter.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

public class MainActivity extends AppCompatActivity implements GridFragment.Callback {

    public static final String PREFS_KEY = "PREFERENCES";
    public static final String PREFS_SORT_KEY = "SORT_ORDER";

    public static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static final String VIDEOFRAGMENT_TAG = "VIDEOTAG";
    public static final String REVIEWFRAGMENT_TAG = "REVIEWTAG";

    public static final String GRIDFRAGMENT_TAG = "GRIDTAG";
    private boolean isMultipane;

    private BottomBar mBottomBar;
    private MovieItem mSelectedMovie;

    public static final String SELECTED_MOVIE_KEY = "SELECTED_MOVIE";
    public static final String MOVIE_LIST_KEY = "MOVIE_LIST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        setBottomBar(savedInstanceState);

        final FragmentManager fm = getSupportFragmentManager();

        if (findViewById(R.id.fragment_detail) != null) {
            isMultipane = true;

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
            Fragment videoFragment;
            Fragment reviewFragment;
            if (savedInstanceState != null) {
                detailFragment = (Fragment) fm.getFragment(savedInstanceState, DETAILFRAGMENT_TAG);
                videoFragment = (Fragment) fm.getFragment(savedInstanceState, VIDEOFRAGMENT_TAG);
                reviewFragment = (Fragment) fm.getFragment(savedInstanceState, REVIEWFRAGMENT_TAG);
                Log.v("Get DetailFragment", "Restore DetailFragment");
            } else {
                detailFragment = new DetailFragment();
                videoFragment = new VideoFragment();
                reviewFragment = new ReviewFragment();
                Log.v("Get DetailFragment", "New DetailFragment");
            }

//            // If screen was just rotated, restore the previously selected movie
//            if (savedInstanceState != null && savedInstanceState.containsKey(MainActivity.SELECTED_MOVIE_KEY)) {
//                MovieItem selectedMovie = savedInstanceState.getParcelable(MainActivity.SELECTED_MOVIE_KEY);
//                if (selectedMovie != null) {
//                    Log.v("Restore in Activity", selectedMovie.getmTitle());
//                }
//                Bundle args = new Bundle();
//                args.putParcelable(SELECTED_MOVIE_KEY, selectedMovie);
//                detailFragment.setArguments(args);
//            }

            fm.beginTransaction()
                    .replace(R.id.fragment_detail, detailFragment, DETAILFRAGMENT_TAG).commit();

        } else {
            isMultipane = false;

            // Set grid fragment
            GridFragment gridFragment = new GridFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, gridFragment, GRIDFRAGMENT_TAG)
                    .commit();
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
                .replace(R.id.fragment_detail, videoFragment, MainActivity.VIDEOFRAGMENT_TAG)
                .commit();
    }

    private void setReviewFragment() {
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

            Bundle args = new Bundle();
            args.putParcelable(SELECTED_MOVIE_KEY, selectedMovie);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(SELECTED_MOVIE_KEY, selectedMovie);

            startActivity(detailIntent);
        }
    }

    @Override
    public void moviesLoaded() {
        if (isMultipane) {
            GridFragment gf = (GridFragment) getSupportFragmentManager().findFragmentByTag(GRIDFRAGMENT_TAG);
            mSelectedMovie = gf.getMovie(0);
            Log.v("SelectedTitle",mSelectedMovie.getmTitle());

            setInfoFragment();
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
    }
}
