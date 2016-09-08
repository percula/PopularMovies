package com.perculacreative.peter.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements GridFragment.Callback {

    public static final String PREFS_KEY = "PREFERENCES";
    public static final String PREFS_SORT_KEY = "SORT_ORDER";

    public static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static final String GRIDFRAGMENT_TAG = "GRIDTAG";
    private boolean isMultipane;

    public static final String SELECTED_MOVIE_KEY = "SELECTED_MOVIE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_detail_container) != null) {
            isMultipane = true;

            // Set grid fragment
            GridFragment gridFragment = new GridFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, gridFragment, GRIDFRAGMENT_TAG)
                    .commit();

            // Use first movie for detail fragment initialization
//            MovieItem firstMovie = gridFragment.getMovie(0);
//            Bundle args = new Bundle();
//            args.putParcelable(SELECTED_MOVIE_KEY, firstMovie);

            // Set detail fragment
            DetailFragment detailFragment = new DetailFragment();

            // If screen was just rotated, restore the previously selected movie
            if (savedInstanceState != null && savedInstanceState.containsKey(MainActivity.SELECTED_MOVIE_KEY)) {
                MovieItem selectedMovie = savedInstanceState.getParcelable(MainActivity.SELECTED_MOVIE_KEY);
                if (selectedMovie != null) {
                    Log.v("Restore in Activity", selectedMovie.getmTitle());
                }
                Bundle args = new Bundle();
                args.putParcelable(SELECTED_MOVIE_KEY, selectedMovie);
                detailFragment.setArguments(args);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                    .commit();

        } else {
            isMultipane = false;

            // Set grid fragment
            GridFragment gridFragment = new GridFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, gridFragment, GRIDFRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onItemSelected(MovieItem selectedMovie) {
        if (isMultipane) {
            Bundle args = new Bundle();
            args.putParcelable(SELECTED_MOVIE_KEY, selectedMovie);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(SELECTED_MOVIE_KEY, selectedMovie);

            startActivity(detailIntent);
        }
    }

    @Override
    public void moviesLoaded() {
        GridFragment gf = (GridFragment) getSupportFragmentManager().findFragmentByTag(GRIDFRAGMENT_TAG);
        MovieItem selectedMovie = gf.getMovie(0);
        Log.v("moviesloadedcallback", selectedMovie.getmTitle());

        DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if (df != null && df.getSelectedMovie() == null) {
            onItemSelected(selectedMovie);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if (df != null && df.getSelectedMovie() != null) {
            outState.putParcelable(MainActivity.SELECTED_MOVIE_KEY, df.getSelectedMovie());
        }
        GridFragment gf = (GridFragment) getSupportFragmentManager().findFragmentByTag(GRIDFRAGMENT_TAG);
        super.onSaveInstanceState(outState);
    }
}
