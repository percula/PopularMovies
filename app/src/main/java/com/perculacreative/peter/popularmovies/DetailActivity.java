package com.perculacreative.peter.popularmovies;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

public class DetailActivity extends AppCompatActivity {

    private BottomBar mBottomBar;
    MovieItem mSelectedMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail2);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        mSelectedMovie = getIntent().getParcelableExtra(MainActivity.SELECTED_MOVIE_KEY);


        ab.setTitle(mSelectedMovie.getmTitle());

        setInfoFragment();

        setBottomBar(savedInstanceState);
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
                .replace(R.id.fragment_detail_scrollview, detailFragment, MainActivity.DETAILFRAGMENT_TAG)
                .commit();
    }

    private void setVideoFragment() {
        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SELECTED_MOVIE_KEY, mSelectedMovie);

        VideoFragment videoFragment = new VideoFragment();
        videoFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail_scrollview, videoFragment, MainActivity.DETAILFRAGMENT_TAG)
                .commit();
    }

    private void setReviewFragment() {
        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SELECTED_MOVIE_KEY, mSelectedMovie);

        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail_scrollview, reviewFragment, MainActivity.DETAILFRAGMENT_TAG)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mBottomBar.onSaveInstanceState(outState);
    }
}
